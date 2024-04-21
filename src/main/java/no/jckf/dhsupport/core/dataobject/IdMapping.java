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

package no.jckf.dhsupport.core.dataobject;

import no.jckf.dhsupport.core.bytestream.Encoder;

import java.nio.charset.StandardCharsets;

public class IdMapping extends DataObject
{
    protected static String separator1 = "_DH-BSW_";

    protected static String separator2 = "_STATE_";

    protected String biome;

    protected String block;

    public IdMapping(String biome, String block)
    {
        this.biome = biome;
        this.block = block;
    }

    @Override
    public void encode(Encoder encoder)
    {
        String serialized = this.biome + IdMapping.separator1 + this.block + IdMapping.separator2;

        // TODO: A writeUtf() method?
        encoder.writeShort(serialized.length());
        encoder.write(serialized.getBytes(StandardCharsets.UTF_8));
    }
}
