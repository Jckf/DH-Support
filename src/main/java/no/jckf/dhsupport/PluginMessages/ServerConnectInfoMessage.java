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

package no.jckf.dhsupport.PluginMessages;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.nio.charset.StandardCharsets;

public class ServerConnectInfoMessage extends PluginMessage
{
    protected String address;

    protected int port;

    public byte[] encode()
    {
        ByteArrayDataOutput writer = ByteStreams.newDataOutput();

        writer.writeBoolean(this.address != null);
        if (this.address != null) {
            writer.writeShort(this.address.length());
            writer.write(this.address.getBytes(StandardCharsets.UTF_8));
        }

        writer.writeShort(this.port);

        return writer.toByteArray();
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public void setPort(int port)
    {
        this.port = port;
    }
}
