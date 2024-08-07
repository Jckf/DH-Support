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

package no.jckf.dhsupport.core.handler;

import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.message.plugin.FullDataChunkMessage;
import no.jckf.dhsupport.core.message.plugin.FullDataSourceRequestMessage;
import no.jckf.dhsupport.core.message.plugin.FullDataSourceResponseMessage;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.UUID;

public class LodHandler
{
    protected static int CHUNK_SIZE = 1024 * 16; // TODO: Configurable?

    protected DhSupport dhSupport;

    protected PluginMessageHandler pluginMessageHandler;

    protected int bufferId = 0; // TODO: Should be tracked per player instead of globally, and reset when a player disconnects.

    public LodHandler(DhSupport dhSupport, PluginMessageHandler pluginMessageHandler)
    {
        this.dhSupport = dhSupport;
        this.pluginMessageHandler = pluginMessageHandler;
    }

    public void register()
    {
        this.pluginMessageHandler.getEventBus().registerHandler(FullDataSourceRequestMessage.class, (requestMessage) -> {
            //this.dhSupport.info("LOD request for " + requestMessage.getPosition().getX() + " x " + requestMessage.getPosition().getZ());

            int myBufferId = this.bufferId++;

            // TODO: Some sort of Player wrapper or interface object. Bukkit classes should not be imported here.
            UUID worldUuid = Bukkit.getPlayer(requestMessage.getSender()).getWorld().getUID();

            this.dhSupport.getLodData(worldUuid, requestMessage.getPosition())
                .thenAccept((lod) -> {
                    int chunkCount = (int) Math.ceil((double) lod.length / CHUNK_SIZE);

                    for (int chunkNo = 0; chunkNo < chunkCount; chunkNo++) {
                        FullDataChunkMessage chunkResponse = new FullDataChunkMessage();
                        chunkResponse.setBufferId(myBufferId);
                        chunkResponse.setIsFirst(chunkNo == 0);
                        chunkResponse.setData(Arrays.copyOfRange(
                            lod,
                            CHUNK_SIZE * chunkNo,
                            Math.min(CHUNK_SIZE * chunkNo + CHUNK_SIZE, lod.length)
                        ));

                        this.pluginMessageHandler.sendPluginMessage(requestMessage.getSender(), chunkResponse);
                    }

                    FullDataSourceResponseMessage responseMessage = new FullDataSourceResponseMessage();
                    responseMessage.isResponseTo(requestMessage);
                    responseMessage.setBufferId(myBufferId);

                    this.pluginMessageHandler.sendPluginMessage(requestMessage.getSender(), responseMessage);

                    //this.dhSupport.info("LOD in " + chunkCount + " parts sent for " + requestMessage.getPosition().getX() + " x " + requestMessage.getPosition().getZ());
                })
                .exceptionally((exception) -> {
                    this.dhSupport.warning(exception.getMessage());
                    return null;
                });
        });
    }
}
