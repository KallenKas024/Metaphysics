package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.util.ExtraCodecs;

public class RegistryOps<T> extends DelegatingOps<T> {
   private final RegistryOps.RegistryInfoLookup lookupProvider;

   private static RegistryOps.RegistryInfoLookup memoizeLookup(final RegistryOps.RegistryInfoLookup pLookupProvider) {
      return new RegistryOps.RegistryInfoLookup() {
         private final Map<ResourceKey<? extends Registry<?>>, Optional<? extends RegistryOps.RegistryInfo<?>>> lookups = new HashMap<>();

         public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> p_256043_) {
            return (Optional<RegistryOps.RegistryInfo<T>>) this.lookups.computeIfAbsent(p_256043_, pLookupProvider::lookup);
         }
      };
   }

   public static <T> RegistryOps<T> create(DynamicOps<T> pDelegate, final HolderLookup.Provider pRegistries) {
      return create(pDelegate, memoizeLookup(new RegistryOps.RegistryInfoLookup() {
         public <E> Optional<RegistryOps.RegistryInfo<E>> lookup(ResourceKey<? extends Registry<? extends E>> p_256323_) {
            return pRegistries.lookup(p_256323_).map((p_258224_) -> {
               return new RegistryOps.RegistryInfo<>(p_258224_, p_258224_, p_258224_.registryLifecycle());
            });
         }
      }));
   }

   public static <T> RegistryOps<T> create(DynamicOps<T> pDelegate, RegistryOps.RegistryInfoLookup pLookupProvider) {
      return new RegistryOps<>(pDelegate, pLookupProvider);
   }

   private RegistryOps(DynamicOps<T> pDelegate, RegistryOps.RegistryInfoLookup pLookupProvider) {
      super(pDelegate);
      this.lookupProvider = pLookupProvider;
   }

   public <E> Optional<HolderOwner<E>> owner(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
      return this.lookupProvider.lookup(pRegistryKey).map(RegistryOps.RegistryInfo::owner);
   }

   public <E> Optional<HolderGetter<E>> getter(ResourceKey<? extends Registry<? extends E>> pRegistryKey) {
      return this.lookupProvider.lookup(pRegistryKey).map(RegistryOps.RegistryInfo::getter);
   }

   public static <E, O> RecordCodecBuilder<O, HolderGetter<E>> retrieveGetter(ResourceKey<? extends Registry<? extends E>> pRegistryOps) {
      return ExtraCodecs.retrieveContext((p_274811_) -> {
         if (p_274811_ instanceof RegistryOps<?> registryops) {
            return registryops.lookupProvider.lookup(pRegistryOps).map((p_255527_) -> {
               return DataResult.success(p_255527_.getter(), p_255527_.elementsLifecycle());
            }).orElseGet(() -> {
               return DataResult.error(() -> {
                  return "Unknown registry: " + pRegistryOps;
               });
            });
         } else {
            return DataResult.error(() -> {
               return "Not a registry ops";
            });
         }
      }).forGetter((p_255526_) -> {
         return null;
      });
   }

   public static <E> com.mojang.serialization.MapCodec<HolderLookup.RegistryLookup<E>> retrieveRegistryLookup(ResourceKey<? extends Registry<? extends E>> resourceKey) {
      return ExtraCodecs.retrieveContext(ops -> {
         if (!(ops instanceof RegistryOps<?> registryOps))
            return DataResult.error(() -> "Not a registry ops");

         return registryOps.lookupProvider.lookup(resourceKey).map(registryInfo -> {
            if (!(registryInfo.owner() instanceof HolderLookup.RegistryLookup<E> registryLookup))
               return DataResult.<HolderLookup.RegistryLookup<E>>error(() -> "Found holder getter but was not a registry lookup for " + resourceKey);

            return DataResult.success(registryLookup, registryInfo.elementsLifecycle());
         }).orElseGet(() -> DataResult.error(() -> "Unknown registry: " + resourceKey));
      });
   }

   public static <E, O> RecordCodecBuilder<O, Holder.Reference<E>> retrieveElement(ResourceKey<E> pKey) {
      ResourceKey<? extends Registry<E>> resourcekey = ResourceKey.createRegistryKey(pKey.registry());
      return ExtraCodecs.retrieveContext((p_274808_) -> {
         if (p_274808_ instanceof RegistryOps<?> registryops) {
            return registryops.lookupProvider.lookup(resourcekey).flatMap((p_255518_) -> {
               return p_255518_.getter().get(pKey);
            }).map(DataResult::success).orElseGet(() -> {
               return DataResult.error(() -> {
                  return "Can't find value: " + pKey;
               });
            });
         } else {
            return DataResult.error(() -> {
               return "Not a registry ops";
            });
         }
      }).forGetter((p_255524_) -> {
         return null;
      });
   }

   public static record RegistryInfo<T>(HolderOwner<T> owner, HolderGetter<T> getter, Lifecycle elementsLifecycle) {
   }

   public interface RegistryInfoLookup {
      <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> pRegistryKey);
   }
}
