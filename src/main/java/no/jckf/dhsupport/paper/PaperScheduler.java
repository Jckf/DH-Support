package no.jckf.dhsupport.paper;

import com.tcoded.folialib.FoliaLib;
import no.jckf.dhsupport.core.scheduling.Scheduler;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class PaperScheduler implements Scheduler
{
    protected JavaPlugin plugin;

    protected FoliaLib foliaLib;

    public PaperScheduler(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    protected FoliaLib getFoliaLib()
    {
        if (this.foliaLib == null) {
            this.foliaLib = new FoliaLib(this.plugin);
        }

        return this.foliaLib;
    }

    @Override
    public <U> CompletableFuture<U> run(Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        this.getFoliaLib().getScheduler().runAsync((task) -> {
            future.complete(supplier.get());
        });

        return future;
    }

    @Override
    public <U> CompletableFuture<U> runRegional(UUID worldId, int x, int z, Supplier<U> supplier)
    {
        CompletableFuture<U> future = new CompletableFuture<>();

        Location location = new Location(
            this.plugin.getServer().getWorld(worldId),
            x,
            0,
            z
        );

        this.getFoliaLib().getScheduler().runAtLocation(
            location,
            (task) -> future.complete(supplier.get())
        );

        return future;
    }
}
