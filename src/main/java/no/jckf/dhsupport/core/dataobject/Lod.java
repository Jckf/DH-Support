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

import java.util.ArrayList;
import java.util.List;

public class Lod extends DataObject
{
    protected static int width = 64;

    protected static int height = 63;

    protected static int separator = 0xFFFFFFFF;

    @Override
    public void encode(Encoder encoder)
    {
        List<IdMapping> idMappings = new ArrayList<>();
        idMappings.add(0, new IdMapping("minecraft:plains", "minecraft:water"));

        DataPoint stoneBlock = new DataPoint();
        stoneBlock.setMappingId(0);

        encoder.writeInt(Lod.separator);

        for (int xy = 0; xy < Lod.width * Lod.width; xy++) {
            encoder.writeInt(Lod.height);
        }

        encoder.writeInt(Lod.separator);

        for (int xy = 0; xy < Lod.width * Lod.width; xy++) {
            for (int z = 0; z < Lod.height; z++) {
                stoneBlock.setStartZ(z);
                stoneBlock.encode(encoder);
            }
        }

        encoder.writeInt(Lod.separator);

        encoder.writeInt(idMappings.size());
        idMappings.forEach((mapping) -> mapping.encode(encoder));
    }
}
