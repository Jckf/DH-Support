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

package no.jckf.dhsupport;

import no.jckf.dhsupport.Handlers.PluginMessageHandler;
import no.jckf.dhsupport.Handlers.SocketMessageHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class DhSupport extends JavaPlugin
{
    public PluginMessageHandler pluginMessageHandler;

    public SocketMessageHandler socketMessageHandler;

    @Override
    public void onEnable()
    {
        // Create config file if none is present.
        this.saveDefaultConfig();

        // Load missing values from default file.
        this.getConfig().options().copyDefaults(true);

        try {
            this.pluginMessageHandler = new PluginMessageHandler(this);
            this.pluginMessageHandler.onEnable();

            this.socketMessageHandler = new SocketMessageHandler(this);
            this.socketMessageHandler.onEnable();
        } catch (Exception exception) {
            this.warning("Failed to enable DH Support: " + exception.getClass() + " - " + exception.getMessage());

            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        try {
            if (this.socketMessageHandler != null) {
                this.socketMessageHandler.onDisable();
            }

            if (this.pluginMessageHandler != null) {
                this.pluginMessageHandler.onDisable();
            }
        } catch (Exception exception) {
            this.warning("Failed to gracefully disable DH Support: " + exception.getClass() + " - " + exception.getMessage());
        }
    }

    public void info(String message)
    {
        this.getLogger().info(message);
    }

    public void warning(String message)
    {
        this.getLogger().warning(message);
    }
}
