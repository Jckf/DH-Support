/*
 * DH Support, server-side support for Distant Horizons.
 * Copyright (C) 2024 Jim C K Flaten
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package no.jckf.dhsupport.Messages.Socket;

import no.jckf.dhsupport.ByteStream.Decoder;
import no.jckf.dhsupport.DataObjects.SectionPosition;

import java.util.Collection;

public class GenerationTaskPriorityRequest extends TrackableSocketMessage
{
    protected int levelHashCode;

    protected Collection<SectionPosition> sectionPositions;

    @Override
    public void decode(Decoder decoder)
    {
        this.levelHashCode = decoder.readInt();
        this.sectionPositions = decoder.readCollection(SectionPosition.class);
    }

    public void setLevelHashCode(int hashCode)
    {
        this.levelHashCode = hashCode;
    }

    public int getLevelHashCode()
    {
        return this.levelHashCode;
    }

    public void setSectionPositions(Collection<SectionPosition> sectionPositions)
    {
        this.sectionPositions = sectionPositions;
    }

    public Collection<SectionPosition> getSectionPositions()
    {
        return this.sectionPositions;
    }
}
