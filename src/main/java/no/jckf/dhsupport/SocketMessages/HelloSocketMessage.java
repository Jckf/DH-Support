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

package no.jckf.dhsupport.SocketMessages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class HelloSocketMessage extends SocketMessage
{
    protected int version;

    @Override
    public void decode(ByteArrayDataInput reader)
    {
        this.version = reader.readInt();
    }

    @Override
    public byte[] encode()
    {
        ByteArrayDataOutput writer = ByteStreams.newDataOutput();

        writer.writeInt(this.version);

        return writer.toByteArray();
    }

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }
}
