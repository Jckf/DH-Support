package no.jckf.dhsupport.core.dataobject;

import no.jckf.dhsupport.core.bytestream.Encoder;

public class BlockPosition extends DataObject
{
    protected int x;

    protected int y;

    protected int z;

    public BlockPosition()
    {

    }

    public BlockPosition(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public int getX()
    {
        return x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public int getY()
    {
        return y;
    }

    public void setZ(int z)
    {
        this.z = z;
    }

    public int getZ()
    {
        return z;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeInt(this.x);
        encoder.writeInt(this.y);
        encoder.writeInt(this.z);
    }
}
