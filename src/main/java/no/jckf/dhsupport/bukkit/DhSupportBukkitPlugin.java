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

package no.jckf.dhsupport.bukkit;

import no.jckf.dhsupport.bukkit.handler.ConfigLoader;
import no.jckf.dhsupport.bukkit.handler.Handler;
import no.jckf.dhsupport.bukkit.handler.PluginMessageProxy;
import no.jckf.dhsupport.bukkit.handler.WorldHandler;
import no.jckf.dhsupport.core.DhSupport;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class DhSupportBukkitPlugin extends JavaPlugin
{
    protected DhSupport dhSupport;

    protected Metrics metrics;

    protected final Map<Class<? extends Handler>, Handler> handlers = new HashMap<>()
    {{
        this.put(ConfigLoader.class, null);
        this.put(PluginMessageProxy.class, null);
    }};

    @Override
    public void onEnable()
    {
        this.dhSupport = new DhSupport();
        this.dhSupport.setLogger(this.getLogger());

        this.metrics = new Metrics(this, 21843);

        for (Class<? extends Handler> className : this.handlers.keySet()) {
            try {
                Handler instance = className
                    .getConstructor(DhSupportBukkitPlugin.class)
                    .newInstance(this);

                instance.onEnable();

                this.handlers.replace(className, instance);
            } catch (Exception exception) {
                this.getLogger().warning("Failed to enable handler: " + className);
                this.getLogger().warning(exception.getClass().getSimpleName() + " - " + exception.getMessage());
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        this.dhSupport.onEnable();

        this.getServer().getPluginManager().registerEvents(new WorldHandler(this), this);

        this.getLogger().info("Ready 😀");
    }

    @Override
    public void onDisable()
    {
        if (this.dhSupport != null) {
            this.dhSupport.onDisable();
            this.dhSupport = null;
        }

        for (Class<? extends Handler> className : this.handlers.keySet()) {
            Handler instance = this.handlers.get(className);

            try {
                if (instance != null) {
                    instance.onDisable();
                }
            } catch (Exception exception) {
                this.getLogger().warning("Failed to disable handler: " + className);
                this.getLogger().warning(exception.getClass().getSimpleName() + " - " + exception.getMessage());
            }

            this.handlers.replace(className, null);
        }

        this.getLogger().info("👋😭");
    }

    @Nullable
    public DhSupport getDhSupport()
    {
        return this.dhSupport;
    }
}
