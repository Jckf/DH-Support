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
