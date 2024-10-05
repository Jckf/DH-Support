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

package no.jckf.dhsupport.core.database.repositories;

import no.jckf.dhsupport.core.database.Database;
import no.jckf.dhsupport.core.database.models.LodModel;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncLodRepository extends LodRepository
{
    protected interface Task<T>
    {
        T run();
    }

    protected Executor executor = Executors.newSingleThreadExecutor();

    public AsyncLodRepository(Database database)
    {
        super(database);
    }

    protected <T> CompletableFuture<T> queueTask(Task<T> task)
    {
        CompletableFuture<T> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            try {
                future.complete(task.run());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    public CompletableFuture<LodModel> saveLodAsync(UUID worldId, int sectionX, int sectionZ, byte[] data, byte[] beacons)
    {
        return this.queueTask(() -> this.saveLod(worldId, sectionX, sectionZ, data, beacons));
    }

    public CompletableFuture<LodModel> loadLodAsync(UUID worldId, int sectionX, int sectionZ)
    {
        return this.queueTask(() -> this.loadLod(worldId, sectionX, sectionZ));
    }

    public CompletableFuture<Boolean> lodExistsAsync(UUID worldId, int sectionX, int sectionZ)
    {
        return this.queueTask(() -> this.lodExists(worldId, sectionX, sectionZ));
    }

    public CompletableFuture<Boolean> deleteLodAsync(UUID worldId, int sectionX, int sectionZ)
    {
        return this.queueTask(() -> this.deleteLod(worldId, sectionX, sectionZ));
    }
}
