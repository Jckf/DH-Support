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

package no.jckf.dhsupport;

import java.util.ArrayList;
import java.util.List;

public class MessageTypeRegistry
{
    protected List<Class<? extends DhMessage>> messageTypes = new ArrayList<>();

    public void registerMessageType(int id, Class<? extends DhMessage> messageClass)
    {
        this.messageTypes.add(id, messageClass);
    }

    public Class<? extends DhMessage> getMessageType(int id)
    {
        return this.messageTypes.get(id);
    }
}