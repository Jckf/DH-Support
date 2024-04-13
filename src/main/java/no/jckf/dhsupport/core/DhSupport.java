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

package no.jckf.dhsupport.core;

import no.jckf.dhsupport.core.configuration.Configurable;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.handler.PluginMessageHandler;
import no.jckf.dhsupport.core.handler.SocketMessageHandler;
import no.jckf.dhsupport.core.message.plugin.PluginMessageSender;
import javax.annotation.Nullable;

import java.util.logging.Logger;

public class DhSupport implements Configurable
{
    protected Configuration configuration;

    protected Logger logger;

    protected PluginMessageHandler pluginMessageHandler;

    protected SocketMessageHandler socketMessageHandler;

    protected PluginMessageSender pluginMessageSender;

    public void onEnable()
    {
        this.getPluginMessageHandler().onEnable();
        this.getSocketMessageHandler().onEnable();
    }

    public void onDisable()
    {
        this.getSocketMessageHandler().onDisable();
        this.getPluginMessageHandler().onDisable();
    }

    public PluginMessageHandler getPluginMessageHandler()
    {
        if (this.pluginMessageHandler == null) {
            this.pluginMessageHandler = new PluginMessageHandler(this);
        }

        return this.pluginMessageHandler;
    }

    public SocketMessageHandler getSocketMessageHandler()
    {
        if (this.socketMessageHandler == null) {
            this.socketMessageHandler = new SocketMessageHandler(this);
        }

        return this.socketMessageHandler;
    }

    public void setPluginMessageSender(PluginMessageSender sender)
    {
        this.pluginMessageSender = sender;
    }

    @Nullable
    public PluginMessageSender getPluginMessageSender()
    {
        return this.pluginMessageSender;
    }

    @Override
    public Configuration getConfig()
    {
        if (this.configuration == null) {
            this.configuration = new Configuration();
        }

        return this.configuration;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Nullable
    public Logger getLogger()
    {
        return this.logger;
    }

    public void info(String message)
    {
        this.getLogger().info(message);
    }

    public void warning(String message)
    {
        this.getLogger().warning(message);
    }
}
