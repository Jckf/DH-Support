/*
 * DH Support, server-side support for Distant Horizons.
 * Copyright (C) 2024 Jim C K Flaten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package no.jckf.dhsupport.core;

import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.configuration.Configurable;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.DhsConfig;
import no.jckf.dhsupport.core.database.Database;
import no.jckf.dhsupport.core.database.migrations.CreateLodsTable;
import no.jckf.dhsupport.core.database.models.LodModel;
import no.jckf.dhsupport.core.database.repositories.AsyncLodRepository;
import no.jckf.dhsupport.core.dataobject.Beacon;
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import no.jckf.dhsupport.core.handler.LodHandler;
import no.jckf.dhsupport.core.handler.PlayerConfigHandler;
import no.jckf.dhsupport.core.handler.PluginMessageHandler;
import no.jckf.dhsupport.core.lodbuilders.LodBuilder;
import no.jckf.dhsupport.core.message.plugin.PluginMessageSender;
import no.jckf.dhsupport.core.scheduling.Scheduler;
import no.jckf.dhsupport.core.world.WorldInterface;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DhSupport implements Configurable
{
    protected String dataDirectory;

    protected Database database;

    protected AsyncLodRepository lodRepository;

    protected Configuration configuration;

    protected Logger logger;

    protected Scheduler scheduler;

    protected Map<UUID, WorldInterface> worldInterfaces = new HashMap<>();

    protected PluginMessageHandler pluginMessageHandler;

    protected PluginMessageSender pluginMessageSender;

    protected Map<String, CompletableFuture<Lod>> queuedBuilders = new HashMap<>();

    protected Map<String, LodModel> touchedLods = new ConcurrentHashMap<>();

    public DhSupport()
    {
        this.configuration = new Configuration();

        this.database = new Database();
        this.lodRepository = new AsyncLodRepository(this.database);

        this.pluginMessageHandler = new PluginMessageHandler(this);
    }

    public void onEnable()
    {
        this.lodRepository.setLogger(this.getLogger());

        try {
            this.database.open(this.getDataDirectory() + "/data.sqlite");

            this.database.addMigration(CreateLodsTable.class);

            this.database.migrate();
        } catch (Exception exception) {
            this.warning("Failed to initialize database: " + exception.getMessage());
            // TODO: Disable the plugin?
        }

        (new PlayerConfigHandler(this, this.pluginMessageHandler)).register();
        (new LodHandler(this, this.pluginMessageHandler)).register();

        this.pluginMessageHandler.onEnable();
    }

    public void onDisable()
    {
        if (this.pluginMessageHandler != null) {
            this.pluginMessageHandler.onDisable();
        }
    }

    public void setDataDirectory(String dataDirectory)
    {
        this.dataDirectory = dataDirectory;
    }

    public String getDataDirectory()
    {
        return this.dataDirectory;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler()
    {
        return this.scheduler;
    }

    public void setWorldInterface(UUID id, @Nullable WorldInterface worldInterface)
    {
        if (worldInterface == null) {
            this.worldInterfaces.remove(id);
            return;
        }

        this.worldInterfaces.put(id, worldInterface);
    }

    @Nullable
    public WorldInterface getWorldInterface(UUID id)
    {
        return this.worldInterfaces.get(id);
    }

    public PluginMessageHandler getPluginMessageHandler()
    {
        return this.pluginMessageHandler;
    }

    public void setPluginMessageSender(PluginMessageSender sender)
    {
        this.pluginMessageSender = sender;
    }

    @Nullable
    public PluginMessageSender getPluginMessageSender()
    {
        return this.pluginMessageSender;
    }

    public AsyncLodRepository getLodRepository()
    {
        return this.lodRepository;
    }

    public Configuration getConfig()
    {
        return this.configuration;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Nullable
    public Logger getLogger()
    {
        return this.logger;
    }

    public void info(String message)
    {
        this.getLogger().info(message);
    }

    public void warning(String message)
    {
        this.getLogger().warning(message);
    }

    public LodBuilder getBuilder(UUID worldId, SectionPosition position)
    {
        String builderType = this.getWorldInterface(worldId).getConfig().getString(DhsConfig.BUILDER_TYPE);

        LodBuilder builder;

        try {
            Class<? extends LodBuilder> builderClass = Class.forName(LodBuilder.class.getPackageName() + "." + builderType).asSubclass(LodBuilder.class);

            Constructor<?> constructor = builderClass.getConstructor(WorldInterface.class, SectionPosition.class);

            builder = (LodBuilder) constructor.newInstance(this.getWorldInterface(worldId).newInstance(), position);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException exception) {
            return null;
        }

        return builder;
    }

    public CompletableFuture<Lod> queueBuilder(UUID worldId, SectionPosition position, LodBuilder builder)
    {
        String key = LodModel.create()
            .setWorldId(worldId)
            .setX(position.getX())
            .setZ(position.getZ())
            .toString();

        if (this.queuedBuilders.containsKey(key)) {
            return this.queuedBuilders.get(key);
        }

        Scheduler scheduler = this.getScheduler();

        CompletableFuture<Lod> queued;

        if (scheduler.canReadWorldAsync()) {
            queued = this.getScheduler().runOnSeparateThread(
                builder::generate
            );
        } else {
            queued = this.getScheduler().runOnRegionThread(
                worldId,
                Coordinates.sectionToBlock(position.getX()),
                Coordinates.sectionToBlock(position.getZ()),
                builder::generate
            );
        }

        queued = queued.thenApply((lod) -> {
                this.queuedBuilders.remove(key);

                return lod;
            })
            .exceptionally((exception) -> {
                exception.printStackTrace();

                this.queuedBuilders.remove(key);

                return null;
            });

        this.queuedBuilders.put(key, queued);

        return queued;
    }

    public CompletableFuture<LodModel> getLod(UUID worldId, SectionPosition position)
    {
        return this.getLodRepository().loadLodAsync(worldId, position.getX(), position.getZ())
            .thenCompose((lodModel) -> {
                if (lodModel != null) {
                    return CompletableFuture.completedFuture(lodModel);
                }

                return this.queueBuilder(worldId, position, this.getBuilder(worldId, position))
                    .thenCompose((lod) -> {
                        WorldInterface world = this.getWorldInterface(worldId);

                        int worldX = Coordinates.sectionToBlock(position.getX());
                        int worldZ = Coordinates.sectionToBlock(position.getZ());

                        Collection<Beacon> beacons = this.getScheduler()
                            .runOnRegionThread(worldId, worldX, worldZ, () -> {
                                Collection<Beacon> accumulator = new ArrayList<>();

                                for (int xMultiplier = 0; xMultiplier < 4; xMultiplier++) {
                                    for (int zMultiplayer = 0; zMultiplayer < 4; zMultiplayer++) {
                                        accumulator.addAll(world.getBeaconsInChunk(worldX + 16 * xMultiplier, worldZ + 16 * zMultiplayer));
                                    }
                                }

                                return accumulator;
                            })
                            .join();

                        Encoder lodEncoder = new Encoder();
                        lod.encode(lodEncoder);

                        Encoder beaconEncoder = new Encoder();
                        beaconEncoder.writeCollection(beacons);

                        return this.lodRepository.saveLodAsync(
                            worldId,
                            position.getX(),
                            position.getZ(),
                            lodEncoder.toByteArray(),
                            beaconEncoder.toByteArray()
                        );
                    });
            });
    }

    public void touchLod(UUID worldId, int x, int z)
    {
        int sectionX = Coordinates.blockToSection(x);
        int sectionZ = Coordinates.blockToSection(z);

        LodModel lodModel = LodModel.create()
            .setWorldId(worldId)
            .setX(sectionX)
            .setZ(sectionZ);

        String key = lodModel.toString();

        if (this.touchedLods.containsKey(key)) {
            return;
        }

        this.touchedLods.put(key, lodModel);
    }

    public void updateTouchedLods()
    {
        for (String key : this.touchedLods.keySet()) {
            LodModel lodModel = this.touchedLods.get(key);

            this.getLodRepository().deleteLodAsync(lodModel.getWorldId(), lodModel.getX(), lodModel.getZ())
                .thenAccept((deleted) -> {
                    if (!deleted) {
                        return;
                    }

                    SectionPosition position = new SectionPosition();
                    position.setDetailLevel(6);
                    position.setX(lodModel.getX());
                    position.setZ(lodModel.getZ());

                    this.getLod(lodModel.getWorldId(), position);

                    this.touchedLods.remove(key);
                });
        }
    }
}
