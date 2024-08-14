package no.jckf.dhsupport.bukkit;

import no.jckf.dhsupport.core.scheduling.Scheduler;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class BukkitScheduler implements Scheduler
{
    protected ExecutorService executor;

    public BukkitScheduler()
    {
        this.executor = Executors.newFixedThreadPool(4);
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
