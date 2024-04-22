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
        int firstLayer = 60;
        int minY = this.worldInterface.getMinY();
        int maxY = this.worldInterface.getMaxY();
        int height = maxY - minY;

        int offsetX = this.position.getX() * 64;
        int offsetZ = this.position.getZ() * 64;

        encoder.writeInt(Lod.separator);

        List<IdMapping> idMappings = new ArrayList<>();
        Map<String, Integer> mapMap = new HashMap<>();

        List<List<DataPoint>> columns = new ArrayList<>();

        String biome = ""; // Initialize to squelch warnings.

        for (int x = 0; x < Lod.width; x++) {
            for (int z = 0; z < Lod.width; z++) {
                if (x % 16 == 0 || z % 16 == 0) {
                    biome = this.worldInterface.getBiomeAt(offsetX + x, offsetZ + z);
                }

                List<DataPoint> column = new ArrayList<>();

                @Nullable
                DataPoint previous = null;

                for (int y = 0; y < height; y++) {
                    int worldX = offsetX + x;
                    int worldY = firstLayer + minY + y;
                    int worldZ = offsetZ + z;

                    String material = this.worldInterface.getMaterialAt(worldX, worldY, worldZ);
                    String compositeKey = biome + "-" + material;

                    @Nullable
                    Integer id = mapMap.get(compositeKey);

                    if (id == null) {
                        idMappings.add(new IdMapping(biome, material));
                        id = idMappings.size() - 1;
                        mapMap.put(compositeKey, id);
                    }

                    DataPoint point = null;

                    if (previous != null && previous.getMappingId() == id) {
                        point = previous;

                        point.setHeight(point.getHeight() + 1);

                        if (material.equals("minecraft:air") && point.getHeight() >= 15) {
                            point.setHeight(point.getHeight() + height - y);
                            break;
                        }
                    } else {
                        point = new DataPoint();
                        column.add(point);

                        point.setStartY(firstLayer + y);
                        point.setMappingId(id);
                    }

                    point.setSkyLight(this.worldInterface.getSkyLightAt(worldX, worldY, worldZ));
                    point.setBlockLight(this.worldInterface.getBlockLightAt(worldX, worldY, worldZ));

                    if (previous != null && this.worldInterface.isTransparent(worldX, worldY, worldZ)) {
                        previous.setSkyLight(point.getSkyLight());
                    }

                    previous = point;
                }

                columns.add(column);

                encoder.writeInt(column.size());
            }
        }

        encoder.writeInt(Lod.separator);

        for (int xz = 0; xz < columns.size(); xz++) {
            for (int y = 0; y < columns.get(xz).size(); y++) {
                columns.get(xz).get(y).encode(encoder);
            }
        }

        encoder.writeInt(Lod.separator);

        encoder.writeInt(idMappings.size());
        idMappings.forEach((mapping) -> mapping.encode(encoder));
    }
}
