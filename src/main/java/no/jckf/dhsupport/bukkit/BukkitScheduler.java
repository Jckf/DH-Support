package no.jckf.dhsupport.bukkit;

import com.tcoded.folialib.FoliaLib;
import no.jckf.dhsupport.core.configuration.DhsConfig;
import no.jckf.dhsupport.core.scheduling.Scheduler;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class BukkitScheduler implements Scheduler
{
    protected DhSupportBukkitPlugin plugin;

    protected FoliaLib foliaLib;

    protected Executor executor;

    public BukkitScheduler(DhSupportBukkitPlugin plugin)
    {
        this.plugin = plugin;

        this.foliaLib = new FoliaLib(this.plugin);

        this.executor = Executors.newFixedThreadPool(this.plugin.getDhSupport().getConfig().getInt(DhsConfig.SCHEDULER_THREADS));
    }

    @Override
    public <U> CompletableFuture<U> runOnMainThread(Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        this.foliaLib.getScheduler().runNextTick((task) -> {
            try {
                future.complete(supplier.get());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }

    @Override
    public <U> CompletableFuture<U> runOnRegionThread(UUID worldId, int x, int z, Supplier<U> supplier)
    {
        // We can read most things async on normal Bukkit/Spigot/Paper implementations.
        if (!this.foliaLib.isFolia()) {
            return this.runOnSeparateThread(supplier);
        }

        CompletableFuture<U> future = new CompletableFuture<>();

        Location location = new Location(
            this.plugin.getServer().getWorld(worldId),
            x,
            0,
            z
        );

        this.foliaLib.getScheduler().runAtLocation(
            location,
            (task) -> {
                try {
                    future.complete(supplier.get());
                } catch (Exception exception) {
                    future.completeExceptionally(exception);
                }
            }
        );

        return future;
    }

    @Override
    public <U> CompletableFuture<U> runOnSeparateThread(Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        this.executor.execute(() -> {
            try {
                future.complete(supplier.get());
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });

        return future;
    }
}
