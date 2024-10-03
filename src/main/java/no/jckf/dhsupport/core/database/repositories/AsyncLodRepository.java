package no.jckf.dhsupport.core.database.repositories;

import no.jckf.dhsupport.core.database.Database;
import no.jckf.dhsupport.core.database.models.LodModel;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AsyncLodRepository extends LodRepository
{
    protected Executor executor = Executors.newSingleThreadExecutor();

    public AsyncLodRepository(Database database)
    {
        super(database);
    }

    protected void queueTask(Runnable runnable)
    {
        this.executor.execute(runnable);
    }

    public CompletableFuture<LodModel> loadLodAsync(UUID worldId, int sectionX, int sectionZ)
    {
        CompletableFuture<LodModel> future = new CompletableFuture<>();

        this.queueTask(() -> {
            future.complete(this.loadLod(worldId, sectionX, sectionZ));
        });

        return future;
    }
}
