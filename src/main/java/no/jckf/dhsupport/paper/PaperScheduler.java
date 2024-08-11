package no.jckf.dhsupport.paper;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import no.jckf.dhsupport.core.scheduling.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PaperScheduler implements Scheduler
{
    protected JavaPlugin plugin;

    public PaperScheduler(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public <U> CompletableFuture<U> run(Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            future.complete(supplier.get());
        });

        return future;
    }

    @Override
    public <U> CompletableFuture<U> runGlobal(Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        Bukkit.getGlobalRegionScheduler().run(this.plugin, (task) -> {
            future.complete(supplier.get());
        });

        return future;
    }

    @Override
    public <U> CompletableFuture<U> runRegional(UUID worldId, int x, int z, Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        Bukkit.getRegionScheduler().run(this.plugin,
            this.plugin.getServer().getWorld(worldId),
            x,
            z,
            (task) -> future.complete(supplier.get())
        );

        return future;
    }
}
