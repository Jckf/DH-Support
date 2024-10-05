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
import no.jckf.dhsupport.core.enums.CompressionType;
import no.jckf.dhsupport.core.enums.GenerationStep;
import no.jckf.dhsupport.core.world.WorldInterface;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZ;
import org.tukaani.xz.XZOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @see FullDataSourceV2DTO
 */
public class Lod extends DataObject
{
    public static int dataFormatVersion = 1;

    public static CompressionType compressionType = CompressionType.LZMA2;

    public static int width = 64;

    protected WorldInterface worldInterface;

    protected SectionPosition position;

    protected List<IdMapping> idMappings;

    protected List<List<DataPoint>> columns;

    protected Collection<Beacon> beacons;

    public Lod(WorldInterface worldInterface, SectionPosition position, List<IdMapping> idMappings, List<List<DataPoint>> columns)
    {
        this.worldInterface = worldInterface;
        this.position = position;
        this.idMappings = idMappings;
        this.columns = columns;
        this.beacons = beacons;
    }

    public void setBeacons(Collection<Beacon> beacons)
    {
        this.beacons = beacons;
    }

    public Collection<Beacon> getBeacons()
    {
        return beacons;
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
            OutputStream compressorStream = new XZOutputStream(compressedStream, new LZMA2Options(3), XZ.CHECK_CRC64);

            compressorStream.write(uncompressedData);
            compressorStream.flush();
        } catch (Exception exception) {
            // Uhh...
            System.out.println(exception.getClass().getSimpleName() + " - " + exception.getMessage());
        }

        return compressedStream.toByteArray();
    }
}
