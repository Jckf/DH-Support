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
import no.jckf.dhsupport.core.event.EventBus;
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

    protected EventBus<SocketMessage> eventBus;

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

        this.eventBus = new EventBus<>();

        this.eventBus.registerHandler(HelloSocketMessage.class, (hello) -> {
            if (hello.getVersion() != this.protocolVersion) {
                CloseSocketMessage response = new CloseSocketMessage();
                response.setMessage("Incompatible Distant Horizons version. Server speaks protocol version " + this.protocolVersion + ", but client sent protocol version " + hello.getVersion());
                this.sendSocketMessage(hello.getSender(), response);
                return;
            }

            HelloSocketMessage response = new HelloSocketMessage();
            response.setVersion(this.protocolVersion);
            this.sendSocketMessage(hello.getSender(), response);
        });

        this.eventBus.registerHandler(PlayerUuidSocketMessage.class, (uuid) -> {
            AckSocketMessage response = new AckSocketMessage();
            response.isResponseTo(uuid);
            this.sendSocketMessage(uuid.getSender(), response);
        });

        this.eventBus.registerHandler(PlayerConfigSocketMessage.class, (config) -> {
            // TODO: Clamp values according to server config.
            this.sendSocketMessage(config.getSender(), config);
        });

        this.eventBus.registerHandler(FullDataRequestSocketMessage.class, (request) -> {
            FullDataResponseSocketMessage response = new FullDataResponseSocketMessage();
            response.isResponseTo(request);
            //this.sendSocketMessage(socket, response); // TODO: Actually respond with some data. Disabled for now to stop the client from spamming requests.
        });

        this.eventBus.registerHandler(GenerationTaskPriorityRequest.class, (request) -> {
            this.dhSupport.info("Priority request for:");
            request.getSectionPositions().forEach((pos) -> this.dhSupport.info("    " + pos.getX() + " x " + pos.getZ() + " @ " + pos.getDetailLevel()));
        });

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

        message.setSender(socket);

        this.eventBus.dispatch(message);
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
