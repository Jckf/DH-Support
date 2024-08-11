package no.jckf.dhsupport.core.scheduling;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Scheduler
{
    <U> CompletableFuture<U> run(Supplier<U> supplier);

    <U> CompletableFuture<U> runGlobal(Supplier<U> supplier);

    <U> CompletableFuture<U> runRegional(UUID worldId, int x, int z, Supplier<U> supplier);
}
