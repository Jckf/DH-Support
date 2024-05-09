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
    protected int detailLevel;

    protected int x;

    protected int z;

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
        encoder.writeByte(this.detailLevel);
        encoder.writeInt(this.x);
        encoder.writeInt(this.z);
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.detailLevel = decoder.readByte();
        this.x = decoder.readInt();
        this.z = decoder.readInt();
    }
}
