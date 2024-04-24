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

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FrameOutputStream;
import net.jpountz.xxhash.XXHashFactory;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class LodBuilder
{
    protected static Map<String, byte[]> cache = new HashMap<>();

    protected WorldInterface worldInterface;

    protected SectionPosition position;

    public LodBuilder(WorldInterface worldInterface, SectionPosition position)
    {
        this.worldInterface = worldInterface;
        this.position = position;
    }

    public byte[] generate()
    {
        String cacheKey = this.position.getX() + "x" + this.position.getZ();

        if (LodBuilder.cache.containsKey(cacheKey)) {
            return LodBuilder.cache.get(cacheKey);
        }

        //System.out.println("Building LOD...");

        Encoder dataSourceEncoder = new Encoder();

        dataSourceEncoder.writeInt(0); // Detail level
        dataSourceEncoder.writeInt(64); // Width
        dataSourceEncoder.writeInt(this.worldInterface.getMinY());
        dataSourceEncoder.writeByte(1); // World gen step

        Lod lod = new Lod(this.worldInterface, this.position);
        lod.encode(dataSourceEncoder);
        byte[] uncompressedData = dataSourceEncoder.toByteArray();

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

        byte[] result = compressedStream.toByteArray();

        //System.out.println("LOD size: " + Math.ceil(result.length / 1024.0));

        LodBuilder.cache.put(cacheKey, result);

        return result;
    }
}
