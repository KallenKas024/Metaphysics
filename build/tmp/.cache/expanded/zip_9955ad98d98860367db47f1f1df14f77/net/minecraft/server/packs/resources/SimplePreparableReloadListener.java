package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.profiling.ProfilerFiller;

public abstract class SimplePreparableReloadListener<T> implements PreparableReloadListener {
   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      return CompletableFuture.supplyAsync(() -> {
         return this.prepare(pResourceManager, pPreparationsProfiler);
      }, pBackgroundExecutor).thenCompose(pStage::wait).thenAcceptAsync((p_10792_) -> {
         this.apply(p_10792_, pResourceManager, pReloadProfiler);
      }, pGameExecutor);
   }

   /**
    * Performs any reloading that can be done off-thread, such as file IO
    */
   protected abstract T prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler);

   /**
    * Applies the prepared sound event registrations and caches to the sound manager.
    * @param pObject The prepared sound event registrations and caches
    * @param pResourceManager The resource manager
    * @param pProfiler The profiler
    */
   protected abstract void apply(T pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler);
}