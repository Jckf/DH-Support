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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import no.jckf.dhsupport.core.DhSupport;

public class SocketServer
{
    protected DhSupport plugin;

    protected EventLoopGroup bossGroup;

    protected EventLoopGroup workerGroup;

    public SocketServer(DhSupport plugin)
    {
        this.plugin = plugin;
    }

    public final void startup(int port) throws Exception
    {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(this.bossGroup, this.workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new SocketInitializer(this.plugin))
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        ChannelFuture future = bootstrap.bind(port).sync();

        if (!future.isSuccess()) {
            throw new Exception("Failed to bind to port " + port);
        }
    }

    public final void shutdown() throws Exception
    {
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }
}
