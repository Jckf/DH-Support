package no.jckf.dhsupport.core.lodbuilders;

import no.jckf.dhsupport.core.dataobject.Lod;
import no.jckf.dhsupport.core.dataobject.SectionPosition;
import no.jckf.dhsupport.core.world.WorldInterface;

public abstract class LodBuilder
{
    protected WorldInterface worldInterface;

    protected SectionPosition position;

    public LodBuilder(WorldInterface worldInterface, SectionPosition position)
    {
        this.worldInterface = worldInterface;
        this.position = position;
    }

    public abstract Lod generate();
}
