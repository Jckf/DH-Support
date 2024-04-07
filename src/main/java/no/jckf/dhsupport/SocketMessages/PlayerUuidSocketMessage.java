package no.jckf.dhsupport.SocketMessages;

import com.google.common.io.ByteArrayDataInput;

import java.util.UUID;

public class PlayerUuidSocketMessage extends TrackableSocketMessage
{
    protected UUID uuid;

    @Override
    public void decode(ByteArrayDataInput reader)
    {
        this.uuid = new UUID(reader.readLong(), reader.readLong());
    }

    public void setUuid(UUID uuid)
    {
        this.uuid = uuid;
    }

    public UUID getUuid()
    {
        return this.uuid;
    }
}
