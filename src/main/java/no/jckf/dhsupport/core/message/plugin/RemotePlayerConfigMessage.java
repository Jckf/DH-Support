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

package no.jckf.dhsupport.core.message.plugin;

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.DhsConfig;

public class RemotePlayerConfigMessage extends PluginMessage
{
    public static String[] KEYS = {
        DhsConfig.RENDER_DISTANCE,
        DhsConfig.DISTANT_GENERATION_ENABLED,
        DhsConfig.FULL_DATA_REQUEST_CONCURRENCY_LIMIT,
        DhsConfig.REAL_TIME_UPDATES_ENABLED,
        DhsConfig.LOGIN_DATA_SYNC_ENABLED,
        DhsConfig.LOGIN_DATA_SYNC_RC_LIMIT,
    };

    protected int renderDistance;

    protected boolean distantGenerationEnabled;

    protected int fullDataRequestConcurrencyLimit;

    protected boolean realTimeUpdatesEnabled;

    protected boolean loginDataSyncEnabled;

    protected int loginDataSyncRcLimit;

    public void setRenderDistance(int distance)
    {
        this.renderDistance = distance;
    }

    public int getRenderDistance()
    {
        return this.renderDistance;
    }

    public void setDistantGenerationEnabled(boolean enabled)
    {
        this.distantGenerationEnabled = enabled;
    }

    public boolean getDistantGenerationEnabled()
    {
        return this.distantGenerationEnabled;
    }

    public void setFullDataRequestConcurrencyLimit(int limit)
    {
        this.fullDataRequestConcurrencyLimit = limit;
    }

    public int getFullDataRequestConcurrencyLimit()
    {
        return fullDataRequestConcurrencyLimit;
    }

    public void setRealTimeUpdatesEnabled(boolean enabled)
    {
        this.realTimeUpdatesEnabled = enabled;
    }

    public boolean isRealTimeUpdatesEnabled()
    {
        return realTimeUpdatesEnabled;
    }

    public void setLoginDataSyncEnabled(boolean enabled)
    {
        this.loginDataSyncEnabled = enabled;
    }

    public boolean getLoginDataSyncEnabled()
    {
        return loginDataSyncEnabled;
    }

    public void setLoginDataSyncRcLimit(int limit)
    {
        this.loginDataSyncRcLimit = limit;
    }

    public int getLoginDataSyncRcLimit()
    {
        return loginDataSyncRcLimit;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeInt(this.renderDistance);
        encoder.writeBoolean(this.distantGenerationEnabled);
        encoder.writeInt(this.fullDataRequestConcurrencyLimit);
        encoder.writeBoolean(this.realTimeUpdatesEnabled);
        encoder.writeBoolean(this.loginDataSyncEnabled);
        encoder.writeInt(this.loginDataSyncRcLimit);
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.renderDistance = decoder.readInt();
        this.distantGenerationEnabled = decoder.readBoolean();
        this.fullDataRequestConcurrencyLimit = decoder.readInt();
        this.realTimeUpdatesEnabled = decoder.readBoolean();
        this.loginDataSyncEnabled = decoder.readBoolean();
        this.loginDataSyncRcLimit = decoder.readInt();
    }

    public void fromConfiguration(Configuration config)
    {
        this.renderDistance = config.getInt(DhsConfig.RENDER_DISTANCE);
        this.distantGenerationEnabled = config.getBool(DhsConfig.DISTANT_GENERATION_ENABLED);
        this.fullDataRequestConcurrencyLimit = config.getInt(DhsConfig.FULL_DATA_REQUEST_CONCURRENCY_LIMIT);
        this.realTimeUpdatesEnabled = config.getBool(DhsConfig.REAL_TIME_UPDATES_ENABLED);
        this.loginDataSyncEnabled = config.getBool(DhsConfig.LOGIN_DATA_SYNC_ENABLED);
        this.loginDataSyncRcLimit = config.getInt(DhsConfig.LOGIN_DATA_SYNC_RC_LIMIT);
    }

    public Configuration toConfiguration()
    {
        Configuration config = new Configuration();

        config.set(DhsConfig.RENDER_DISTANCE, this.renderDistance);
        config.set(DhsConfig.DISTANT_GENERATION_ENABLED, this.distantGenerationEnabled);
        config.set(DhsConfig.FULL_DATA_REQUEST_CONCURRENCY_LIMIT, this.fullDataRequestConcurrencyLimit);
        config.set(DhsConfig.REAL_TIME_UPDATES_ENABLED, this.realTimeUpdatesEnabled);
        config.set(DhsConfig.LOGIN_DATA_SYNC_ENABLED, this.loginDataSyncEnabled);
        config.set(DhsConfig.LOGIN_DATA_SYNC_RC_LIMIT, this.loginDataSyncRcLimit);

        return config;
    }
}
