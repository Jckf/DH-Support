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

package no.jckf.dhsupport.core.socketserver;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import no.jckf.dhsupport.core.DhSupport;

public class SocketHandler extends ChannelInboundHandlerAdapter
{
    protected DhSupport dhSupport;

    protected ChannelGroup sockets;

    public SocketHandler(DhSupport dhSupport, ChannelGroup sockets)
    {
        this.dhSupport = dhSupport;
        this.sockets = sockets;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        this.sockets.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object data)
    {
        ByteBuf buf = (ByteBuf) data;

        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), bytes);

        this.dhSupport.getSocketMessageHandler().onSocketMessageReceived(context.channel(), bytes);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause)
    {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        context.close();
    }
}
