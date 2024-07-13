package no.jckf.dhsupport.core.message.plugin;

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.bytestream.Encoder;
import no.jckf.dhsupport.core.dataobject.SectionPosition;

public class FullDataSourceRequestMessage extends TrackablePluginMessage
{
    protected String worldName;

    protected SectionPosition position;

    protected Long timestamp;

    public void setWorldName(String worldName)
    {
        this.worldName = worldName;
    }

    public String getWorldName()
    {
        return worldName;
    }

    public void setPosition(SectionPosition position)
    {
        this.position = position;
    }

    public SectionPosition getPosition()
    {
        return position;
    }

    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.worldName = decoder.readShortString();

        this.position = new SectionPosition();
        this.position.decode(decoder);

        this.timestamp = decoder.readOptional(decoder::readLong);
    }
}
