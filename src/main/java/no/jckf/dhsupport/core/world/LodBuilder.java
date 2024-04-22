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

package no.jckf.dhsupport.core.world;

import net.jpountz.lz4.LZ4FrameOutputStream;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;

public class LodBuilder
{
    protected WorldInterface worldInterface;

    protected SectionPosition position;

    public LodBuilder(WorldInterface worldInterface, SectionPosition position)
    {
        this.worldInterface = worldInterface;
        this.position = position;
    }

    public byte[] generate()
    {
        System.out.println("Building LOD...");

        ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();

        try {
            LZ4FrameOutputStream compressorStream = new LZ4FrameOutputStream(new BufferedOutputStream(compressedStream));

            Encoder dataSourceEncoder = new Encoder();

            dataSourceEncoder.writeInt(0); // Detail level
            dataSourceEncoder.writeInt(64); // Width
            dataSourceEncoder.writeInt(this.worldInterface.getMinY());
            dataSourceEncoder.writeByte(1); // World gen step

            Lod lod = new Lod(this.worldInterface, this.position);
            lod.encode(dataSourceEncoder);

            byte[] uncompressedData = dataSourceEncoder.toByteArray();

            compressorStream.write(uncompressedData);
            compressorStream.flush();
        } catch (Exception exception) {
            // Uhh...
            System.out.println(exception.getClass().getSimpleName() + " - " + exception.getMessage());
        }

        return compressedStream.toByteArray();
    }
}
