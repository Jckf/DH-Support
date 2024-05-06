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

package no.jckf.dhsupport.core.handler.message;

import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.handler.SocketMessageHandler;
import no.jckf.dhsupport.core.message.socket.FullDataRequestSocketMessage;
import no.jckf.dhsupport.core.message.socket.FullDataResponseSocketMessage;
import org.bukkit.Bukkit;

import java.util.UUID;

public class SocketLodHandler
{
    protected DhSupport dhSupport;

    protected SocketMessageHandler socketMessageHandler;

    public SocketLodHandler(DhSupport dhSupport, SocketMessageHandler socketMessageHandler)
    {
        this.dhSupport = dhSupport;
        this.socketMessageHandler = socketMessageHandler;
    }

    public void register()
    {
        this.socketMessageHandler.getEventBus().registerHandler(FullDataRequestSocketMessage.class, (requestMessage) -> {
            UUID playerUuid = this.dhSupport.getPlayerUuidBySocketId(requestMessage.getSender().id());

            // This happens when a player disconnects with pending messages.
            if (playerUuid == null) {
                return;
            }

            // TODO: Some sort of Player wrapper or interface object. Bukkit classes should not be imported here.
            UUID worldUuid = Bukkit.getPlayer(playerUuid).getWorld().getUID();

            // TODO: Verify that the requested section is withing the player's view distance.

            this.dhSupport.getLodData(worldUuid, requestMessage.getPosition())
                .thenAccept((lodData) -> {
                    FullDataResponseSocketMessage response = new FullDataResponseSocketMessage();
                    response.isResponseTo(requestMessage);
                    response.setData(lodData);

                    this.socketMessageHandler.sendSocketMessage(requestMessage.getSender(), response);
                });
        });

        //this.socketMessageHandler.getEventBus().registerHandler(GenerationTaskPriorityRequest.class, (requestMessage) -> { });
    }
}
