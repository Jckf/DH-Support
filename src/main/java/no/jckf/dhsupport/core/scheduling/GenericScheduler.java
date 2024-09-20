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

package no.jckf.dhsupport.core.scheduling;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class GenericScheduler implements Scheduler
{
    protected ExecutorService executor;

    public GenericScheduler(int threads)
    {
        this.executor = Executors.newFixedThreadPool(threads);
    }

    @Override
    public <U> CompletableFuture<U> run(Supplier<U> supplier)
    {
        return CompletableFuture.supplyAsync(supplier, this.executor);
    }

    @Override
    public <U> CompletableFuture<U> runRegional(UUID worldId, int x, int z, Supplier<U> supplier)
    {
        return this.run(supplier);
    }
}
