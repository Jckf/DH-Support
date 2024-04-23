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

package no.jckf.dhsupport.core.dataobject;

import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.world.WorldInterface;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lod extends DataObject
{
    protected static int width = 64;

    protected static int separator = 0xFFFFFFFF;

    protected WorldInterface worldInterface;

    protected SectionPosition position;

    public Lod(WorldInterface worldInterface, SectionPosition position)
    {
        this.worldInterface = worldInterface;
        this.position = position;
    }

    @Override
    public void encode(Encoder encoder)
    {
        int minY = this.worldInterface.getMinY();
        int maxY = this.worldInterface.getMaxY();
        int height = maxY - minY;

        int seaLevel = this.worldInterface.getSeaLevel();
        int relativeSeaLevel = seaLevel - minY;

        int offsetX = this.position.getX() * 64;
        int offsetZ = this.position.getZ() * 64;

        encoder.writeInt(Lod.separator);

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

                if (relativeX % 16 == 0 || relativeZ % 16 == 0) {
                    biome = this.worldInterface.getBiomeAt(offsetX + relativeX, offsetZ + relativeZ);
                }

                List<DataPoint> column = new ArrayList<>();

                @Nullable
                DataPoint previous = null;
                Integer solidGround = null;

                for (int relativeY = relativeTopLayer; (solidGround == null || relativeY >= solidGround) && relativeY >= 0; relativeY--) {
                    int worldY = minY + relativeY;

                    String material = this.worldInterface.getMaterialAt(worldX, worldY, worldZ);

                    if (material.equals("minecraft:air") || material.equals("minecraft:void_air")) {
                        previous = null;
                        continue;
                    }

                    if (solidGround == null && !material.equals("minecraft:water")) {
                        solidGround = relativeY - 25; // Stop 25 blocks under the first non-air/water block.
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
                    }

                    point.setSkyLight(this.worldInterface.getSkyLightAt(worldX, worldY + 1, worldZ));
                    point.setBlockLight(this.worldInterface.getBlockLightAt(worldX, worldY, worldZ));

                    previous = point;
                }

                columns.add(column);

                encoder.writeInt(column.size());
            }
        }

        encoder.writeInt(Lod.separator);

        for (List<DataPoint> column : columns) {
            for (DataPoint dataPoint : column) {
                dataPoint.encode(encoder);
            }
        }

        encoder.writeInt(Lod.separator);

        encoder.writeInt(idMappings.size());

        for (IdMapping mapping : idMappings) {
            mapping.encode(encoder);
        }
    }
}
