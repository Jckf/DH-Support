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

package no.jckf.dhsupport.Messages.Socket;

import no.jckf.dhsupport.ByteStream.Decoder;
import no.jckf.dhsupport.ByteStream.Encoder;

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

    // TODO: Setters and getters.
}
