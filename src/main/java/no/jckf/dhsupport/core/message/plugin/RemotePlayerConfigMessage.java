package no.jckf.dhsupport.core.message.plugin;

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;

public class RemotePlayerConfigMessage extends PluginMessage
{
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
}
