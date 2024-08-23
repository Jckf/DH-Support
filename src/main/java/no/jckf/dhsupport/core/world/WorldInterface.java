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

package no.jckf.dhsupport.core.world;

import no.jckf.dhsupport.core.configuration.Configurable;

import java.util.Map;
import java.util.UUID;

public interface WorldInterface extends Configurable
{
    WorldInterface newInstance();

    UUID getId();

    String getName();

    boolean chunkExists(int x, int z);

    int getMinY();

    int getMaxY();

    int getSeaLevel();

    int getHighestYAt(int x, int z);

    String getBiomeAt(int x, int z);

    String getMaterialAt(int x, int y, int z);

    String getBlockStateAsStringAt(int x, int y, int z);

    Map<String, String> getBlockPropertiesAt(int x, int y, int z);

    byte getBlockLightAt(int x, int y, int z);

    byte getSkyLightAt(int x, int y, int z);

    boolean isTransparent(int x, int y, int z);
}
