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
import no.jckf.dhsupport.core.database.repositories.LodRepository;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DhSupport implements Configurable
{
    protected String dataDirectory;

    protected Database database;

    protected LodRepository lodRepository;

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
        this.lodRepository = new LodRepository(this.database);

        this.pluginMessageHandler = new PluginMessageHandler(this);
    }

    public void onEnable()
    {
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

    public LodRepository getLodRepository()
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

        CompletableFuture<Lod> queued = this.getScheduler().runRegional(
                worldId,
                Coordinates.sectionToBlock(position.getX()),
                Coordinates.sectionToBlock(position.getZ()),
                builder::generate
            )
            .thenApply((lod) -> {
                this.queuedBuilders.remove(key);

                return lod;
            })
            .exceptionally((nothing) -> {
                this.queuedBuilders.remove(key);

                return null;
            });

        this.queuedBuilders.put(key, queued);

        return queued;
    }

    public CompletableFuture<byte[]> getLodData(UUID worldId, SectionPosition position)
    {
        return this.getLodData(worldId, position, false);
    }

    public CompletableFuture<byte[]> getLodData(UUID worldId, SectionPosition position, boolean recreate)
    {
        if (!recreate) {
            LodModel lodModel = this.lodRepository.loadLod(worldId, position.getX(), position.getZ());

            if (lodModel != null) {
                return CompletableFuture.completedFuture(lodModel.getData());
            }
        }

        LodBuilder builder = this.getBuilder(worldId, position);

        return this.queueBuilder(worldId, position, builder).thenApply((lod) -> {
            Encoder encoder = new Encoder();
            lod.encode(encoder);
            byte[] data = encoder.toByteArray();

            this.lodRepository.saveLodQueued(worldId, position.getX(), position.getZ(), data);

            return data;
        });
    }

    public void touchLod(UUID worldId, int x, int z)
    {
        int sectionX = Coordinates.blockToSection(x);
        int sectionZ = Coordinates.blockToSection(z);

        if (!this.lodRepository.lodExists(worldId, sectionX, sectionZ)) {
            return;
        }

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

    public int updateTouchedLods()
    {
        int updates = 0;

        for (String key : this.touchedLods.keySet()) {
            LodModel lodModel = this.touchedLods.get(key);

            this.getLodRepository().deleteLod(lodModel.getWorldId(), lodModel.getX(), lodModel.getZ());

            SectionPosition position = new SectionPosition();
            position.setX(lodModel.getX());
            position.setZ(lodModel.getZ());

            this.getLodData(lodModel.getWorldId(), position, true);

            updates++;

            this.touchedLods.remove(key);
        }

        return updates;
    }
}
