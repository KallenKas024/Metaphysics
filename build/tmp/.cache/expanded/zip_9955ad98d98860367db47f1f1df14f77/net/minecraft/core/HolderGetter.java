package net.minecraft.core;

import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface HolderGetter<T> {
   Optional<Holder.Reference<T>> get(ResourceKey<T> pResourceKey);

   default Holder.Reference<T> getOrThrow(ResourceKey<T> pResourceKey) {
      return this.get(pResourceKey).orElseThrow(() -> {
         return new IllegalStateException("Missing element " + pResourceKey);
      });
   }

   Optional<HolderSet.Named<T>> get(TagKey<T> pTagKey);

   default HolderSet.Named<T> getOrThrow(TagKey<T> pTagKey) {
      return this.get(pTagKey).orElseThrow(() -> {
         return new IllegalStateException("Missing tag " + pTagKey);
      });
   }

   public interface Provider {
      <T> Optional<HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> pRegistryKey);

      default <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> pRegistryKey) {
         return this.lookup(pRegistryKey).orElseThrow(() -> {
            return new IllegalStateException("Registry " + pRegistryKey.location() + " not found");
         });
      }
   }
}