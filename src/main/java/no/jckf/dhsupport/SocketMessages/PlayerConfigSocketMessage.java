package no.jckf.dhsupport.SocketMessages;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * (int) render distance
 * (bool) distant generation enabled
 * (int) full data request concurrency limit
 * (int) generation task priority request rate limit
 * (bool) real time updates enabled
 * (bool) login data sync enabled
 * (int) login data sync rate/concurrency limit
 * (bool) generate multiple dimensions
 */
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
