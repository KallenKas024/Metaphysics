package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public interface PreparableReloadListener {
   CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor);

   default String getName() {
      return this.getClass().getSimpleName();
   }

   public interface PreparationBarrier {
      <T> CompletableFuture<T> wait(T pBackgroundResult);
   }
}