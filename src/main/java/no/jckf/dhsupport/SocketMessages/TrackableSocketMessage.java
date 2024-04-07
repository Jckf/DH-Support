package no.jckf.dhsupport.SocketMessages;

public abstract class TrackableSocketMessage extends SocketMessage
{
    protected int tracker;

    public void setTracker(int tracker)
    {
        this.tracker = tracker;
    }

    public int getTracker()
    {
        return this.tracker;
    }
}
