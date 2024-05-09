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

import no.jckf.dhsupport.bukkit.BukkitWorldInterface;
import no.jckf.dhsupport.bukkit.DhSupportBukkitPlugin;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldHandler implements Listener
{
    protected DhSupportBukkitPlugin plugin;

    public WorldHandler(DhSupportBukkitPlugin plugin)
    {
        this.plugin = plugin;

        this.plugin.getServer().getWorlds().forEach(this::addWorldInterface);
    }

    protected void addWorldInterface(World world)
    {
        this.plugin.getDhSupport().setWorldInterface(world.getUID(), new BukkitWorldInterface(world));
    }

    protected void removeWorldInterface(World world)
    {
        this.plugin.getDhSupport().setWorldInterface(world.getUID(), null);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent worldLoad)
    {
        this.addWorldInterface(worldLoad.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent worldUnload)
    {
        this.removeWorldInterface(worldUnload.getWorld());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent chunkLoad)
    {

    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent chunkUnload)
    {

    }
}
