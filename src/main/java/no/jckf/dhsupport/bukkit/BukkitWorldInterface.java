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

import no.jckf.dhsupport.core.Coordinates;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.WorldConfiguration;
import no.jckf.dhsupport.core.dataobject.Beacon;
import no.jckf.dhsupport.core.world.WorldInterface;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;

import java.awt.*;
import java.io.File;
import java.util.*;

public class BukkitWorldInterface implements WorldInterface
{
    protected World world;

    protected Configuration config;

    protected WorldConfiguration worldConfig;

    protected Map<String, ChunkSnapshot> chunks = new HashMap<>();

    public BukkitWorldInterface(World world, Configuration config)
    {
        this.world = world;
        this.config = config;
        this.worldConfig = new WorldConfiguration(this, config);
    }

    @Override
    public WorldInterface newInstance()
    {
        return new BukkitWorldInterface(this.world, this.config);
    }

    protected ChunkSnapshot getChunk(int x, int z)
    {
        int chunkX = Coordinates.blockToChunk(x);
        int chunkZ = Coordinates.blockToChunk(z);

        String key = chunkX + "x" + chunkZ;

        if (this.chunks.containsKey(key)) {
            return this.chunks.get(key);
        }

        ChunkSnapshot chunk = this.world.getChunkAt(chunkX, chunkZ).getChunkSnapshot(true, true, false);

        this.chunks.put(key, chunk);

        return chunk;
    }

    @Override
    public UUID getId()
    {
        return this.world.getUID();
    }

    @Override
    public String getName()
    {
        return this.world.getName();
    }

    @Override
    public boolean chunkExists(int x, int z)
    {
        int chunkX = Coordinates.blockToChunk(x);
        int chunkZ = Coordinates.blockToChunk(z);

        boolean alreadyLoaded = this.world.isChunkLoaded(chunkX, chunkZ);

        if (alreadyLoaded) {
            return true;
        }

        int regionX = Coordinates.chunkToRegion(chunkX);
        int regionZ = Coordinates.chunkToRegion(chunkZ);

        File regionFile = new File(world.getWorldFolder() + "/region/r." + regionX + "." + regionZ + ".mca");

        if (!regionFile.exists()) {
            return false;
        }

        boolean exists = this.world.loadChunk(chunkX, chunkZ, false);

        if (exists) {
            this.world.unloadChunk(chunkX, chunkZ, false);
        }

        return exists;
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
    public int getSeaLevel()
    {
        return this.world.getSeaLevel();
    }

    @Override
    public int getHighestYAt(int x, int z)
    {
        return this.getChunk(x, z).getHighestBlockYAt(Coordinates.blockToChunkRelative(x), Coordinates.blockToChunkRelative(z));
    }

    @Override
    public String getBiomeAt(int x, int z)
    {
        return this.getChunk(x, z).getBiome(Coordinates.blockToChunkRelative(x), this.getSeaLevel(), Coordinates.blockToChunkRelative(z)).getKey().toString();
    }

    @Override
    public String getMaterialAt(int x, int y, int z)
    {
        return this.getChunk(x, z).getBlockType(Coordinates.blockToChunkRelative(x), y, Coordinates.blockToChunkRelative(z)).getKey().toString();
    }

    @Override
    public String getBlockStateAsStringAt(int x, int y, int z)
    {
        return this.getChunk(x, z).getBlockData(Coordinates.blockToChunkRelative(x), y, Coordinates.blockToChunkRelative(z)).getAsString();
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
        return (byte) this.getChunk(x, z).getBlockEmittedLight(Coordinates.blockToChunkRelative(x), y, Coordinates.blockToChunkRelative(z));
    }

    @Override
    public byte getSkyLightAt(int x, int y, int z)
    {
        return (byte) this.getChunk(x, z).getBlockSkyLight(Coordinates.blockToChunkRelative(x), y, Coordinates.blockToChunkRelative(z));
    }

    @Override
    public Collection<Beacon> getBeaconsInChunk(int x, int z)
    {
        Collection<Beacon> beacons = new ArrayList<>();

        BlockState[] blocks = this.world.getChunkAt(Coordinates.blockToChunk(x), Coordinates.blockToChunk(z)).getTileEntities();

        for (BlockState block : blocks) {
            if (!block.getType().equals(Material.BEACON)) {
                continue;
            }

            beacons.add(
                new Beacon(
                    block.getX(),
                    block.getY(),
                    block.getZ(),
                    Color.WHITE.getRGB() // TODO?
                )
            );
        }

        return beacons;
    }

    @Override
    public Configuration getConfig()
    {
        return this.worldConfig;
    }
}
