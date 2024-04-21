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

public class DataPoint extends DataObject
{
    protected byte skyLight = 0x0F;

    protected byte blockLight = 0x00;

    protected int height = 1;

    protected int startZ = 0;

    protected int mappingId;

    public void setSkyLight(byte value)
    {
        this.skyLight = value;
    }

    public void setBlockLight(byte value)
    {
        this.blockLight = value;
    }

    public void setHeight(int value)
    {
        this.height = value;
    }

    public void setStartZ(int value)
    {
        this.startZ = value;
    }

    public void setMappingId(int id)
    {
        this.mappingId = id;
    }

    @Override
    public void encode(Encoder encoder)
    {
        long data = 0;

        data |= this.mappingId;
        data |= (long) (this.height & 0x0FFF) << 32;
        data |= (long) (this.startZ & 0x0FFF) << 32 + 12;
        data |= (long) (this.blockLight & 0x0F) << 32 + 12 + 12;
        data |= (long) (this.skyLight & 0x0F) << 32 + 12 + 12 + 4;

        encoder.writeLong(data);
    }
}
