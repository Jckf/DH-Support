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
import no.jckf.dhsupport.core.bytestream.Encoder;

/**
 * 0: Rate limited
 * 1: Invalid level
 * 2: Invalid section position
 * 3: Request rejected
 */
public class ExceptionMessage extends TrackablePluginMessage
{
    protected int typeId;

    protected String message;

    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    public int getTypeId()
    {
        return typeId;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeInt(this.typeId);
        encoder.writeString(this.message);
    }

    @Override
    public void decode(Decoder decoder)
    {
        this.typeId = decoder.readInt();
        this.message = decoder.readString();
    }
}
