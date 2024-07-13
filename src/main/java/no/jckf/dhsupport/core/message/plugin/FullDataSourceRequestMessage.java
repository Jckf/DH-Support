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

package no.jckf.dhsupport.core.message.plugin;

import no.jckf.dhsupport.core.bytestream.Decoder;
import no.jckf.dhsupport.core.dataobject.SectionPosition;

public class FullDataSourceRequestMessage extends TrackablePluginMessage
{
    protected String worldName;

    protected SectionPosition position;

    protected Long timestamp;

    public void setWorldName(String worldName)
    {
        this.worldName = worldName;
    }

    public String getWorldName()
    {
        return worldName;
    }

    public void setPosition(SectionPosition position)
    {
        this.position = position;
    }

    public SectionPosition getPosition()
    {
        return position;
    }

    public void setTimestamp(Long timestamp)
    {
        this.timestamp = timestamp;
    }

    public Long getTimestamp()
    {
        return timestamp;
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.worldName = decoder.readShortString();

        this.position = new SectionPosition();
        this.position.decode(decoder);

        this.timestamp = decoder.readOptional(decoder::readLong);
    }
}
