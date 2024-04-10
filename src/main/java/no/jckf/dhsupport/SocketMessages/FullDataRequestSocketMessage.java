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

package no.jckf.dhsupport.SocketMessages;

import no.jckf.dhsupport.MessageReader;

public class FullDataRequestSocketMessage extends TrackableSocketMessage
{
    protected int levelHasCode;

    protected byte detailLevel;

    protected int x;

    protected int z;

    protected int checksum;

    @Override
    public void decode(MessageReader reader)
    {
        this.levelHasCode = reader.readInt();
        this.detailLevel = reader.readByte();
        this.x = reader.readInt();
        this.z = reader.readInt();
        this.checksum = reader.readOptional(reader::readInt);
    }
}
