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

package no.jckf.dhsupport;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class MessageWriter
{
    protected ByteArrayDataOutput output;

    public MessageWriter()
    {
        this.output = ByteStreams.newDataOutput();
    }

    public void write(byte[] data)
    {
        this.output.write(data);
    }

    public void writeByte(byte value)
    {
        this.output.writeByte(value);
    }

    public void writeByte(short value)
    {
        this.writeByte((byte) value);
    }

    public void writeByte(int value)
    {
        this.writeByte((byte) value);
    }

    public void writeBoolean(boolean value)
    {
        this.output.writeBoolean(value);
    }

    public void writeShort(short value)
    {
        this.output.writeShort(value);
    }

    public void writeShort(int value)
    {
        this.writeShort((short) value);
    }

    public void writeInt(int value)
    {
        this.output.writeInt(value);
    }

    public void writeLong(long value)
    {
        this.output.writeLong(value);
    }

    public void writeString(String value)
    {
        this.output.writeInt(value.length());
        this.output.writeUTF(value);
    }

    public boolean writeOptional(Object value)
    {
        boolean hasValue = value != null;

        this.writeBoolean(hasValue);

        return hasValue;
    }

    public byte[] toByteArray()
    {
        return this.output.toByteArray();
    }
}
