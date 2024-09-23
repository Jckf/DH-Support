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

import no.jckf.dhsupport.core.Coordinates;
import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.DhsConfig;
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import no.jckf.dhsupport.core.message.plugin.ExceptionMessage;
import no.jckf.dhsupport.core.message.plugin.FullDataChunkMessage;
import no.jckf.dhsupport.core.message.plugin.FullDataSourceRequestMessage;
import no.jckf.dhsupport.core.message.plugin.FullDataSourceResponseMessage;
import no.jckf.dhsupport.core.world.WorldInterface;
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

            // TODO: Some sort of Player wrapper or interface object. Bukkit classes should not be imported here.
            UUID worldUuid = Bukkit.getPlayer(requestMessage.getSender()).getWorld().getUID();

            WorldInterface world = this.dhSupport.getWorldInterface(worldUuid);

            Configuration config = world.getConfig();

            SectionPosition position = requestMessage.getPosition();

            int worldX = Coordinates.sectionToBlock(position.getX());
            int worldZ = Coordinates.sectionToBlock(position.getZ());

            String borderCenter = config.getString(DhsConfig.BORDER_CENTER);
            Integer borderRadius = config.getInt(DhsConfig.BORDER_RADIUS);

            if (borderCenter != null && borderRadius != null) {
                String[] centerXz = borderCenter.split(",");

                if (centerXz.length != 2) {
                    this.dhSupport.warning("Border for world " + world.getName() + " is misconfigured.");
                } else {
                    int centerX = Integer.getInteger(centerXz[0]);
                    int centerZ = Integer.getInteger(centerXz[1]);

                    int minX = centerX - borderRadius;
                    int maxX = centerX + borderRadius;

                    int minZ = centerZ - borderRadius;
                    int maxZ = centerZ + borderRadius;

                    int higherLodX = worldX + 64;
                    int higherLodZ = worldZ + 64;

                    if (higherLodX < minX || worldX > maxX || higherLodZ < minZ || worldZ > maxZ) {
                        ExceptionMessage exceptionMessage = new ExceptionMessage();
                        exceptionMessage.isResponseTo(requestMessage);
                        exceptionMessage.setTypeId(ExceptionMessage.TYPE_REQUEST_REJECTED);
                        exceptionMessage.setMessage("World border");
                        this.pluginMessageHandler.sendPluginMessage(requestMessage.getSender(), exceptionMessage);
                        return;
                    }
                }
            }

            boolean generate = config.getBool(DhsConfig.GENERATE_NEW_CHUNKS);

            if (!generate) {
                for (int relativeChunkX = 0; relativeChunkX < Lod.width / 16; relativeChunkX++) {
                    for (int relativeChunkZ = 0; relativeChunkZ < Lod.width / 16; relativeChunkZ++) {
                        if (!world.chunkExists(worldX + relativeChunkX * 16, worldZ + relativeChunkZ * 16)) {
                            ExceptionMessage exceptionMessage = new ExceptionMessage();
                            exceptionMessage.isResponseTo(requestMessage);
                            exceptionMessage.setTypeId(ExceptionMessage.TYPE_REQUEST_REJECTED);
                            exceptionMessage.setMessage("Fog of war");
                            this.pluginMessageHandler.sendPluginMessage(requestMessage.getSender(), exceptionMessage);
                            return;
                        }
                    }
                }
            }

            int myBufferId = this.bufferId++;

            this.dhSupport.getLodData(worldUuid, position)
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

                    ExceptionMessage exceptionMessage = new ExceptionMessage();
                    exceptionMessage.isResponseTo(requestMessage);
                    exceptionMessage.setTypeId(ExceptionMessage.TYPE_REQUEST_REJECTED);
                    exceptionMessage.setMessage("Internal error");
                    this.pluginMessageHandler.sendPluginMessage(requestMessage.getSender(), exceptionMessage);

                    return null;
                });
        });
    }
}
