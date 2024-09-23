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

import com.tcoded.folialib.FoliaLib;
import no.jckf.dhsupport.bukkit.handler.ConfigLoader;
import no.jckf.dhsupport.bukkit.handler.PluginMessageProxy;
import no.jckf.dhsupport.bukkit.handler.WorldHandler;
import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.configuration.DhsConfig;
import no.jckf.dhsupport.core.scheduling.GenericScheduler;
import no.jckf.dhsupport.paper.PaperScheduler;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public class DhSupportBukkitPlugin extends JavaPlugin
{
    protected DhSupport dhSupport;

    protected Metrics metrics;

    protected FoliaLib foliaLib;

    protected ConfigLoader configLoader;

    protected PluginMessageProxy pluginMessageProxy;

    @Override
    public void onEnable()
    {
        this.dhSupport = new DhSupport();
        this.dhSupport.setLogger(this.getLogger());
        this.dhSupport.setDataDirectory(this.getDataFolder().getAbsolutePath());

        this.foliaLib = new FoliaLib(this);

        this.metrics = new Metrics(this, 21843);

        this.configLoader = new ConfigLoader(this);
        this.configLoader.onEnable();

        this.pluginMessageProxy = new PluginMessageProxy(this);
        this.pluginMessageProxy.onEnable();

        this.dhSupport.onEnable();

        if (this.foliaLib.isFolia()) {
            this.getLogger().info("Using Paper scheduler.");

            this.dhSupport.setScheduler(new PaperScheduler(this, this.foliaLib));
        } else {
            this.getLogger().info("Using generic scheduler.");

            this.dhSupport.setScheduler(new GenericScheduler(this.getDhSupport().getConfig().getInt(DhsConfig.GENERIC_SCHEDULER_THREADS)));
        }

        this.getServer().getPluginManager().registerEvents(new WorldHandler(this), this);

        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            int inserted = this.dhSupport.getLodRepository().processQueuedSaves();

            if (inserted != 0) {
                this.getLogger().info("Executed " + inserted + " queued inserts.");
            }
        }, 0, 10 * 20);

        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            int updated = this.dhSupport.updateTouchedLods();

            if (updated != 0) {
                this.getLogger().info("Updated " + updated + " changed LODs.");
            }
        }, 0, 60 * 20);

        this.getLogger().info("Ready!");
    }

    @Override
    public void onDisable()
    {
        if (this.pluginMessageProxy != null) {
            this.pluginMessageProxy.onDisable();
            this.pluginMessageProxy = null;
        }

        if (this.configLoader != null) {
            this.configLoader.onDisable();
            this.configLoader = null;
        }

        if (this.dhSupport != null) {
            this.dhSupport.onDisable();
            this.dhSupport = null;
        }

        this.getLogger().info("Lights out!");
    }

    @Nullable
    public DhSupport getDhSupport()
    {
        return this.dhSupport;
    }
}
