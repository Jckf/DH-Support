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

package no.jckf.dhsupport.core.event;

import java.util.*;

public class EventBus<E>
{
    @SuppressWarnings("rawtypes")
    protected Map<Class, List<EventHandler>> handlers = new HashMap<>();

    public <T extends E> void registerHandler(Class<T> eventClass, EventHandler<T> handler)
    {
        if (!this.handlers.containsKey(eventClass)) {
            this.handlers.put(eventClass, new ArrayList<>());
        }

        this.handlers.get(eventClass).add(handler);
    }

    public <T extends E> void unregisterHandler(Class<T> eventClass, EventHandler<T> handler)
    {
        if (!this.handlers.containsKey(eventClass)) {
            return;
        }

        this.handlers.get(eventClass).remove(handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends E> void dispatch(T event)
    {
        if (!this.handlers.containsKey(event.getClass())) {
            return;
        }

        this.handlers.get(event.getClass()).forEach((handler) -> handler.handle(event));
    }
}
