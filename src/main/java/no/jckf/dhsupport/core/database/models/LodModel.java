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

package no.jckf.dhsupport.core.database.models;

import java.util.UUID;

public class LodModel extends Model
{
    public static LodModel create()
    {
        return new LodModel();
    }

    protected UUID worldId;

    protected int x;

    protected int z;

    protected byte[] data;

    protected int timestamp;

    public LodModel setWorldId(UUID worldId)
    {
        this.worldId = worldId;

        return this;
    }

    public UUID getWorldId()
    {
        return worldId;
    }

    public LodModel setX(int x)
    {
        this.x = x;

        return this;
    }

    public int getX()
    {
        return x;
    }

    public LodModel setZ(int z)
    {
        this.z = z;

        return this;
    }

    public int getZ()
    {
        return z;
    }

    public LodModel setData(byte[] data)
    {
        this.data = data;

        return this;
    }

    public byte[] getData()
    {
        return data;
    }

    public LodModel setTimestamp(int timestamp)
    {
        this.timestamp = timestamp;

        return this;
    }

    public int getTimestamp()
    {
        return timestamp;
    }

    public String toString()
    {
        return this.getWorldId() + "@" + this.getX() + "x" + this.getZ();
    }
}
