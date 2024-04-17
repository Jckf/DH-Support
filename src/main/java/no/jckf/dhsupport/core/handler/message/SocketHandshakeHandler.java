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
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.DhsConfig;
import no.jckf.dhsupport.core.handler.SocketMessageHandler;
import no.jckf.dhsupport.core.message.socket.*;

import java.util.UUID;

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
        this.socketMessageHandler.getEventBus().registerHandler(HelloSocketMessage.class, (helloMessage) -> {
            if (helloMessage.getVersion() != this.socketMessageHandler.protocolVersion) {
                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("Incompatible Distant Horizons version. Server speaks protocol version " + this.socketMessageHandler.protocolVersion + ", but client sent protocol version " + helloMessage.getVersion());
                this.socketMessageHandler.sendSocketMessage(helloMessage.getSender(), response);
                return;
            }

            HelloSocketMessage response = new HelloSocketMessage();
            response.setVersion(this.socketMessageHandler.protocolVersion);
            this.socketMessageHandler.sendSocketMessage(helloMessage.getSender(), response);
        });

        this.socketMessageHandler.getEventBus().registerHandler(PlayerUuidSocketMessage.class, (uuidMessage) -> {
            UUID uuid = uuidMessage.getUuid();

            if (this.dhSupport.getSocketIdByPlayerUuid(uuid) != null) {
                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("UUID hijack attempt.");
                this.socketMessageHandler.sendSocketMessage(uuidMessage.getSender(), response);
                return;
            }

            this.dhSupport.associatePlayerAndSocket(uuidMessage.getUuid(), uuidMessage.getSender().id());

            AckSocketMessage response = new AckSocketMessage();
            response.isResponseTo(uuidMessage);
            this.socketMessageHandler.sendSocketMessage(uuidMessage.getSender(), response);
        });

        this.socketMessageHandler.getEventBus().registerHandler(PlayerConfigSocketMessage.class, (configMessage) -> {
            Configuration dhsConfig = this.dhSupport.getConfig();
            Configuration clientConfig = configMessage.toConfiguration();

            // This is not very flexible, but will do for now.
            for (String key : DhsConfig.getKeys()) {
                this.dhSupport.getLogger().info("Config key " + key + ":");

                Object dhsValue = dhsConfig.get(key);
                Object clientValue = clientConfig.get(key);
                Object keepValue;

                if (dhsValue instanceof Boolean dhsBool && clientValue instanceof Boolean clientBool) {
                    keepValue = dhsBool && clientBool;

                    this.dhSupport.getLogger().info("    Server " + (dhsBool ? "Y" : "N") + " or client " + (clientBool ? "Y" : "N") + " = " + ((boolean) keepValue ? "Y" : "N"));
                } else if (dhsValue instanceof Integer dhsInt && clientValue instanceof Integer clientInt) {
                    keepValue = dhsInt < clientInt ? dhsInt : clientInt;

                    this.dhSupport.getLogger().info("    Server " + dhsInt + " or client " + clientInt + " = " + keepValue);
                } else {
                    keepValue = null;

                    this.dhSupport.getLogger().info("    Uhh... 😵‍💫");
                }

                clientConfig.set(key, keepValue);
            }

            // TODO: Store the resulting config in some sort of context object for this player.

            this.socketMessageHandler.sendSocketMessage(configMessage.getSender(), configMessage);
        });
    }
}
