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

package no.jckf.dhsupport.core.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.message.MessageTypeRegistry;
import no.jckf.dhsupport.core.message.socket.*;
import no.jckf.dhsupport.core.socketserver.SocketServer;
import no.jckf.dhsupport.core.Utils;

public class SocketMessageHandler
{
    protected DhSupport dhSupport;

    protected MessageTypeRegistry messageTypeRegistry;

    protected SocketServer socketServer;

    protected final int protocolVersion = 2;

    public SocketMessageHandler(DhSupport dhSupport)
    {
        this.dhSupport = dhSupport;
    }

    public void onEnable()
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
        this.messageTypeRegistry.registerMessageType(0x000a, PartialUpdateSocketMessage.class);
        this.messageTypeRegistry.registerMessageType(0x000b, GenerationTaskPriorityRequest.class);
        this.messageTypeRegistry.registerMessageType(0x000c, GenerationTaskPriorityResponse.class);

        this.socketServer = new SocketServer(this.dhSupport);

        try {
            this.socketServer.startup(this.dhSupport.getConfig().getInt("port"));
        } catch (Exception exception) {
            this.dhSupport.warning("Failed to start socket server: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
            // TODO: Do something about it?
        }
    }

    public void onDisable()
    {
        try {
            this.socketServer.shutdown();
        } catch (Exception exception) {
            this.dhSupport.warning("Failed to gracefully stop socket server: " + exception.getClass().getSimpleName() + " - " + exception.getMessage());
            this.socketServer = null;
        }
    }

    public void onSocketMessageReceived(Channel socket, byte[] data)
    {
        this.dhSupport.info("Socket message received. Length: " + data.length);

        SocketMessage message;

        try {
            message = this.readSocketMessage(data);
        } catch (Exception exception) {
            this.dhSupport.warning("Error while parsing incoming socket message: " + exception.getClass() + " - " + exception.getMessage());
            this.dhSupport.warning("Data was: " + Utils.bytesToHex(data));
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

            this.dhSupport.info("UUID: " + playerUuid.getUuid());

            /*Player player = this.plugin.getServer().getPlayer(playerUuid.getUuid());

            // TODO: Also check if player is using DH at all or has already connected.
            if (player == null) {
                this.plugin.warning("UUID does not match any online player.");

                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("Invalid UUID.");
                this.sendSocketMessage(socket, response);
                return;
            }

            this.plugin.info("Socket belongs to player " + player.getName());*/

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

        if (message instanceof GenerationTaskPriorityRequest) {
            GenerationTaskPriorityRequest priorityRequest = (GenerationTaskPriorityRequest) message;

            this.dhSupport.info("Priority request for:");
            priorityRequest.getSectionPositions().forEach((pos) -> this.dhSupport.info("    " + pos.getX() + " x " + pos.getZ() + " @ " + pos.getDetailLevel()));
            return;
        }

        // TODO: Some sort of event bus for handlers, instead of a million ifs 👆
        this.dhSupport.warning("Message was successfully parsed, but there is no code to handle it.");
    }

    protected SocketMessage readSocketMessage(byte[] data)
    {
        Decoder decoder = new Decoder(data);

        short messageTypeId = decoder.readShort();

        Class<? extends SocketMessage> messageClass = (Class<? extends SocketMessage>) this.messageTypeRegistry.getMessageClass(messageTypeId);

        if (messageClass == null) {
            this.dhSupport.warning("Unknown message type " + messageTypeId + " with body " + Utils.bytesToHex(data));
            return null;
        }

        this.dhSupport.info("Looks like a " + messageClass.getSimpleName());

        SocketMessage message;

        try {
            message = messageClass.getConstructor().newInstance();

            if (message instanceof TrackableSocketMessage) {
                TrackableSocketMessage trackable = (TrackableSocketMessage) message;

                trackable.setTracker(decoder.readInt());

                this.dhSupport.info("Message is trackable: " + trackable.getTracker());
            }

            message.decode(decoder);
        } catch (Exception exception) {
            this.dhSupport.warning("Failed to init message class: " + exception.getClass() + " - " + exception.getMessage());
            return null;
        }

        return message;
    }

    protected void sendSocketMessage(Channel socket, SocketMessage message)
    {
        int messageTypeId = this.messageTypeRegistry.getMessageTypeId(message.getClass());

        if (messageTypeId == -1) {
            this.dhSupport.warning("Trying to send unknown message type: " + message.getClass());
            return;
        }

        byte[] data;

        try {
            Encoder encoder = new Encoder();
            message.encode(encoder);
            data = encoder.toByteArray();
        } catch (Exception exception) {
            this.dhSupport.warning("Failed to encode " + message.getClass() + ": " + exception.getClass() + " - " + exception.getMessage());
            return;
        }

        int tracker = -1;

        if (message instanceof TrackableSocketMessage) {
            TrackableSocketMessage trackable = (TrackableSocketMessage) message;

            tracker = trackable.getTracker();
        }

        // TODO: Keep track of trackable messages.

        Encoder encoder = new Encoder();

        encoder.writeInt(data.length + 2 + (tracker == -1 ? 0 : 4));
        encoder.writeShort(messageTypeId);

        if (tracker != -1) {
            encoder.writeInt(tracker);
        }

        encoder.write(data);

        byte[] fullMessage = encoder.toByteArray();

        this.dhSupport.info("Sending: " + Utils.bytesToHex(fullMessage));

        socket.write(Unpooled.wrappedBuffer(fullMessage));
        socket.flush();
    }
}
