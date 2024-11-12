package net.minecraft.server.packs.resources;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class MultiPackResourceManager implements CloseableResourceManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<String, FallbackResourceManager> namespacedManagers;
   private final List<PackResources> packs;

   public MultiPackResourceManager(PackType pType, List<PackResources> pPacks) {
      this.packs = List.copyOf(pPacks);
      Map<String, FallbackResourceManager> map = new HashMap<>();
      List<String> list = pPacks.stream().flatMap((p_215471_) -> {
         return p_215471_.getNamespaces(pType).stream();
      }).distinct().toList();

      for(PackResources packresources : pPacks) {
         ResourceFilterSection resourcefiltersection = this.getPackFilterSection(packresources);
         Set<String> set = packresources.getNamespaces(pType);
         Predicate<ResourceLocation> predicate = resourcefiltersection != null ? (p_215474_) -> {
            return resourcefiltersection.isPathFiltered(p_215474_.getPath());
         } : null;

         for(String s : list) {
            boolean flag = set.contains(s);
            boolean flag1 = resourcefiltersection != null && resourcefiltersection.isNamespaceFiltered(s);
            if (flag || flag1) {
               FallbackResourceManager fallbackresourcemanager = map.get(s);
               if (fallbackresourcemanager == null) {
                  fallbackresourcemanager = new FallbackResourceManager(pType, s);
                  map.put(s, fallbackresourcemanager);
               }

               if (flag && flag1) {
                  fallbackresourcemanager.push(packresources, predicate);
               } else if (flag) {
                  fallbackresourcemanager.push(packresources);
               } else {
                  fallbackresourcemanager.pushFilterOnly(packresources.packId(), predicate);
               }
            }
         }
      }

      this.namespacedManagers = map;
   }

   @Nullable
   private ResourceFilterSection getPackFilterSection(PackResources pPackResources) {
      try {
         return pPackResources.getMetadataSection(ResourceFilterSection.TYPE);
      } catch (IOException ioexception) {
         LOGGER.error("Failed to get filter section from pack {}", (Object)pPackResources.packId());
         return null;
      }
   }

   public Set<String> getNamespaces() {
      return this.namespacedManagers.keySet();
   }

   public Optional<Resource> getResource(ResourceLocation pLocation) {
      ResourceManager resourcemanager = this.namespacedManagers.get(pLocation.getNamespace());
      return resourcemanager != null ? resourcemanager.getResource(pLocation) : Optional.empty();
   }

   public List<Resource> getResourceStack(ResourceLocation pLocation) {
      ResourceManager resourcemanager = this.namespacedManagers.get(pLocation.getNamespace());
      return resourcemanager != null ? resourcemanager.getResourceStack(pLocation) : List.of();
   }

   public Map<ResourceLocation, Resource> listResources(String pPath, Predicate<ResourceLocation> pFilter) {
      checkTrailingDirectoryPath(pPath);
      Map<ResourceLocation, Resource> map = new TreeMap<>();

      for(FallbackResourceManager fallbackresourcemanager : this.namespacedManagers.values()) {
         map.putAll(fallbackresourcemanager.listResources(pPath, pFilter));
      }

      return map;
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String pPath, Predicate<ResourceLocation> pFilter) {
      checkTrailingDirectoryPath(pPath);
      Map<ResourceLocation, List<Resource>> map = new TreeMap<>();

      for(FallbackResourceManager fallbackresourcemanager : this.namespacedManagers.values()) {
         map.putAll(fallbackresourcemanager.listResourceStacks(pPath, pFilter));
      }

      return map;
   }

   private static void checkTrailingDirectoryPath(String pPath) {
      if (pPath.endsWith("/")) {
         throw new IllegalArgumentException("Trailing slash in path " + pPath);
      }
   }

   public Stream<PackResources> listPacks() {
      return this.packs.stream();
   }

   public void close() {
      this.packs.forEach(PackResources::close);
   }
}