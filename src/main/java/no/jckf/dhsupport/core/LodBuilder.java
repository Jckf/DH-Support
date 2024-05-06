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

package no.jckf.dhsupport.core;

import no.jckf.dhsupport.core.dataobject.DataPoint;
import no.jckf.dhsupport.core.dataobject.IdMapping;
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import no.jckf.dhsupport.core.world.WorldInterface;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LodBuilder
{
    protected WorldInterface worldInterface;

    protected SectionPosition position;

    public LodBuilder(WorldInterface worldInterface, SectionPosition position)
    {
        this.worldInterface = worldInterface;
        this.position = position;
    }

    public Lod generate()
    {
        int minY = this.worldInterface.getMinY();
        int maxY = this.worldInterface.getMaxY();
        int height = maxY - minY;

        int seaLevel = this.worldInterface.getSeaLevel();
        int relativeSeaLevel = seaLevel - minY;

        int offsetX = this.position.getX() * 64;
        int offsetZ = this.position.getZ() * 64;

        List<IdMapping> idMappings = new ArrayList<>();
        Map<String, Integer> mapMap = new HashMap<>();

        List<List<DataPoint>> columns = new ArrayList<>();

        String biome = ""; // Initialize to squelch warnings.

        for (int relativeX = 0; relativeX < Lod.width; relativeX++) {
            for (int relativeZ = 0; relativeZ < Lod.width; relativeZ++) {
                int worldX = offsetX + relativeX;
                int worldZ = offsetZ + relativeZ;

                // Actual Y of top-most block.
                int topLayer = this.worldInterface.getHighestYAt(worldX, worldZ);

                // Distance from bottom to top-most block.
                int relativeTopLayer = topLayer - minY;

                biome = this.worldInterface.getBiomeAt(offsetX + relativeX, offsetZ + relativeZ);

                List<DataPoint> column = new ArrayList<>();

                @Nullable
                DataPoint previous = null;

                @Nullable
                Integer solidGround = null;

                for (int relativeY = height; (solidGround == null || relativeY >= solidGround) && relativeY >= 0; relativeY--) {
                    int worldY = minY + relativeY;

                    String material = this.worldInterface.getMaterialAt(worldX, worldY, worldZ);

                    if (solidGround == null) {
                        switch (material) {
                            case "minecraft:stone":
                            case "minecraft:grass":
                            case "minecraft:dirt":
                            case "minecraft:sand":
                            case "minecraft:sandstone":
                            case "minecraft:mycelium":
                                solidGround = Math.min(relativeY - 10, relativeSeaLevel - 10);
                        }
                    }

                    String compositeKey = biome + "|" + material;
                    //String compositeKey = biome + "|" + material + "|" + this.worldInterface.getBlockStateAsStringAt(worldX, worldY, worldZ);

                    @Nullable
                    Integer id = mapMap.get(compositeKey);

                    if (id == null) {
                        idMappings.add(new IdMapping(biome, material, null));
                        //idMappings.add(new IdMapping(biome, material, this.worldInterface.getBlockPropertiesAt(worldX, worldY, worldZ)));
                        id = idMappings.size() - 1;
                        mapMap.put(compositeKey, id);
                    }

                    DataPoint point;

                    if (previous != null && previous.getMappingId() == id) {
                        point = previous;

                        point.setStartY(point.getStartY() - 1);
                        point.setHeight(point.getHeight() + 1);
                    } else {
                        point = new DataPoint();
                        column.add(point);

                        point.setStartY(relativeY);
                        point.setMappingId(id);

                        point.setSkyLight(this.worldInterface.getSkyLightAt(worldX, worldY + 1, worldZ));
                        point.setBlockLight(this.worldInterface.getBlockLightAt(worldX, worldY + 1, worldZ));

                        if (relativeY == height && (material.equals("minecraft:air") || material.equals("minecraft:void_air"))) {
                            point.setStartY(relativeTopLayer + 1);
                            point.setHeight(height - relativeTopLayer);

                            relativeY = point.getStartY();
                        }
                    }

                    previous = point;
                }

                columns.add(column);
            }
        }

        return new Lod(this.worldInterface, this.position, idMappings, columns);
    }
}
