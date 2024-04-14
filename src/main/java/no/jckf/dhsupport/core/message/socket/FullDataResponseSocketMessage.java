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

package no.jckf.dhsupport.core.message.socket;

import no.jckf.dhsupport.core.bytestream.Encoder;

public class FullDataResponseSocketMessage extends TrackableSocketMessage
{
    protected static final byte formatVersion = 3;

    @Override
    public void encode(Encoder encoder)
    {
        encoder.writeBoolean(true); // True if data is available. False otherwise.
        encoder.writeByte(FullDataResponseSocketMessage.formatVersion);
        encoder.writeInt(0); // Compressed data length
        // TODO: LZ4 compressed data.
        // Source summary info:
        //     int level min y
        // Data points:
        //     int "data guard byte"
        //     foreach x,z:
        //         int data point count
        //     int "data guard byte"
        //     foreach x,z:
        //         foreach points:
        //             long pont
        // ID mappings:
        //     int "data guard byte"
        //     int entry count
        //     foreach entries:
        //         string entry (biome (resource ns + ":" + resource path) + "_DH-BSW_" + block state (resource ns + ":" + source path + "_STATE_" + block state props (key:value pairs wrapped in curlies)))
    }
}
