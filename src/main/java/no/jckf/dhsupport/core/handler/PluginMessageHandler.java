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

import no.jckf.dhsupport.core.DhSupport;
import no.jckf.dhsupport.core.Utils;
import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.event.EventBus;
import no.jckf.dhsupport.core.message.MessageTypeRegistry;
import no.jckf.dhsupport.core.message.plugin.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class PluginMessageHandler
{
    protected DhSupport dhSupport;

    private MessageTypeRegistry messageTypeRegistry;

    public final String pluginChannel = "distant_horizons:message";

    public final int protocolVersion = 3;

    private EventBus<PluginMessage> eventBus;

    public PluginMessageHandler(DhSupport dhSupport)
    {
        this.dhSupport = dhSupport;

        this.eventBus = new EventBus<>();

        // Define plugin channel message types.
        this.messageTypeRegistry = new MessageTypeRegistry();
        this.messageTypeRegistry.registerMessageType(0, null);
        this.messageTypeRegistry.registerMessageType(1, CloseReasonMessage.class);
        this.messageTypeRegistry.registerMessageType(2, CurrentLevelKeyMessage.class);
        this.messageTypeRegistry.registerMessageType(3, RemotePlayerConfigMessage.class);
        this.messageTypeRegistry.registerMessageType(4, CancelMessage.class);
        this.messageTypeRegistry.registerMessageType(5, ExceptionMessage.class);
        this.messageTypeRegistry.registerMessageType(6, FullDataSourceRequestMessage.class);
        this.messageTypeRegistry.registerMessageType(7, FullDataSourceResponseMessage.class);
        this.messageTypeRegistry.registerMessageType(8, /*FullDataPartialUpdateMessage.class*/ null);
        this.messageTypeRegistry.registerMessageType(9, FullDataChunkMessage.class);
    }

    public void onEnable()
    {

    }

    public void onDisable()
    {

    }

    @Nullable
    public EventBus<PluginMessage> getEventBus()
    {
        return this.eventBus;
    }

    public void onPluginMessageReceived(@NotNull String channel, @NotNull UUID senderUuid, byte[] data)
    {
        PluginMessage message;

        try {
            message = this.readPluginMessage(data);
        } catch (Exception exception) {
            this.dhSupport.warning("Error while parsing incoming plugin message: " + exception.getClass() + " - " + exception.getMessage());
            this.dhSupport.warning("Data was: " + Utils.bytesToHex(data));
            return;
        }

        if (message == null) {
            return;
        }

        message.setSender(senderUuid);

        this.eventBus.dispatch(message);
    }

    protected PluginMessage readPluginMessage(byte[] data)
    {
        //this.dhSupport.info("Plugin message received. Length: " + data.length);

        Decoder decoder = new Decoder(data);

        #if READ_FORGE_BYTE == "true"
            decoder.readByte();
        #endif

        // Read the client's protocol version.
        short protocolVersion = decoder.readShort();

        if (protocolVersion != this.protocolVersion) {
            this.dhSupport.warning("Unsupported protocol version: " + protocolVersion);
            return null;
        }

        // Read the message type ID.
        short messageTypeId = decoder.readShort();

        Class<? extends PluginMessage> messageClass = (Class<? extends PluginMessage>) this.messageTypeRegistry.getMessageClass(messageTypeId);

        //this.dhSupport.info("Looks like a " + messageClass.getSimpleName());

        PluginMessage message;

        try {
            message = messageClass.getConstructor().newInstance();

            if (message instanceof TrackablePluginMessage) {
                ((TrackablePluginMessage) message).setTracker(decoder.readInt());
            }

            message.decode(decoder);
        } catch (Exception exception) {
            this.dhSupport.warning("Failed to init message class: " + exception.getClass() + " - " + exception.getMessage());
            return null;
        }

        return message;
    }

    public void sendPluginMessage(UUID recipientUuid, PluginMessage message)
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

        Encoder encoder = new Encoder();

        #if READ_FORGE_BYTE == "true"
            encoder.writeByte(0);
        #endif

        encoder.writeShort(this.protocolVersion);
        encoder.writeShort(messageTypeId);

        if (message instanceof TrackablePluginMessage) {
            encoder.writeInt(((TrackablePluginMessage) message).getTracker());
        }

        encoder.write(data);

        byte[] fullMessage = encoder.toByteArray();

        //this.dhSupport.info("Sending: " + Utils.bytesToHex(fullMessage));

        this.dhSupport.getPluginMessageSender().sendPluginMessage(recipientUuid, this.pluginChannel, fullMessage);
    }
}
