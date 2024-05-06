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
import no.jckf.dhsupport.core.enums.CompressionType;
import no.jckf.dhsupport.core.enums.GenerationStep;
import no.jckf.dhsupport.core.world.WorldInterface;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @see FullDataSourceV2DTO
 */
public class Lod extends DataObject
{
    public static int dataFormatVersion = 1;

    public static CompressionType compressionType = CompressionType.LZ4;

    public static int width = 64;

    protected WorldInterface worldInterface;

    protected SectionPosition position;

    protected List<IdMapping> idMappings;

    protected List<List<DataPoint>> columns;

    public Lod(WorldInterface worldInterface, SectionPosition position, List<IdMapping> idMappings, List<List<DataPoint>> columns)
    {
        this.worldInterface = worldInterface;
        this.position = position;
        this.idMappings = idMappings;
        this.columns = columns;
    }

    protected void encodeData(Encoder encoder)
    {
        Encoder toCompress = new Encoder();

        for (List<DataPoint> column : this.columns) {
            toCompress.writeShort(column.size());

            for (DataPoint dataPoint : column) {
                dataPoint.encode(toCompress);
            }
        }

        byte[] compressed = this.compress(toCompress.toByteArray());

        encoder.writeInt(compressed.length);
        encoder.write(compressed);
    }

    protected void encodeColumnGenerationStep(Encoder encoder)
    {
        byte[] bytesToCompress = new byte[Lod.width * Lod.width];
        Arrays.fill(bytesToCompress, (byte) GenerationStep.STRUCTURE_START.value);

        byte[] compressed = this.compress(bytesToCompress);

        encoder.writeInt(compressed.length);
        encoder.write(compressed);
    }

    protected void encodeWorldCompressionType(Encoder encoder)
    {
        byte[] bytesToCompress = new byte[Lod.width * Lod.width]; // All zeroes means WorldCompressionType.STRICT

        byte[] compressed = this.compress(bytesToCompress);

        encoder.writeInt(compressed.length);
        encoder.write(compressed);
    }

    protected void encodeMappings(Encoder encoder)
    {
        Encoder toCompress = new Encoder();

        toCompress.writeInt(this.idMappings.size());

        for (IdMapping mapping : this.idMappings) {
            mapping.encode(toCompress);
        }

        byte[] compressed = this.compress(toCompress.toByteArray());

        encoder.writeInt(compressed.length);
        encoder.write(compressed);
    }

    @Override
    public void encode(Encoder encoder)
    {
        this.position.encode(encoder);

        encoder.writeInt(0); // TODO: Checksum.

        this.encodeData(encoder);
        this.encodeColumnGenerationStep(encoder);
        this.encodeWorldCompressionType(encoder);
        this.encodeMappings(encoder);

        encoder.writeByte(Lod.dataFormatVersion);

        encoder.writeByte(Lod.compressionType.value);

        encoder.writeBoolean(true); // Apply to parent

        encoder.writeLong(0); // TODO: Last modified TS.
        encoder.writeLong(0); // TODO: Created TS.
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
