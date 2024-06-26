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
import no.jckf.dhsupport.core.handler.PluginMessageHandler;
import no.jckf.dhsupport.core.message.plugin.HelloPluginMessage;
import no.jckf.dhsupport.core.message.plugin.ServerConnectInfoMessage;

public class PluginHandshakeHandler
{
    protected DhSupport dhSupport;

    protected PluginMessageHandler pluginMessageHandler;

    public PluginHandshakeHandler(DhSupport dhSupport, PluginMessageHandler pluginMessageHandler)
    {
        this.dhSupport = dhSupport;
        this.pluginMessageHandler = pluginMessageHandler;
    }

    public void register()
    {
        this.pluginMessageHandler.getEventBus().registerHandler(HelloPluginMessage.class, (helloMessage) -> {
            Configuration config = this.dhSupport.getConfig();

            ServerConnectInfoMessage response = new ServerConnectInfoMessage();
            response.setAddress(config.getString(DhsConfig.HOST));
            response.setPort(config.getInt(DhsConfig.PORT));

            this.pluginMessageHandler.sendPluginMessage(helloMessage.getSender(), response);
        });
    }
}
