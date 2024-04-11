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

package no.jckf.dhsupport.ByteStream;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class Decoder
{
    protected ByteArrayDataInput input;

    public Decoder(byte[] data)
    {
        this.input = ByteStreams.newDataInput(data);
    }

    public byte readByte()
    {
        return this.input.readByte();
    }

    public boolean readBoolean()
    {
        return this.input.readBoolean();
    }

    public short readShort()
    {
        return this.input.readShort();
    }

    public int readInt()
    {
        return this.input.readInt();
    }

    public long readLong()
    {
        return this.input.readLong();
    }

    public String readString()
    {
        byte[] chars = new byte[this.input.readInt()];
        this.input.readFully(chars, 0, chars.length);
        return new String(chars, StandardCharsets.UTF_8);
    }

    @Nullable
    public <T> T readOptional(Supplier<T> supplier)
    {
        if (this.readBoolean()) {
            return supplier.get();
        }

        return null;
    }

    @Nullable
    public <T> T readObject(Class<T> className)
    {
        T instance;

        try {
            instance = className.getConstructor().newInstance();
        } catch (Exception exception) {
            return null;
        }

        if (instance instanceof Decodable) {
            ((Decodable) instance).decode(this);
        } else {
            // TODO: Decoders for primitives.
        }

        return instance;
    }

    public <T> Collection<T> readCollection(Class<T> className)
    {
        int size = this.readInt();

        List<T> items = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            T item = this.readObject(className);

            items.add(item);
        }

        return items;
    }
}
