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

package no.jckf.dhsupport.core.message.socket;

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.configuration.Configuration;
import no.jckf.dhsupport.core.configuration.DhsConfig;

public class PlayerConfigSocketMessage extends SocketMessage
{
    protected int renderDistance;

    protected boolean distantGenerationEnabled;

    protected int fullDataRequestConcurrencyLimit;

    protected int generationTaskPriorityRequestRateLimit;

    protected boolean realTimeUpdatedEnabled;

    protected boolean loginDataSyncEnabled;

    protected int loginDataSyncRateConcurrencyLimit;

    protected boolean generateMultipleDimensions;

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeInt(this.renderDistance);
        encoder.writeBoolean(this.distantGenerationEnabled);
        encoder.writeInt(this.fullDataRequestConcurrencyLimit);
        encoder.writeInt(this.generationTaskPriorityRequestRateLimit);
        encoder.writeBoolean(this.realTimeUpdatedEnabled);
        encoder.writeBoolean(this.loginDataSyncEnabled);
        encoder.writeInt(this.loginDataSyncRateConcurrencyLimit);
        encoder.writeBoolean(this.generateMultipleDimensions);
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.renderDistance = decoder.readInt();
        this.distantGenerationEnabled = decoder.readBoolean();
        this.fullDataRequestConcurrencyLimit = decoder.readInt();
        this.generationTaskPriorityRequestRateLimit = decoder.readInt();
        this.realTimeUpdatedEnabled = decoder.readBoolean();
        this.loginDataSyncEnabled = decoder.readBoolean();
        this.loginDataSyncRateConcurrencyLimit = decoder.readInt();
        this.generateMultipleDimensions = decoder.readBoolean();
    }

    public void fromConfiguration(Configuration config)
    {
        this.renderDistance = config.getInt(DhsConfig.RENDER_DISTANCE);
        this.distantGenerationEnabled = config.getBool(DhsConfig.DISTANT_GENERATION_ENABLED);
        this.fullDataRequestConcurrencyLimit = config.getInt(DhsConfig.FULL_DATA_REQUEST_CONCURRENCY_LIMIT);
        this.generationTaskPriorityRequestRateLimit = config.getInt(DhsConfig.GENERATION_TASK_PRIORITY_REQUEST_RATE_LIMIT);
        this.realTimeUpdatedEnabled = config.getBool(DhsConfig.REAL_TIME_UPDATES_ENABLED);
        this.loginDataSyncEnabled = config.getBool(DhsConfig.LOGIN_DATA_SYNC_ENABLED);
        this.loginDataSyncRateConcurrencyLimit = config.getInt(DhsConfig.LOGIN_DATA_SYNC_RC_LIMIT);
        this.generateMultipleDimensions = config.getBool(DhsConfig.GENERATE_MULTIPLE_DIMENSIONS);
    }

    public Configuration toConfiguration()
    {
        Configuration config = new Configuration();

        config.set(DhsConfig.RENDER_DISTANCE, this.renderDistance);
        config.set(DhsConfig.DISTANT_GENERATION_ENABLED, this.distantGenerationEnabled);
        config.set(DhsConfig.FULL_DATA_REQUEST_CONCURRENCY_LIMIT, this.fullDataRequestConcurrencyLimit);
        config.set(DhsConfig.GENERATION_TASK_PRIORITY_REQUEST_RATE_LIMIT, this.generationTaskPriorityRequestRateLimit);
        config.set(DhsConfig.REAL_TIME_UPDATES_ENABLED, this.realTimeUpdatedEnabled);
        config.set(DhsConfig.LOGIN_DATA_SYNC_ENABLED, this.loginDataSyncEnabled);
        config.set(DhsConfig.LOGIN_DATA_SYNC_RC_LIMIT, this.loginDataSyncRateConcurrencyLimit);
        config.set(DhsConfig.GENERATE_MULTIPLE_DIMENSIONS, this.generateMultipleDimensions);

        return config;
    }
}
