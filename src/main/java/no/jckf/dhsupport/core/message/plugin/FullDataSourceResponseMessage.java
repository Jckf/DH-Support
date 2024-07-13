package no.jckf.dhsupport.core.message.plugin;

import no.jckf.dhsupport.core.bytestream.Encoder;

public class FullDataSourceResponseMessage extends TrackablePluginMessage
{
    protected byte[] data;

    public void setData(byte[] data)
    {
        this.data = data;
    }

    public byte[] getData()
    {
        return data;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeBoolean(true);
        encoder.write(this.data);
    }
}
