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
import no.jckf.dhsupport.core.message.socket.*;

public class SocketHandshakeHandler
{
    protected DhSupport dhSupport;

    protected SocketMessageHandler socketMessageHandler;

    public SocketHandshakeHandler(DhSupport dhSupport, SocketMessageHandler socketMessageHandler)
    {
        this.dhSupport = dhSupport;
        this.socketMessageHandler = socketMessageHandler;
    }

    public void register()
    {
        this.socketMessageHandler.getEventBus().registerHandler(HelloSocketMessage.class, (hello) -> {
            if (hello.getVersion() != this.socketMessageHandler.protocolVersion) {
                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("Incompatible Distant Horizons version. Server speaks protocol version " + this.socketMessageHandler.protocolVersion + ", but client sent protocol version " + hello.getVersion());
                this.socketMessageHandler.sendSocketMessage(hello.getSender(), response);
                return;
            }

            HelloSocketMessage response = new HelloSocketMessage();
            response.setVersion(this.socketMessageHandler.protocolVersion);
            this.socketMessageHandler.sendSocketMessage(hello.getSender(), response);
        });

        this.socketMessageHandler.getEventBus().registerHandler(PlayerUuidSocketMessage.class, (uuid) -> {
            AckSocketMessage response = new AckSocketMessage();
            response.isResponseTo(uuid);
            this.socketMessageHandler.sendSocketMessage(uuid.getSender(), response);
        });

        this.socketMessageHandler.getEventBus().registerHandler(PlayerConfigSocketMessage.class, (config) -> {
            // TODO: Clamp values according to server config.
            this.socketMessageHandler.sendSocketMessage(config.getSender(), config);
        });
    }
}
