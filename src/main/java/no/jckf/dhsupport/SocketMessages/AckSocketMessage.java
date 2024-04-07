package no.jckf.dhsupport.SocketMessages;

import com.google.common.io.ByteArrayDataInput;

public class AckSocketMessage extends TrackableSocketMessage
{
    @Override
    public byte[] encode()
    {
        return new byte[0];
    }

    @Override
    public void decode(ByteArrayDataInput reader)
    {

    }
}
