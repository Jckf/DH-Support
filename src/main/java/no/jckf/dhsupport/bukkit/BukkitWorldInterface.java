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

import no.jckf.dhsupport.core.world.WorldInterface;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class BukkitWorldInterface implements WorldInterface
{
    World world;

    public BukkitWorldInterface(World world)
    {
        this.world = world;
    }

    @Override
    public int getMinY()
    {
        return this.world.getMinHeight();
    }

    @Override
    public int getMaxY()
    {
        return this.world.getMaxHeight();
    }

    @Override
    public int getHighestYAt(int x, int z)
    {
        return this.world.getHighestBlockAt(x, z).getY();
    }

    @Override
    public String getBiomeAt(int x, int z)
    {
        return this.world.getBlockAt(x, this.world.getSeaLevel(), z).getBiome().getKey().toString();
    }

    @Override
    public String getMaterialAt(int x, int y, int z)
    {
        return this.world.getBlockAt(x, y, z).getBlockData().getMaterial().getKey().toString();
    }

    @Override
    public String getBlockStateAsStringAt(int x, int y, int z)
    {
        return this.world.getBlockAt(x, y, z).getBlockData().getAsString();
    }

    @Override
    public Map<String, String> getBlockPropertiesAt(int x, int y, int z)
    {
        Map<String, String> properties = new HashMap<>();

        String dataString = this.getBlockStateAsStringAt(x, y, z);

        int kvStart = dataString.indexOf("[");

        if (kvStart == -1) {
            return properties;
        }

        String[] kvStrings = dataString.substring(kvStart + 1, dataString.length() - 1).split(",");

        for (String kvString : kvStrings) {
            String[] kv = kvString.split("=", 2);

            properties.put(kv[0], kv[1]);
        }

        return properties;
    }

    @Override
    public byte getBlockLightAt(int x, int y, int z)
    {
        return this.world.getBlockAt(x, y, z).getLightFromBlocks();
    }

    @Override
    public byte getSkyLightAt(int x, int y, int z)
    {
        return this.world.getBlockAt(x, y, z).getLightFromSky();
    }

    @Override
    public boolean isTransparent(int x, int y, int z)
    {
        return this.world.getBlockAt(x, y, z).getBlockData().getMaterial().isTransparent();
    }
}
