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

package no.jckf.dhsupport.core.configuration;

import java.util.HashMap;
import java.util.Map;

public class Configuration
{
    protected Map<String, Object> variables = new HashMap<>();

    public void set(String key, Object value)
    {
        this.variables.put(key, value);
    }

    public Object get(String key)
    {
        return this.variables.get(key);
    }

    public boolean getBool(String key)
    {
        return (boolean) this.get(key);
    }

    public Integer getInt(String key)
    {
        return (Integer) this.get(key);
    }

    public String getString(String key)
    {
        return (String) this.get(key);
    }
}
