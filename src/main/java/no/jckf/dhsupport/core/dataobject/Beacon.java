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

public class Beacon extends DataObject
{
    protected BlockPosition position;

    protected int color;

    public Beacon()
    {

    }

    public Beacon(BlockPosition position, int color)
    {
        this.position = position;
        this.color = color;
    }

    public Beacon(int x, int y, int z, int color)
    {
        this(new BlockPosition(x, y, z), color);
    }

    @Override
    public void encode(Encoder encoder)
    {
        this.position.encode(encoder);

        encoder.writeInt(this.color);
    }
}
