package no.jckf.dhsupport.SocketMessages;

import no.jckf.dhsupport.MessageWriter;

public class PartialUpdateSocketMessage extends SocketMessage
{
    protected int levelHashCode;

    protected int x;

    protected int z;

    @Override
    public void encode(MessageWriter writer)
    {
        writer.writeInt(this.levelHashCode);
        writer.writeInt(this.x);
        writer.writeInt(this.z);
        writer.writeInt(0); // Data length.
        //writer.write(...) // Data.
    }

    public void setLevelHashCode(int hashCode)
    {
        this.levelHashCode = hashCode;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setZ(int z)
    {
        this.z = z;
    }
}
