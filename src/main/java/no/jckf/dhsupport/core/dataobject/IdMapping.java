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

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class IdMapping extends DataObject
{
    protected static String separator1 = "_DH-BSW_";

    protected static String separator2 = "_STATE_";

    protected String biome;

    protected String block;

    @Nullable
    protected Map<String, String> properties;

    public IdMapping(String biome, String block, @Nullable Map<String, String> properties)
    {
        this.biome = biome;
        this.block = block;
        this.properties = properties;
    }

    @Override
    public void encode(Encoder encoder)
    {
        StringBuilder propStringBuilder = new StringBuilder();

        if (this.properties != null) {
            for (String k : this.properties.keySet()) {
                propStringBuilder.append("{")
                    .append(k)
                    .append(":")
                    .append(this.properties.get(k))
                    .append("}");
            }
        }

        String serialized = this.biome + IdMapping.separator1 + this.block + (propStringBuilder.isEmpty() ? "" : IdMapping.separator2 + propStringBuilder);

        encoder.writeShortString(serialized);
    }
}
