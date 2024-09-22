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
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import no.jckf.dhsupport.core.exceptions.DatabaseException;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public class DhSupport implements Configurable
{
    protected String dataDirectory;

    protected Database database;

    protected Configuration configuration;

    protected Logger logger;

    protected Scheduler scheduler;

    protected Map<UUID, WorldInterface> worldInterfaces = new HashMap<>();

    protected PluginMessageHandler pluginMessageHandler;

    protected PluginMessageSender pluginMessageSender;

    protected Queue<PreparedStatement> queuedInserts = new ConcurrentLinkedQueue<>();

    public DhSupport()
    {
        this.database = new Database(this);

        this.pluginMessageHandler = new PluginMessageHandler(this);
    }

    public void onEnable()
    {
        try {
            this.database.open();

            this.database.addMigration("Create LODs table", CreateLodsTable.class);

            this.database.migrate();
        } catch (DatabaseException exception) {
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
            //this.info("Removing world interface for " + id);

            this.worldInterfaces.remove(id);
            return;
        }

        //this.info("Adding world interface for " + id);

        this.worldInterfaces.put(id, worldInterface);
    }

    @Nullable
    public WorldInterface getWorldInterface(UUID id)
    {
        return this.worldInterfaces.get(id);
    }

    @Nullable
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

    @Override
    public Configuration getConfig()
    {
        if (this.configuration == null) {
            this.configuration = new Configuration();
        }

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

    public byte[] getLodFromDatabase(UUID worldId, int x, int z)
    {
        String sql = "SELECT data FROM lods WHERE worldId = ? AND x = ? AND z = ?;";

        try (PreparedStatement statement = this.database.getConnection().prepareStatement(sql)) {
            statement.setString(1, worldId.toString());
            statement.setInt(2, x);
            statement.setInt(3, z);

            ResultSet result = statement.executeQuery();

            return result.getBytes(1);
        } catch (SQLException | DatabaseException exception) {
            this.warning("Error while fetching LOD from database: " + exception.getMessage());
            return null;
        }
    }

    public void queueInsertLodIntoDatabase(UUID worldId, int x, int z, byte[] data)
    {
        String sql = "INSERT INTO lods (worldId, x, z, data) VALUES (?, ?, ?, ?);";

        try {
            PreparedStatement statement = this.database.getConnection().prepareStatement(sql);

            statement.setString(1, worldId.toString());
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.setBytes(4, data);

            this.queuedInserts.add(statement);
        } catch (SQLException | DatabaseException exception) {
            this.warning("Error while queuing insert: " + exception.getMessage());
        }
    }

    public int executeQueuedInserts()
    {
        if (this.queuedInserts.isEmpty()) {
            return 0;
        }

        int inserted = 0;

        while (true) {
            try (PreparedStatement statement = this.queuedInserts.poll()) {
                if (statement == null) {
                    break;
                }

                statement.executeUpdate();

                inserted++;
            } catch (SQLException exception) {
                this.warning("Error while executing queued insert: " + exception.getMessage());
            }
        }

        return inserted;
    }

    public CompletableFuture<Lod> queueBuilder(UUID worldId, SectionPosition position, LodBuilder builder)
    {
        return this.getScheduler().runRegional(
            worldId,
            position.getX() * 64,
            position.getZ() * 64,
            builder::generate
        );
    }

    public CompletableFuture<byte[]> getLodData(UUID worldId, SectionPosition position)
    {
        byte[] fromDatabase = this.getLodFromDatabase(worldId, position.getX(), position.getZ());

        if (fromDatabase != null) {
            return CompletableFuture.completedFuture(fromDatabase);
        }

        WorldInterface world = this.getWorldInterface(worldId);

        String builderType = world.getConfig().getString(DhsConfig.BUILDER_TYPE);

        LodBuilder builder;

        try {
            Class<? extends LodBuilder> builderClass = Class.forName("no.jckf.dhsupport.core.lodbuilders." + builderType).asSubclass(LodBuilder.class);

            Constructor<? extends LodBuilder> constructor = builderClass.getConstructor(WorldInterface.class, SectionPosition.class);

            builder = constructor.newInstance(world.newInstance(), position);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            this.warning(exception.toString());

            return CompletableFuture.failedFuture(new Exception("Failed to instantiate LOD builder \"" + builderType + "\" for world \"" + world.getName() + "\"."));
        }

        return this.queueBuilder(worldId, position, builder).thenApply((lod) -> {
            Encoder encoder = new Encoder();
            lod.encode(encoder);
            byte[] data = encoder.toByteArray();

            this.queueInsertLodIntoDatabase(worldId, position.getX(), position.getZ(), data);

            return data;
        });
    }
}
