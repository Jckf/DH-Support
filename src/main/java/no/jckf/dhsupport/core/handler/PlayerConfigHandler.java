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
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.DhsConfig;
import no.jckf.dhsupport.core.message.plugin.CurrentLevelKeyMessage;
import no.jckf.dhsupport.core.message.plugin.RemotePlayerConfigMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerConfigHandler
{
    protected DhSupport dhSupport;

    protected PluginMessageHandler pluginMessageHandler;

    public PlayerConfigHandler(DhSupport dhSupport, PluginMessageHandler pluginMessageHandler)
    {
        this.dhSupport = dhSupport;
        this.pluginMessageHandler = pluginMessageHandler;
    }

    public void register()
    {
        this.pluginMessageHandler.getEventBus().registerHandler(RemotePlayerConfigMessage.class, (configMessage) -> {
            // TODO: Some sort of Player wrapper or interface object. Bukkit classes should not be imported here.
            Player player = Bukkit.getPlayer(configMessage.getSender());

            Configuration dhsConfig = this.dhSupport.getWorldInterface(player.getWorld().getUID()).getConfig();

            String levelKeyPrefix = dhsConfig.getString(DhsConfig.LEVEL_KEY_PREFIX);
            String levelKey = player.getWorld().getName();

            if (levelKeyPrefix != null) {
                levelKey = levelKeyPrefix + levelKey;
            }

            this.dhSupport.info("Received DH config for " + player.getName() + " in " + levelKey);

            CurrentLevelKeyMessage levelKeyResponse = new CurrentLevelKeyMessage();
            levelKeyResponse.setKey(levelKey);
            this.pluginMessageHandler.sendPluginMessage(configMessage.getSender(), levelKeyResponse);

            Configuration clientConfig = configMessage.toConfiguration();

            // This is not very flexible, but will do for now.
            for (String key : RemotePlayerConfigMessage.KEYS) {
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

            RemotePlayerConfigMessage configResponse = new RemotePlayerConfigMessage();
            configResponse.fromConfiguration(clientConfig);

            this.pluginMessageHandler.sendPluginMessage(configMessage.getSender(), configResponse);
        });
    }
}
