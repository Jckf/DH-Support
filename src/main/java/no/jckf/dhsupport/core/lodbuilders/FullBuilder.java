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

package no.jckf.dhsupport.core.lodbuilders;

import no.jckf.dhsupport.core.Coordinates;
import no.jckf.dhsupport.core.configuration.DhsConfig;
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

public class FullBuilder extends LodBuilder
{
    public FullBuilder(WorldInterface worldInterface, SectionPosition position)
    {
        super(worldInterface, position);
    }

    public Lod generate()
    {
        int minY = this.worldInterface.getMinY();
        int maxY = this.worldInterface.getMaxY();
        int height = maxY - minY;

        int offsetX = Coordinates.sectionToBlock(this.position.getX());
        int offsetZ = Coordinates.sectionToBlock(this.position.getZ());

        int yStep = this.worldInterface.getConfig().getInt(DhsConfig.BUILDER_RESOLUTION);

        List<IdMapping> idMappings = new ArrayList<>();
        Map<String, Integer> mapMap = new HashMap<>();

        List<List<DataPoint>> columns = new ArrayList<>();

        for (int relativeX = 0; relativeX < Lod.width; relativeX++) {
            for (int relativeZ = 0; relativeZ < Lod.width; relativeZ++) {
                int worldX = offsetX + relativeX;
                int worldZ = offsetZ + relativeZ;

                // Actual Y of top-most block.
                int topLayer = this.worldInterface.getHighestYAt(worldX, worldZ);

                // Distance from bottom to top-most block.
                int relativeTopLayer = topLayer - minY;

                String biome = this.worldInterface.getBiomeAt(worldX, worldZ);

                List<DataPoint> column = new ArrayList<>();

                @Nullable
                DataPoint previous = null;

                int firstY = height - yStep;

                for (int relativeY = firstY; relativeY >= 1 - yStep; relativeY -= yStep) {
                    int thisStep = yStep;

                    if (relativeY < 0) {
                        thisStep -= -relativeY;
                        relativeY = 0;
                    }

                    int worldY = minY + relativeY + thisStep - 1;

                    String material = this.worldInterface.getMaterialAt(worldX, worldY, worldZ);

                    String compositeKey = biome + "|" + material + "|" + this.worldInterface.getBlockStateAsStringAt(worldX, worldY, worldZ);

                    @Nullable
                    Integer id = mapMap.get(compositeKey);

                    if (id == null) {
                        idMappings.add(new IdMapping(biome, material, this.worldInterface.getBlockPropertiesAt(worldX, worldY, worldZ)));
                        id = idMappings.size() - 1;
                        mapMap.put(compositeKey, id);
                    }

                    DataPoint point;

                    if (previous != null && previous.getMappingId() == id) {
                        point = previous;

                        point.setStartY(point.getStartY() - thisStep);
                        point.setHeight(point.getHeight() + thisStep);
                    } else {
                        point = new DataPoint();
                        column.add(point);

                        point.setStartY(relativeY);
                        point.setHeight(thisStep);
                        point.setMappingId(id);

                        if (worldY + 1 < maxY) {
                            point.setSkyLight(this.worldInterface.getSkyLightAt(worldX, worldY + 1, worldZ));
                            point.setBlockLight(this.worldInterface.getBlockLightAt(worldX, worldY + 1, worldZ));
                        }

                        // Start by filling the top of the column with air, then jump down to the top layer.
                        if (relativeY == firstY && (material.equals("minecraft:air") || material.equals("minecraft:void_air"))) {
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
