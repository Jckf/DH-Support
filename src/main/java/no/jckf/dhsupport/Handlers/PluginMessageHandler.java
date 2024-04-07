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
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import no.jckf.dhsupport.DhSupport;
import no.jckf.dhsupport.MessageTypeRegistry;
import no.jckf.dhsupport.PluginMessages.CurrentLevelKeyMessage;
import no.jckf.dhsupport.PluginMessages.HelloPluginMessage;
import no.jckf.dhsupport.PluginMessages.PluginMessage;
import no.jckf.dhsupport.PluginMessages.ServerConnectInfoMessage;
import no.jckf.dhsupport.Utils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class PluginMessageHandler implements PluginMessageListener
{
    protected DhSupport plugin;

    private MessageTypeRegistry messageTypeRegistry;

    protected final String pluginChannel = "distant_horizons:plugin_channel";

    protected final int protocolVersion = 1;

    public PluginMessageHandler(DhSupport plugin)
    {
        this.plugin = plugin;
    }

    public void onEnable()
    {
        // Define plugin channel message types.
        this.messageTypeRegistry = new MessageTypeRegistry();
        this.messageTypeRegistry.registerMessageType(0, null);
        this.messageTypeRegistry.registerMessageType(1, HelloPluginMessage.class);
        this.messageTypeRegistry.registerMessageType(2, CurrentLevelKeyMessage.class);
        this.messageTypeRegistry.registerMessageType(3, ServerConnectInfoMessage.class);

        // Register for plugin channel messages.
        this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, this.pluginChannel, this);
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, this.pluginChannel);
    }

    public void onDisable()
    {
        this.plugin.getServer().getMessenger().unregisterIncomingPluginChannel(this.plugin);
        this.plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(this.plugin);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] data)
    {
        PluginMessage message;

        try {
            message = this.readPluginMessage(data);
        } catch (Exception exception) {
            this.plugin.warning("Error while parsing incoming plugin message: " + exception.getMessage());
            return;
        }

        if (message instanceof HelloPluginMessage) {
            ServerConnectInfoMessage response = new ServerConnectInfoMessage();
            //response.setAddress("google.com");
            response.setPort(this.plugin.getConfig().getInt("port"));

            this.sendPluginMessage(player, 3, response.encode());
        }
    }

    protected PluginMessage readPluginMessage(byte[] data)
    {
        this.plugin.info("DH plugin message received. Length: " + data.length);

        ByteArrayDataInput reader = ByteStreams.newDataInput(data);

        // Read and discard sub-channel. DH always sends a null byte here.
        reader.readByte();

        // Read the client's protocol version.
        short protocolVersion = reader.readShort();

        if (protocolVersion != this.protocolVersion) {
            this.plugin.warning("Unsupported protocol version: " + protocolVersion);
            return null;
        }

        // Read the message type ID.
        short messageTypeId = reader.readShort();

        Class<? extends PluginMessage> messageClass = this.messageTypeRegistry.getMessageType(messageTypeId);

        this.plugin.info("Looks like a " + messageClass.getSimpleName());

        PluginMessage message;

        try {
            message = messageClass.getConstructor().newInstance();
        } catch (Exception exception) {
            this.plugin.warning("Failed to init message class: " + exception.getMessage());
            return null;
        }

        message.decode(reader);

        return message;
    }

    protected void sendPluginMessage(Player player, int messageTypeId, byte[] data)
    {
        ByteArrayDataOutput writer = ByteStreams.newDataOutput();

        writer.writeByte(0);
        writer.writeShort(this.protocolVersion);
        writer.writeShort(messageTypeId);

        byte[] fullMessage = Bytes.concat(writer.toByteArray(), data);

        this.plugin.info("Sending: " + Utils.bytesToHex(fullMessage));

        player.sendPluginMessage(this.plugin, this.pluginChannel, fullMessage);
    }
}
