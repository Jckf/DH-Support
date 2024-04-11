package no.jckf.dhsupport.DataObjects;

import no.jckf.dhsupport.ByteStream.Decodable;
import no.jckf.dhsupport.ByteStream.Decoder;
import no.jckf.dhsupport.ByteStream.Encodable;
import no.jckf.dhsupport.ByteStream.Encoder;

public abstract class DataObject implements Encodable, Decodable
{
    public void encode(Encoder encoder)
    {
        throw new UnsupportedOperationException();
    }

    public void decode(Decoder decoder)
    {
        throw new UnsupportedOperationException();
    }
}
