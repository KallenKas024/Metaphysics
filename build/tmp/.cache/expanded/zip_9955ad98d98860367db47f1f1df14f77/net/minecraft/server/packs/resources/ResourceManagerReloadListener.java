package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

public interface ResourceManagerReloadListener extends PreparableReloadListener {
   default CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      return pStage.wait(Unit.INSTANCE).thenRunAsync(() -> {
         pReloadProfiler.startTick();
         pReloadProfiler.push("listener");
         this.onResourceManagerReload(pResourceManager);
         pReloadProfiler.pop();
         pReloadProfiler.endTick();
      }, pGameExecutor);
   }

   void onResourceManagerReload(ResourceManager pResourceManager);
}