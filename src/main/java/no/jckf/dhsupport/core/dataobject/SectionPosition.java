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

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;

public class SectionPosition extends DataObject
{
    public static final int DETAIL_LEVEL_WIDTH = 8;
    public static final int X_POS_WIDTH = 28;
    public static final int Z_POS_WIDTH = 28;

    public static final int DETAIL_LEVEL_OFFSET = 0;
    public static final int POS_X_OFFSET = DETAIL_LEVEL_OFFSET + DETAIL_LEVEL_WIDTH;
    public static final int POS_Z_OFFSET = POS_X_OFFSET + X_POS_WIDTH;

    public static final long DETAIL_LEVEL_MASK = Byte.MAX_VALUE;
    public static final int POS_X_MASK = (int) Math.pow(2, X_POS_WIDTH) - 1;
    public static final int POS_Z_MASK = (int) Math.pow(2, Z_POS_WIDTH) - 1;

    protected int detailLevel;

    protected int x;

    protected int z;

    public SectionPosition()
    {

    }

    public SectionPosition(int x, int z, int detailLevel)
    {
        this.x = x;
        this.z = z;
        this.detailLevel = detailLevel;
    }

    public void setDetailLevel(int level)
    {
        this.detailLevel = level;
    }

    public int getDetailLevel()
    {
        return this.detailLevel;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getX()
    {
        return this.x;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public int getZ()
    {
        return this.z;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeLong(this.toLong());
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.fromLong(decoder.readLong());
    }

    public long toLong()
    {
        long data = 0;

        data |= this.detailLevel & DETAIL_LEVEL_MASK;
        data |= (long) (this.x & POS_X_MASK) << POS_X_OFFSET;
        data |= (long) (this.z & POS_Z_MASK) << POS_Z_OFFSET;

        return data;
    }

    public void fromLong(long data)
    {
        this.detailLevel = (int) (data & DETAIL_LEVEL_MASK);
        this.x = (int) ((data >> POS_X_OFFSET) & POS_X_MASK);
        this.z = (int) ((data >> POS_Z_OFFSET) & POS_Z_MASK);

        // Adjust for potential negative values if masks do not account for sign
        if ((this.x & (1 << 23)) != 0) { // Check if the sign bit is set for 24-bit value
            this.x |= ~POS_X_MASK; // Sign extend
        }
        if ((this.z & (1 << 23)) != 0) { // Check if the sign bit is set for 24-bit value
            this.z |= ~POS_Z_MASK; // Sign extend
        }
    }
}
