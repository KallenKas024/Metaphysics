package net.minecraft.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;

public class LayeredRegistryAccess<T> {
   private final List<T> keys;
   private final List<RegistryAccess.Frozen> values;
   private final RegistryAccess.Frozen composite;

   public LayeredRegistryAccess(List<T> pKeys) {
      this(pKeys, Util.make(() -> {
         RegistryAccess.Frozen[] aregistryaccess$frozen = new RegistryAccess.Frozen[pKeys.size()];
         Arrays.fill(aregistryaccess$frozen, RegistryAccess.EMPTY);
         return Arrays.asList(aregistryaccess$frozen);
      }));
   }

   private LayeredRegistryAccess(List<T> pKeys, List<RegistryAccess.Frozen> pValues) {
      this.keys = List.copyOf(pKeys);
      this.values = List.copyOf(pValues);
      this.composite = (new RegistryAccess.ImmutableRegistryAccess(collectRegistries(pValues.stream()))).freeze();
   }

   private int getLayerIndexOrThrow(T pKey) {
      int i = this.keys.indexOf(pKey);
      if (i == -1) {
         throw new IllegalStateException("Can't find " + pKey + " inside " + this.keys);
      } else {
         return i;
      }
   }

   public RegistryAccess.Frozen getLayer(T pKey) {
      int i = this.getLayerIndexOrThrow(pKey);
      return this.values.get(i);
   }

   public RegistryAccess.Frozen getAccessForLoading(T pKey) {
      int i = this.getLayerIndexOrThrow(pKey);
      return this.getCompositeAccessForLayers(0, i);
   }

   public RegistryAccess.Frozen getAccessFrom(T pKey) {
      int i = this.getLayerIndexOrThrow(pKey);
      return this.getCompositeAccessForLayers(i, this.values.size());
   }

   private RegistryAccess.Frozen getCompositeAccessForLayers(int pStartIndex, int pEndIndex) {
      return (new RegistryAccess.ImmutableRegistryAccess(collectRegistries(this.values.subList(pStartIndex, pEndIndex).stream()))).freeze();
   }

   public LayeredRegistryAccess<T> replaceFrom(T pKey, RegistryAccess.Frozen... pValues) {
      return this.replaceFrom(pKey, Arrays.asList(pValues));
   }

   public LayeredRegistryAccess<T> replaceFrom(T pKey, List<RegistryAccess.Frozen> pValues) {
      int i = this.getLayerIndexOrThrow(pKey);
      if (pValues.size() > this.values.size() - i) {
         throw new IllegalStateException("Too many values to replace");
      } else {
         List<RegistryAccess.Frozen> list = new ArrayList<>();

         for(int j = 0; j < i; ++j) {
            list.add(this.values.get(j));
         }

         list.addAll(pValues);

         while(list.size() < this.values.size()) {
            list.add(RegistryAccess.EMPTY);
         }

         return new LayeredRegistryAccess<>(this.keys, list);
      }
   }

   public RegistryAccess.Frozen compositeAccess() {
      return this.composite;
   }

   private static Map<ResourceKey<? extends Registry<?>>, Registry<?>> collectRegistries(Stream<? extends RegistryAccess> pAccesses) {
      Map<ResourceKey<? extends Registry<?>>, Registry<?>> map = new HashMap<>();
      pAccesses.forEach((p_252003_) -> {
         p_252003_.registries().forEach((p_250413_) -> {
            if (map.put(p_250413_.key(), p_250413_.value()) != null) {
               throw new IllegalStateException("Duplicated registry " + p_250413_.key());
            }
         });
      });
      return map;
   }
}