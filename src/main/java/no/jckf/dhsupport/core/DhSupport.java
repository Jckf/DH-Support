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

import io.netty.channel.ChannelId;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.configuration.Configurable;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import no.jckf.dhsupport.core.handler.PluginMessageHandler;
import no.jckf.dhsupport.core.handler.SocketMessageHandler;
import no.jckf.dhsupport.core.handler.message.PluginHandshakeHandler;
import no.jckf.dhsupport.core.handler.message.SocketHandshakeHandler;
import no.jckf.dhsupport.core.handler.message.SocketLodHandler;
import no.jckf.dhsupport.core.message.plugin.PluginMessageSender;
import no.jckf.dhsupport.core.world.WorldInterface;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class DhSupport implements Configurable
{
    protected Configuration configuration;

    protected Logger logger;

    protected Map<UUID, WorldInterface> worldInterfaces = new HashMap<>();

    protected PluginMessageHandler pluginMessageHandler;

    protected SocketMessageHandler socketMessageHandler;

    protected PluginMessageSender pluginMessageSender;

    protected Map<ChannelId, UUID> socketToUuidMap = new HashMap<>();

    protected Map<UUID, ChannelId> uuidToSocketMap = new HashMap<>();

    protected ExecutorService executor;

    protected Map<UUID, Map<String, byte[]>> lodCache = new HashMap<>();

    public DhSupport()
    {
        // Mumble mumble. Something about passing references to an incomplete "this".
        this.pluginMessageHandler = new PluginMessageHandler(this);
        this.socketMessageHandler = new SocketMessageHandler(this);
    }

    public void onEnable()
    {
        this.executor = Executors.newFixedThreadPool(4);

        // TODO: Store these instances somewhere?
        //       References will exist inside the event bus, so they won't be GCed, but this _is_ a little bit ugly.
        (new PluginHandshakeHandler(this, this.pluginMessageHandler)).register();

        (new SocketHandshakeHandler(this, this.socketMessageHandler)).register();
        (new SocketLodHandler(this, this.socketMessageHandler)).register();

        this.pluginMessageHandler.onEnable();
        this.socketMessageHandler.onEnable();
    }

    public void onDisable()
    {
        if (this.socketMessageHandler != null) {
            this.socketMessageHandler.onDisable();
        }

        if (this.pluginMessageHandler != null) {
            this.pluginMessageHandler.onDisable();
        }
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

    @Nullable
    public SocketMessageHandler getSocketMessageHandler()
    {
        return this.socketMessageHandler;
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

    public void associatePlayerAndSocket(UUID playerUuid, ChannelId socketId)
    {
        this.socketToUuidMap.put(socketId, playerUuid);
        this.uuidToSocketMap.put(playerUuid, socketId);

        //this.info("Associated " + socketId + "/" + playerUuid);
    }

    public void closeAndForgetByUuid(UUID playerUuid)
    {
        this.closeAndForgetBySocketId(this.getSocketIdByPlayerUuid(playerUuid));
    }

    public void closeAndForgetBySocketId(ChannelId id)
    {
        try {
            this.socketMessageHandler.getSocketServer().getSocket(id).close();
        } catch (Exception exception) {
            // Whatever. It was probably closed already.
        }

        UUID uuid = this.socketToUuidMap.remove(id);
        this.uuidToSocketMap.remove(uuid);

        //this.info("Forgot " + id + "/" + uuid);
    }

    @Nullable
    public ChannelId getSocketIdByPlayerUuid(UUID uuid)
    {
        return this.uuidToSocketMap.get(uuid);
    }

    @Nullable
    public UUID getPlayerUuidBySocketId(ChannelId id)
    {
        return this.socketToUuidMap.get(id);
    }

    public CompletableFuture<Lod> queueBuilder(LodBuilder builder)
    {
        return CompletableFuture.supplyAsync(builder::generate, this.executor);
    }

    public CompletableFuture<byte[]> getLodData(UUID worldId, SectionPosition position)
    {
        String key = position.getX() + "x" + position.getZ();

        if (!this.lodCache.get(worldId).containsKey(key)) {
            //this.info("Cache miss: " + worldId + " " + key);

            LodBuilder builder = new LodBuilder(this.getWorldInterface(worldId).newInstance(), position);

            return this.queueBuilder(builder).thenApply((lod) -> {
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
