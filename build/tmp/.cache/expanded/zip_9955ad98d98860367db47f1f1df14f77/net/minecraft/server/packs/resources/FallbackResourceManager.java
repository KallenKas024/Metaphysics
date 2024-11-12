package net.minecraft.server.packs.resources;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class FallbackResourceManager implements ResourceManager {
   static final Logger LOGGER = LogUtils.getLogger();
   public final List<FallbackResourceManager.PackEntry> fallbacks = Lists.newArrayList();
   private final PackType type;
   private final String namespace;

   public FallbackResourceManager(PackType pType, String pNamespace) {
      this.type = pType;
      this.namespace = pNamespace;
   }

   public void push(PackResources pResources) {
      this.pushInternal(pResources.packId(), pResources, (Predicate<ResourceLocation>)null);
   }

   public void push(PackResources pResources, Predicate<ResourceLocation> pFilter) {
      this.pushInternal(pResources.packId(), pResources, pFilter);
   }

   public void pushFilterOnly(String pName, Predicate<ResourceLocation> pFilter) {
      this.pushInternal(pName, (PackResources)null, pFilter);
   }

   private void pushInternal(String pName, @Nullable PackResources pResources, @Nullable Predicate<ResourceLocation> pFilter) {
      this.fallbacks.add(new FallbackResourceManager.PackEntry(pName, pResources, pFilter));
   }

   public Set<String> getNamespaces() {
      return ImmutableSet.of(this.namespace);
   }

   public Optional<Resource> getResource(ResourceLocation pLocation) {
      for(int i = this.fallbacks.size() - 1; i >= 0; --i) {
         FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(i);
         PackResources packresources = fallbackresourcemanager$packentry.resources;
         if (packresources != null) {
            IoSupplier<InputStream> iosupplier = packresources.getResource(this.type, pLocation);
            if (iosupplier != null) {
               IoSupplier<ResourceMetadata> iosupplier1 = this.createStackMetadataFinder(pLocation, i);
               return Optional.of(createResource(packresources, pLocation, iosupplier, iosupplier1));
            }
         }

         if (fallbackresourcemanager$packentry.isFiltered(pLocation)) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", pLocation, fallbackresourcemanager$packentry.name);
            return Optional.empty();
         }
      }

      return Optional.empty();
   }

   private static Resource createResource(PackResources pSource, ResourceLocation pLocation, IoSupplier<InputStream> pStreamSupplier, IoSupplier<ResourceMetadata> pMetadataSupplier) {
      return new Resource(pSource, wrapForDebug(pLocation, pSource, pStreamSupplier), pMetadataSupplier);
   }

   private static IoSupplier<InputStream> wrapForDebug(ResourceLocation pLocation, PackResources pPackResources, IoSupplier<InputStream> p_249116_) {
      return LOGGER.isDebugEnabled() ? () -> {
         return new FallbackResourceManager.LeakedResourceWarningInputStream(p_249116_.get(), pLocation, pPackResources.packId());
      } : p_249116_;
   }

   public List<Resource> getResourceStack(ResourceLocation pLocation) {
      ResourceLocation resourcelocation = getMetadataLocation(pLocation);
      List<Resource> list = new ArrayList<>();
      boolean flag = false;
      String s = null;

      for(int i = this.fallbacks.size() - 1; i >= 0; --i) {
         FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(i);
         PackResources pack = fallbackresourcemanager$packentry.resources;
         if (pack != null) {
            var children = pack.getChildren();
            var packs = children == null ? List.of(pack) : children;
            for (final PackResources packresources : packs) {
            IoSupplier<InputStream> iosupplier = packresources.getResource(this.type, pLocation);
            if (iosupplier != null) {
               IoSupplier<ResourceMetadata> iosupplier1;
               if (flag) {
                  iosupplier1 = ResourceMetadata.EMPTY_SUPPLIER;
               } else {
                  iosupplier1 = () -> {
                     IoSupplier<InputStream> iosupplier2 = packresources.getResource(this.type, resourcelocation);
                     return iosupplier2 != null ? parseMetadata(iosupplier2) : ResourceMetadata.EMPTY;
                  };
               }

               list.add(new Resource(packresources, iosupplier, iosupplier1));
            }
            }
         }

         if (fallbackresourcemanager$packentry.isFiltered(pLocation)) {
            s = fallbackresourcemanager$packentry.name;
            break;
         }

         if (fallbackresourcemanager$packentry.isFiltered(resourcelocation)) {
            flag = true;
         }
      }

      if (list.isEmpty() && s != null) {
         LOGGER.warn("Resource {} not found, but was filtered by pack {}", pLocation, s);
      }

      return Lists.reverse(list);
   }

   private static boolean isMetadata(ResourceLocation pLocation) {
      return pLocation.getPath().endsWith(".mcmeta");
   }

   private static ResourceLocation getResourceLocationFromMetadata(ResourceLocation pMetadataResourceLocation) {
      String s = pMetadataResourceLocation.getPath().substring(0, pMetadataResourceLocation.getPath().length() - ".mcmeta".length());
      return pMetadataResourceLocation.withPath(s);
   }

   static ResourceLocation getMetadataLocation(ResourceLocation pLocation) {
      return pLocation.withPath(pLocation.getPath() + ".mcmeta");
   }

   public Map<ResourceLocation, Resource> listResources(String pPath, Predicate<ResourceLocation> pFilter) {
      record ResourceWithSourceAndIndex(PackResources packResources, IoSupplier<InputStream> resource, int packIndex) {
      }
      Map<ResourceLocation, ResourceWithSourceAndIndex> map = new HashMap<>();
      Map<ResourceLocation, ResourceWithSourceAndIndex> map1 = new HashMap<>();
      int i = this.fallbacks.size();

      for(int j = 0; j < i; ++j) {
         FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(j);
         fallbackresourcemanager$packentry.filterAll(map.keySet());
         fallbackresourcemanager$packentry.filterAll(map1.keySet());
         PackResources packresources = fallbackresourcemanager$packentry.resources;
         if (packresources != null) {
            int k = j;
            packresources.listResources(this.type, this.namespace, pPath, (p_248254_, p_248255_) -> {
               if (isMetadata(p_248254_)) {
                  if (pFilter.test(getResourceLocationFromMetadata(p_248254_))) {
                     map1.put(p_248254_, new ResourceWithSourceAndIndex(packresources, p_248255_, k));
                  }
               } else if (pFilter.test(p_248254_)) {
                  map.put(p_248254_, new ResourceWithSourceAndIndex(packresources, p_248255_, k));
               }

            });
         }
      }

      Map<ResourceLocation, Resource> map2 = Maps.newTreeMap();
      map.forEach((p_248258_, p_248259_) -> {
         ResourceLocation resourcelocation = getMetadataLocation(p_248258_);
         ResourceWithSourceAndIndex fallbackresourcemanager$1resourcewithsourceandindex = map1.get(resourcelocation);
         IoSupplier<ResourceMetadata> iosupplier;
         if (fallbackresourcemanager$1resourcewithsourceandindex != null && fallbackresourcemanager$1resourcewithsourceandindex.packIndex >= p_248259_.packIndex) {
            iosupplier = convertToMetadata(fallbackresourcemanager$1resourcewithsourceandindex.resource);
         } else {
            iosupplier = ResourceMetadata.EMPTY_SUPPLIER;
         }

         map2.put(p_248258_, createResource(p_248259_.packResources, p_248258_, p_248259_.resource, iosupplier));
      });
      return map2;
   }

   private IoSupplier<ResourceMetadata> createStackMetadataFinder(ResourceLocation pLocation, int pFallbackIndex) {
      return () -> {
         ResourceLocation resourcelocation = getMetadataLocation(pLocation);

         for(int i = this.fallbacks.size() - 1; i >= pFallbackIndex; --i) {
            FallbackResourceManager.PackEntry fallbackresourcemanager$packentry = this.fallbacks.get(i);
            PackResources packresources = fallbackresourcemanager$packentry.resources;
            if (packresources != null) {
               IoSupplier<InputStream> iosupplier = packresources.getResource(this.type, resourcelocation);
               if (iosupplier != null) {
                  return parseMetadata(iosupplier);
               }
            }

            if (fallbackresourcemanager$packentry.isFiltered(resourcelocation)) {
               break;
            }
         }

         return ResourceMetadata.EMPTY;
      };
   }

   private static IoSupplier<ResourceMetadata> convertToMetadata(IoSupplier<InputStream> pStreamSupplier) {
      return () -> {
         return parseMetadata(pStreamSupplier);
      };
   }

   private static ResourceMetadata parseMetadata(IoSupplier<InputStream> pStreamSupplier) throws IOException {
      try (InputStream inputstream = pStreamSupplier.get()) {
         return ResourceMetadata.fromJsonStream(inputstream);
      }
   }

   private static void applyPackFiltersToExistingResources(FallbackResourceManager.PackEntry pPackEntry, Map<ResourceLocation, FallbackResourceManager.EntryStack> pResources) {
      for(FallbackResourceManager.EntryStack fallbackresourcemanager$entrystack : pResources.values()) {
         if (pPackEntry.isFiltered(fallbackresourcemanager$entrystack.fileLocation)) {
            fallbackresourcemanager$entrystack.fileSources.clear();
         } else if (pPackEntry.isFiltered(fallbackresourcemanager$entrystack.metadataLocation())) {
            fallbackresourcemanager$entrystack.metaSources.clear();
         }
      }

   }

   private void listPackResources(FallbackResourceManager.PackEntry pEntry, String pPath, Predicate<ResourceLocation> pFilter, Map<ResourceLocation, FallbackResourceManager.EntryStack> pOutput) {
      PackResources packresources = pEntry.resources;
      if (packresources != null) {
         packresources.listResources(this.type, this.namespace, pPath, (p_248266_, p_248267_) -> {
            if (isMetadata(p_248266_)) {
               ResourceLocation resourcelocation = getResourceLocationFromMetadata(p_248266_);
               if (!pFilter.test(resourcelocation)) {
                  return;
               }

               (pOutput.computeIfAbsent(resourcelocation, FallbackResourceManager.EntryStack::new)).metaSources.put(packresources, p_248267_);
            } else {
               if (!pFilter.test(p_248266_)) {
                  return;
               }

               (pOutput.computeIfAbsent(p_248266_, FallbackResourceManager.EntryStack::new)).fileSources.add(new FallbackResourceManager.ResourceWithSource(packresources, p_248267_));
            }

         });
      }
   }

   public Map<ResourceLocation, List<Resource>> listResourceStacks(String pPath, Predicate<ResourceLocation> pFilter) {
      Map<ResourceLocation, FallbackResourceManager.EntryStack> map = Maps.newHashMap();

      for(FallbackResourceManager.PackEntry fallbackresourcemanager$packentry : this.fallbacks) {
         applyPackFiltersToExistingResources(fallbackresourcemanager$packentry, map);
         this.listPackResources(fallbackresourcemanager$packentry, pPath, pFilter, map);
      }

      TreeMap<ResourceLocation, List<Resource>> treemap = Maps.newTreeMap();

      for(FallbackResourceManager.EntryStack fallbackresourcemanager$entrystack : map.values()) {
         if (!fallbackresourcemanager$entrystack.fileSources.isEmpty()) {
            List<Resource> list = new ArrayList<>();

            for(FallbackResourceManager.ResourceWithSource fallbackresourcemanager$resourcewithsource : fallbackresourcemanager$entrystack.fileSources) {
               PackResources packresources = fallbackresourcemanager$resourcewithsource.source;
               IoSupplier<InputStream> iosupplier = fallbackresourcemanager$entrystack.metaSources.get(packresources);
               IoSupplier<ResourceMetadata> iosupplier1 = iosupplier != null ? convertToMetadata(iosupplier) : ResourceMetadata.EMPTY_SUPPLIER;
               list.add(createResource(packresources, fallbackresourcemanager$entrystack.fileLocation, fallbackresourcemanager$resourcewithsource.resource, iosupplier1));
            }

            treemap.put(fallbackresourcemanager$entrystack.fileLocation, list);
         }
      }

      return treemap;
   }

   public Stream<PackResources> listPacks() {
      return this.fallbacks.stream().map((p_215386_) -> {
         return p_215386_.resources;
      }).filter(Objects::nonNull);
   }

   static record EntryStack(ResourceLocation fileLocation, ResourceLocation metadataLocation, List<FallbackResourceManager.ResourceWithSource> fileSources, Map<PackResources, IoSupplier<InputStream>> metaSources) {
      EntryStack(ResourceLocation pFileLocation) {
         this(pFileLocation, FallbackResourceManager.getMetadataLocation(pFileLocation), new ArrayList<>(), new Object2ObjectArrayMap<>());
      }
   }

   static class LeakedResourceWarningInputStream extends FilterInputStream {
      private final Supplier<String> message;
      private boolean closed;

      public LeakedResourceWarningInputStream(InputStream pInputStream, ResourceLocation pResourceLocation, String pPackName) {
         super(pInputStream);
         Exception exception = new Exception("Stacktrace");
         this.message = () -> {
            StringWriter stringwriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringwriter));
            return "Leaked resource: '" + pResourceLocation + "' loaded from pack: '" + pPackName + "'\n" + stringwriter;
         };
      }

      public void close() throws IOException {
         super.close();
         this.closed = true;
      }

      protected void finalize() throws Throwable {
         if (!this.closed) {
            FallbackResourceManager.LOGGER.warn("{}", this.message.get());
         }

         super.finalize();
      }
   }

   static record PackEntry(String name, @Nullable PackResources resources, @Nullable Predicate<ResourceLocation> filter) {
      public void filterAll(Collection<ResourceLocation> pLocations) {
         if (this.filter != null) {
            pLocations.removeIf(this.filter);
         }

      }

      public boolean isFiltered(ResourceLocation pLocation) {
         return this.filter != null && this.filter.test(pLocation);
      }
   }

   static record ResourceWithSource(PackResources source, IoSupplier<InputStream> resource) {
   }
}
