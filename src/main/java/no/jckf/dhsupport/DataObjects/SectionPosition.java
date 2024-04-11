package no.jckf.dhsupport.DataObjects;

import no.jckf.dhsupport.ByteStream.Decoder;
import no.jckf.dhsupport.ByteStream.Encoder;

public class SectionPosition extends DataObject
{
    protected int detailLevel;

    protected int x;

    protected int z;

    public void setDetailLevel(int level)
    {
        this.detailLevel = level;
    }

    public int getDetailLevel()
    {
        return this.detailLevel;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getX()
    {
        return this.x;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public int getZ()
    {
        return this.z;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeByte(this.detailLevel);
        encoder.writeInt(this.x);
        encoder.writeInt(this.z);
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.detailLevel = decoder.readByte();
        this.x = decoder.readInt();
        this.z = decoder.readInt();
    }
}
