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
    protected Configuration configuration;

    protected Logger logger;

    protected Scheduler scheduler;

    protected Map<UUID, WorldInterface> worldInterfaces = new HashMap<>();

    protected PluginMessageHandler pluginMessageHandler;

    protected PluginMessageSender pluginMessageSender;

    protected Map<UUID, Map<String, byte[]>> lodCache = new ConcurrentHashMap<>();

    public DhSupport()
    {
        // Mumble mumble. Something about passing references to an incomplete "this".
        this.pluginMessageHandler = new PluginMessageHandler(this);
    }

    public void onEnable()
    {
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
            this.lodCache.remove(id);
            return;
        }

        //this.info("Adding world interface for " + id);

        this.worldInterfaces.put(id, worldInterface);
        this.lodCache.put(id, new HashMap<>());
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
        String key = position.getX() + "x" + position.getZ();

        if (!this.lodCache.get(worldId).containsKey(key)) {
            //this.info("Cache miss: " + worldId + " " + key);

            WorldInterface world = this.getWorldInterface(worldId);

            String builderType = world.getConfig().getString(DhsConfig.BUILDER_TYPE);

            LodBuilder builder;

            try {
                Class<? extends LodBuilder> builderClass = Class.forName("no.jckf.dhsupport.core.lodbuilders." + builderType).asSubclass(LodBuilder.class);

                Constructor<? extends LodBuilder> constructor = builderClass.getConstructor(WorldInterface.class, SectionPosition.class);

                builder = constructor.newInstance(world, position);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                this.getLogger().severe(exception.toString());

                return CompletableFuture.failedFuture(new Exception("Failed to instantiate LOD builder \"" + builderType + "\" for world \"" + world.getName() + "\"."));
            }

            return this.queueBuilder(worldId, position, builder).thenApply((lod) -> {
                Encoder encoder = new Encoder();
                lod.encode(encoder);
                byte[] data = encoder.toByteArray();

                this.lodCache.get(worldId).put(key, data);

                return data;
            });
        }

        //this.info("Cache hit: " + worldId + " " + key);

        return CompletableFuture.completedFuture(this.lodCache.get(worldId).get(key));
    }
}
