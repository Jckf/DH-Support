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

package no.jckf.dhsupport.Handlers;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import no.jckf.dhsupport.DhSupport;
import no.jckf.dhsupport.SocketServer.SocketServer;
import no.jckf.dhsupport.Utils;

public class SocketMessageHandler
{
    protected DhSupport plugin;

    protected SocketServer socketServer;

    public SocketMessageHandler(DhSupport plugin)
    {
        this.plugin = plugin;
    }

    public void onEnable() throws Exception
    {
        this.socketServer = new SocketServer(this.plugin);

        this.socketServer.startup(this.plugin.getConfig().getInt("port"));
    }

    public void onDisable() throws Exception
    {
        this.socketServer.shutdown();
    }

    public void onSocketMessageReceived(byte[] data)
    {
        this.plugin.info("Socket message received: " + data.length);

        ByteArrayDataInput reader = ByteStreams.newDataInput(data);

        int length = reader.readInt() - 2;

        short messageTypeId = reader.readShort();

        byte[] body = new byte[length];
        reader.readFully(body, 0, length);

        this.plugin.info("Looks like type " + messageTypeId + " with " + length + " additional bytes: " + Utils.bytesToHex(body));

        // TODO: New registry for socket messages.
        //       Only read body if no message type match is found.
    }
}
