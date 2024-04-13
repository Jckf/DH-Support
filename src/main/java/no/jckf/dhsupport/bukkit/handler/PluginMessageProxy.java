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

package no.jckf.dhsupport.bukkit.handler;

import no.jckf.dhsupport.bukkit.DhSupportBukkitPlugin;
import no.jckf.dhsupport.core.handler.PluginMessageHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class PluginMessageProxy extends Handler implements PluginMessageListener
{
    protected PluginMessageHandler pluginMessageHandler;

    public PluginMessageProxy(DhSupportBukkitPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public void onEnable()
    {
        this.pluginMessageHandler = this.plugin.getDhSupport().getPluginMessageHandler();

        this.plugin.getDhSupport().setPluginMessageSender(new BukkitPluginMessageSender(this.plugin));

        this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, this.pluginMessageHandler.pluginChannel, this);
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, this.pluginMessageHandler.pluginChannel);
    }

    @Override
    public void onDisable()
    {
        this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin);
        this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message)
    {
        this.pluginMessageHandler.onPluginMessageReceived(channel, player.getUniqueId(), message);
    }
}
