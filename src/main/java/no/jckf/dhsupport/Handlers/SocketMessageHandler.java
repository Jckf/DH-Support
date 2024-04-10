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

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import no.jckf.dhsupport.*;
import no.jckf.dhsupport.SocketMessages.*;
import no.jckf.dhsupport.SocketServer.SocketServer;
import org.bukkit.entity.Player;

public class SocketMessageHandler
{
    protected DhSupport plugin;

    protected MessageTypeRegistry messageTypeRegistry;

    protected SocketServer socketServer;

    protected final int protocolVersion = 2;

    public SocketMessageHandler(DhSupport plugin)
    {
        this.plugin = plugin;
    }

    public void onEnable() throws Exception
    {
        this.messageTypeRegistry = new MessageTypeRegistry();
        this.messageTypeRegistry.registerMessageType(0x0000, null);
        this.messageTypeRegistry.registerMessageType(0x0001, HelloSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0002, CloseSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0003, AckSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0004, CancelSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0005, ExceptionSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0006, PlayerUuidSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0007, PlayerConfigSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0008, FullDataRequestSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x0009, FullDataResponseSocketMessage.class);

        this.socketServer = new SocketServer(this.plugin);

        this.socketServer.startup(this.plugin.getConfig().getInt("port"));
    }

    public void onDisable() throws Exception
    {
        this.socketServer.shutdown();
    }

    public void onSocketMessageReceived(Channel socket, byte[] data)
    {
        this.plugin.info("Socket message received. Length: " + data.length);

        SocketMessage message;

        try {
            message = this.readSocketMessage(data);
        } catch (Exception exception) {
            this.plugin.warning("Error while parsing incoming socket message: " + exception.getClass() + " - " + exception.getMessage());
            this.plugin.warning("Data was: " + Utils.bytesToHex(data));
            return;
        }

        if (message == null) {
            return;
        }

        if (message instanceof HelloSocketMessage) {
            HelloSocketMessage clientHello = (HelloSocketMessage) message;

            if (clientHello.getVersion() != this.protocolVersion) {
                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("Incompatible Distant Horizons version. Server speaks protocol version " + this.protocolVersion + ", but client sent protocol version " + clientHello.getVersion());
                this.sendSocketMessage(socket, response);
                return;
            }

            HelloSocketMessage response = new HelloSocketMessage();
            response.setVersion(this.protocolVersion);
            this.sendSocketMessage(socket, response);
            return;
        }

        if (message instanceof PlayerUuidSocketMessage) {
            PlayerUuidSocketMessage playerUuid = (PlayerUuidSocketMessage) message;

            this.plugin.info("UUID: " + playerUuid.getUuid());

            Player player = this.plugin.getServer().getPlayer(playerUuid.getUuid());

            // TODO: Also check if player is using DH at all or has already connected.
            if (player == null) {
                this.plugin.warning("UUID does not match any online player.");

                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("Invalid UUID.");
                this.sendSocketMessage(socket, response);
                return;
            }

            this.plugin.info("Socket belongs to player " + player.getName());

            AckSocketMessage response = new AckSocketMessage();
            response.isResponseTo(playerUuid);
            this.sendSocketMessage(socket, response);
            return;
        }

        if (message instanceof PlayerConfigSocketMessage) {
            PlayerConfigSocketMessage config = (PlayerConfigSocketMessage) message;

            // TODO: Clamp values according to server config.
            this.sendSocketMessage(socket, config);
            return;
        }

        if (message instanceof FullDataRequestSocketMessage) {
            FullDataRequestSocketMessage dataRequest = (FullDataRequestSocketMessage) message;

            FullDataResponseSocketMessage response = new FullDataResponseSocketMessage();
            response.isResponseTo(dataRequest);
            //this.sendSocketMessage(socket, response); // TODO: Actually respond with some data. Disabled for now to stop the client from spamming requests.
            return;
        }

        // TODO: Some sort of event bus for handlers, instead of a million ifs 👆
        this.plugin.warning("Message was successfully parsed, but there is no code to handle it.");
    }

    protected SocketMessage readSocketMessage(byte[] data)
    {
        MessageReader reader = new MessageReader(data);

        short messageTypeId = reader.readShort();

        Class<? extends SocketMessage> messageClass = (Class<? extends SocketMessage>) this.messageTypeRegistry.getMessageClass(messageTypeId);

        if (messageClass == null) {
            this.plugin.warning("Unknown message type " + messageTypeId + " with body " + Utils.bytesToHex(data));
            return null;
        }

        this.plugin.info("Looks like a " + messageClass.getSimpleName());

        SocketMessage message;

        try {
            message = messageClass.getConstructor().newInstance();

            if (message instanceof TrackableSocketMessage) {
                TrackableSocketMessage trackable = (TrackableSocketMessage) message;

                trackable.setTracker(reader.readInt());

                this.plugin.info("Message is trackable: " + trackable.getTracker());
            }

            message.decode(reader);
        } catch (Exception exception) {
            this.plugin.warning("Failed to init message class: " + exception.getClass() + " - " + exception.getMessage());
            return null;
        }

        return message;
    }

    protected void sendSocketMessage(Channel socket, SocketMessage message)
    {
        int messageTypeId = this.messageTypeRegistry.getMessageTypeId(message.getClass());

        if (messageTypeId == -1) {
            this.plugin.warning("Trying to send unknown message type: " + message.getClass());
            return;
        }

        byte[] data;

        try {
            MessageWriter writer = new MessageWriter();
            message.encode(writer);
            data = writer.toByteArray();
        } catch (Exception exception) {
            this.plugin.warning("Failed to encode " + message.getClass() + ": " + exception.getClass() + " - " + exception.getMessage());
            return;
        }

        int tracker = -1;

        if (message instanceof TrackableSocketMessage) {
            TrackableSocketMessage trackable = (TrackableSocketMessage) message;

            tracker = trackable.getTracker();
        }

        // TODO: Keep track of trackable messages.

        MessageWriter writer = new MessageWriter();

        writer.writeInt(data.length + 2 + (tracker == -1 ? 0 : 4));
        writer.writeShort(messageTypeId);

        if (tracker != -1) {
            writer.writeInt(tracker);
        }

        writer.write(data);

        byte[] fullMessage = writer.toByteArray();

        this.plugin.info("Sending: " + Utils.bytesToHex(fullMessage));

        socket.write(Unpooled.wrappedBuffer(fullMessage));
        socket.flush();
    }
}
