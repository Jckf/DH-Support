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
import no.jckf.dhsupport.core.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader extends Handler
{
    public ConfigLoader(DhSupportBukkitPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public void onEnable()
    {
        // Create config file if none is present.
        this.plugin.saveDefaultConfig();

        // Bukkit plugin config.
        FileConfiguration pluginConfig = this.plugin.getConfig();

        // DH Support config.
        Configuration dhsConfig = this.plugin.getDhSupport().getConfig();

        // Populate DH Support config.
        pluginConfig.getKeys(true).forEach((key) -> dhsConfig.set(key, pluginConfig.get(key)));
    }

    @Override
    public void onDisable()
    {

    }
}
