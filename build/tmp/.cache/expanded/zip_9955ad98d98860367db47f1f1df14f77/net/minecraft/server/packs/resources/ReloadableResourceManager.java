package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.slf4j.Logger;

public class ReloadableResourceManager implements ResourceManager, AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private CloseableResourceManager resources;
   private final List<PreparableReloadListener> listeners = Lists.newArrayList();
   private final PackType type;

   public ReloadableResourceManager(PackType pType) {
      this.type = pType;
      this.resources = new MultiPackResourceManager(pType, List.of());
   }

   public void close() {
      this.resources.close();
   }

   public void registerReloadListener(PreparableReloadListener pListener) {
      this.listeners.add(pListener);
   }

   public ReloadInstance createReload(Executor pBackgroundExecutor, Executor pGameExecutor, CompletableFuture<Unit> pWaitingFor, List<PackResources> pResourcePacks) {
      LOGGER.info("Reloading ResourceManager: {}", LogUtils.defer(() -> {
         return pResourcePacks.stream().map(PackResources::packId).collect(Collectors.joining(", "));
      }));
      this.resources.close();
      this.resources = new MultiPackResourceManager(this.type, pResourcePacks);
      return SimpleReloadInstance.create(this.resources, this.listeners, pBackgroundExecutor, pGameExecutor, pWaitingFor, LOGGER.isDebugEnabled());
   }

   public Optional<Resource> getResource(ResourceLocation pLocation) {
      return this.resources.getResource(pLocation);
   }

   public Set<String> getNamespaces() {
      return this.resources.getNamespaces();
   }

   public List<Resource> getResourceStack(ResourceLocation pLocation) {
      return this.resources.getResourceStack(pLocation);
   }

   public Map<ResourceLocation, Resource> listResources(String pPath, Predicate<ResourceLocation> pFilter) {
      return this.resources.listResources(pPath, pFilter);
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String pPath, Predicate<ResourceLocation> pFilter) {
      return this.resources.listResourceStacks(pPath, pFilter);
   }

   public Stream<PackResources> listPacks() {
      return this.resources.listPacks();
   }

   public void registerReloadListenerIfNotPresent(PreparableReloadListener listener) {
      if (!this.listeners.contains(listener)) {
         this.registerReloadListener(listener);
      }
   }
}
