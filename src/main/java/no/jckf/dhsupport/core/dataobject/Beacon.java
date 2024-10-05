package no.jckf.dhsupport.core.dataobject;

import no.jckf.dhsupport.core.bytestream.Encoder;

public class Beacon extends DataObject
{
    protected BlockPosition position;

    protected int color;

    public Beacon()
    {

    }

    public Beacon(BlockPosition position, int color)
    {
        this.position = position;
        this.color = color;
    }

    public Beacon(int x, int y, int z, int color)
    {
        this(new BlockPosition(x, y, z), color);
    }

    @Override
    public void encode(Encoder encoder)
    {
        this.position.encode(encoder);

        encoder.writeInt(this.color);
    }
}
