package no.jckf.dhsupport.core.message.plugin;

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;

public class CloseReasonMessage extends PluginMessage
{
    protected String reason;

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getReason()
    {
        return reason;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeString(this.reason);
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.reason = decoder.readString();
    }
}
