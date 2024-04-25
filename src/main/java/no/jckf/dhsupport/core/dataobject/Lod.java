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

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.xxhash.XXHashFactory;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.world.WorldInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class Lod extends DataObject
{
    public static int width = 64;

    protected static int separator = 0xFFFFFFFF;

    protected WorldInterface worldInterface;

    protected List<IdMapping> idMappings;

    protected List<List<DataPoint>> columns;

    public Lod(WorldInterface worldInterface, List<IdMapping> idMappings, List<List<DataPoint>> columns)
    {
        this.worldInterface = worldInterface;
        this.idMappings = idMappings;
        this.columns = columns;
    }

    @Override
    public void encode(Encoder encoder)
    {
        Encoder toCompress = new Encoder();

        toCompress.writeInt(0); // Detail level
        toCompress.writeInt(Lod.width);
        toCompress.writeInt(this.worldInterface.getMinY());
        toCompress.writeByte(1); // World gen step

        toCompress.writeInt(Lod.separator);

        this.columns.forEach((column) -> toCompress.writeInt(column.size()));

        toCompress.writeInt(Lod.separator);

        for (List<DataPoint> column : this.columns) {
            for (DataPoint dataPoint : column) {
                dataPoint.encode(toCompress);
            }
        }

        toCompress.writeInt(Lod.separator);

        toCompress.writeInt(this.idMappings.size());

        for (IdMapping mapping : this.idMappings) {
            mapping.encode(toCompress);
        }

        encoder.write(this.compress(toCompress.toByteArray()));
    }

    protected byte[] compress(byte[] uncompressedData)
    {
        ByteArrayOutputStream compressedStream = new ByteArrayOutputStream();

        try {
            LZ4FrameOutputStream compressorStream = new LZ4FrameOutputStream(
                new BufferedOutputStream(compressedStream),
                LZ4FrameOutputStream.BLOCKSIZE.SIZE_4MB,
                uncompressedData.length,
                LZ4Factory.fastestInstance().highCompressor(17),
                XXHashFactory.fastestInstance().hash32(),
                LZ4FrameOutputStream.FLG.Bits.BLOCK_INDEPENDENCE
            );

            compressorStream.write(uncompressedData);
            compressorStream.flush();
        } catch (Exception exception) {
            // Uhh...
            System.out.println(exception.getClass().getSimpleName() + " - " + exception.getMessage());
        }

        return compressedStream.toByteArray();
    }
}
