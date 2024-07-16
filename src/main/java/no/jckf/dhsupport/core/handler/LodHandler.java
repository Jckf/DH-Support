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
import no.jckf.dhsupport.core.message.plugin.FullDataSourceRequestMessage;
import no.jckf.dhsupport.core.message.plugin.FullDataSourceResponseMessage;
import org.bukkit.Bukkit;

import java.util.UUID;

public class LodHandler
{
    protected DhSupport dhSupport;

    protected PluginMessageHandler pluginMessageHandler;

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

            this.dhSupport.getLodData(worldUuid, requestMessage.getPosition())
                .thenAccept((lod) -> {
                    FullDataSourceResponseMessage response = new FullDataSourceResponseMessage();
                    response.isResponseTo(requestMessage);
                    response.setData(lod);

                    this.pluginMessageHandler.sendPluginMessage(requestMessage.getSender(), response);

                    //this.dhSupport.info("LOD sent for " + requestMessage.getPosition().getX() + " x " + requestMessage.getPosition().getZ());
                })
                .exceptionally((exception) -> {
                    this.dhSupport.warning(exception.getMessage());
                    return null;
                });
        });
    }
}
