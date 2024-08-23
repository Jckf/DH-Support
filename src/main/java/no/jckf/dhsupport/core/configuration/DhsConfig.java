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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class DhsConfig
{
    public static String RENDER_DISTANCE = "render_distance";

    public static String DISTANT_GENERATION_ENABLED = "distant_generation_enabled";

    public static String FULL_DATA_REQUEST_CONCURRENCY_LIMIT = "full_data_request_concurrency_limit";

    public static String REAL_TIME_UPDATES_ENABLED = "real_time_updates_enabled";

    public static String LOGIN_DATA_SYNC_ENABLED = "login_data_sync_enabled";

    public static String LOGIN_DATA_SYNC_RC_LIMIT = "login_data_sync_rc_limit";

    public static String GENERIC_SCHEDULER_THREADS = "generic_scheduler_threads";

    public static String GENERATE_NEW_CHUNKS = "generate_new_chunks";

    public static String LEVEL_KEY_PREFIX = "level_key_prefix";

    public static String BORDER_CENTER = "border_center";

    public static String BORDER_RADIUS = "border_radius";

    public static Collection<String> getKeys()
    {
        List<String> keys = new ArrayList<>();

        Arrays.stream(DhsConfig.class.getDeclaredFields())
                .filter((field) -> Modifier.isStatic(field.getModifiers()))
                .forEach((field) -> {
                    try {
                        keys.add((String) field.get(DhsConfig.class));
                    } catch (IllegalAccessException exception) {
                        throw new RuntimeException(exception);
                    }
                });

        return keys;
    }
}
