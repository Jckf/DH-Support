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

package no.jckf.dhsupport.SocketMessages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
    public byte[] encode()
    {
        ByteArrayDataOutput writer = ByteStreams.newDataOutput();

        writer.writeInt(this.renderDistance);
        writer.writeBoolean(this.distantGenerationEnabled);
        writer.writeInt(this.fullDataRequestConcurrencyLimit);
        writer.writeInt(this.generationTaskPriorityRequestRateLimit);
        writer.writeBoolean(this.realTimeUpdatedEnabled);
        writer.writeBoolean(this.loginDataSyncEnabled);
        writer.writeInt(this.loginDataSyncRateConcurrencyLimit);
        writer.writeBoolean(this.generateMultipleDimensions);

        return writer.toByteArray();
    }

    @Override
    public void decode(ByteArrayDataInput reader)
    {
        this.renderDistance = reader.readInt();
        this.distantGenerationEnabled = reader.readBoolean();
        this.fullDataRequestConcurrencyLimit = reader.readInt();
        this.generationTaskPriorityRequestRateLimit = reader.readInt();
        this.realTimeUpdatedEnabled = reader.readBoolean();
        this.loginDataSyncEnabled = reader.readBoolean();
        this.loginDataSyncRateConcurrencyLimit = reader.readInt();
        this.generateMultipleDimensions = reader.readBoolean();
    }

    // TODO: Setters and getters.
}
