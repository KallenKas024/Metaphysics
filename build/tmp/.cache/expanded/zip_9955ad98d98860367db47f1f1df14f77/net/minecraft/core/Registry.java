package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T> extends Keyable, IdMap<T> {
   ResourceKey<? extends Registry<T>> key();

   default Codec<T> byNameCodec() {
      Codec<T> codec = ResourceLocation.CODEC.flatXmap((p_258170_) -> {
         return Optional.ofNullable(this.get(p_258170_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Unknown registry key in " + this.key() + ": " + p_258170_;
            });
         });
      }, (p_258177_) -> {
         return this.getResourceKey(p_258177_).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Unknown registry element in " + this.key() + ":" + p_258177_;
            });
         });
      });
      Codec<T> codec1 = ExtraCodecs.idResolverCodec((p_258179_) -> {
         return this.getResourceKey(p_258179_).isPresent() ? this.getId(p_258179_) : -1;
      }, this::byId, -1);
      return ExtraCodecs.overrideLifecycle(ExtraCodecs.orCompressed(codec, codec1), this::lifecycle, this::lifecycle);
   }

   default Codec<Holder<T>> holderByNameCodec() {
      Codec<Holder<T>> codec = ResourceLocation.CODEC.flatXmap((p_258174_) -> {
         return this.getHolder(ResourceKey.create(this.key(), p_258174_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Unknown registry key in " + this.key() + ": " + p_258174_;
            });
         });
      }, (p_206061_) -> {
         return p_206061_.unwrapKey().map(ResourceKey::location).map(DataResult::success).orElseGet(() -> {
            return DataResult.error(() -> {
               return "Unknown registry element in " + this.key() + ":" + p_206061_;
            });
         });
      });
      return ExtraCodecs.overrideLifecycle(codec, (p_258178_) -> {
         return this.lifecycle(p_258178_.value());
      }, (p_258171_) -> {
         return this.lifecycle(p_258171_.value());
      });
   }

   default <U> Stream<U> keys(DynamicOps<U> pOps) {
      return this.keySet().stream().map((p_235784_) -> {
         return pOps.createString(p_235784_.toString());
      });
   }

   /**
    * @return the name used to identify the given object within this registry or {@code null} if the object is not
    * within this registry
    */
   @Nullable
   ResourceLocation getKey(T pValue);

   Optional<ResourceKey<T>> getResourceKey(T pValue);

   /**
    * @return the integer ID used to identify the given object
    */
   int getId(@Nullable T pValue);

   @Nullable
   T get(@Nullable ResourceKey<T> pKey);

   @Nullable
   T get(@Nullable ResourceLocation pName);

   Lifecycle lifecycle(T p_123012_);

   Lifecycle registryLifecycle();

   default Optional<T> getOptional(@Nullable ResourceLocation pName) {
      return Optional.ofNullable(this.get(pName));
   }

   default Optional<T> getOptional(@Nullable ResourceKey<T> pRegistryKey) {
      return Optional.ofNullable(this.get(pRegistryKey));
   }

   default T getOrThrow(ResourceKey<T> pKey) {
      T t = this.get(pKey);
      if (t == null) {
         throw new IllegalStateException("Missing key in " + this.key() + ": " + pKey);
      } else {
         return t;
      }
   }

   /**
    * @return all keys in this registry
    */
   Set<ResourceLocation> keySet();

   Set<Map.Entry<ResourceKey<T>, T>> entrySet();

   Set<ResourceKey<T>> registryKeySet();

   Optional<Holder.Reference<T>> getRandom(RandomSource pRandom);

   default Stream<T> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   boolean containsKey(ResourceLocation pName);

   boolean containsKey(ResourceKey<T> pKey);

   static <T> T register(Registry<? super T> pRegistry, String pName, T pValue) {
      return register(pRegistry, new ResourceLocation(pName), pValue);
   }

   static <V, T extends V> T register(Registry<V> pRegistry, ResourceLocation pName, T pValue) {
      return register(pRegistry, ResourceKey.create(pRegistry.key(), pName), pValue);
   }

   static <V, T extends V> T register(Registry<V> pRegistry, ResourceKey<V> pKey, T pValue) {
      ((WritableRegistry)pRegistry).register(pKey, (V)pValue, Lifecycle.stable());
      return pValue;
   }

   static <T> Holder.Reference<T> registerForHolder(Registry<T> pRegistry, ResourceKey<T> pKey, T pValue) {
      return ((WritableRegistry)pRegistry).register(pKey, pValue, Lifecycle.stable());
   }

   static <T> Holder.Reference<T> registerForHolder(Registry<T> pRegistry, ResourceLocation pName, T pValue) {
      return registerForHolder(pRegistry, ResourceKey.create(pRegistry.key(), pName), pValue);
   }

   static <V, T extends V> T registerMapping(Registry<V> pRegistry, int pId, String pName, T pValue) {
      ((WritableRegistry)pRegistry).registerMapping(pId, ResourceKey.create(pRegistry.key(), new ResourceLocation(pName)), (V)pValue, Lifecycle.stable());
      return pValue;
   }

   Registry<T> freeze();

   Holder.Reference<T> createIntrusiveHolder(T pValue);

   Optional<Holder.Reference<T>> getHolder(int pId);

   Optional<Holder.Reference<T>> getHolder(ResourceKey<T> pKey);

   Holder<T> wrapAsHolder(T pValue);

   default Holder.Reference<T> getHolderOrThrow(ResourceKey<T> pKey) {
      return this.getHolder(pKey).orElseThrow(() -> {
         return new IllegalStateException("Missing key in " + this.key() + ": " + pKey);
      });
   }

   Stream<Holder.Reference<T>> holders();

   Optional<HolderSet.Named<T>> getTag(TagKey<T> pKey);

   default Iterable<Holder<T>> getTagOrEmpty(TagKey<T> pKey) {
      return DataFixUtils.orElse(this.getTag(pKey), List.of());
   }

   HolderSet.Named<T> getOrCreateTag(TagKey<T> pKey);

   Stream<Pair<TagKey<T>, HolderSet.Named<T>>> getTags();

   Stream<TagKey<T>> getTagNames();

   void resetTags();

   void bindTags(Map<TagKey<T>, List<Holder<T>>> pTagMap);

   default IdMap<Holder<T>> asHolderIdMap() {
      return new IdMap<Holder<T>>() {
         /**
          * @return the integer ID used to identify the given object
          */
         public int getId(Holder<T> p_259992_) {
            return Registry.this.getId(p_259992_.value());
         }

         @Nullable
         public Holder<T> byId(int p_259972_) {
            return (Holder)Registry.this.getHolder(p_259972_).orElse(null);
         }

         public int size() {
            return Registry.this.size();
         }

         public Iterator<Holder<T>> iterator() {
            return Registry.this.holders().map((p_260061_) -> {
               return (Holder<T>)p_260061_;
            }).iterator();
         }
      };
   }

   HolderOwner<T> holderOwner();

   HolderLookup.RegistryLookup<T> asLookup();

   default HolderLookup.RegistryLookup<T> asTagAddingLookup() {
      return new HolderLookup.RegistryLookup.Delegate<T>() {
         protected HolderLookup.RegistryLookup<T> parent() {
            return Registry.this.asLookup();
         }

         public Optional<HolderSet.Named<T>> get(TagKey<T> p_259111_) {
            return Optional.of(this.getOrThrow(p_259111_));
         }

         public HolderSet.Named<T> getOrThrow(TagKey<T> p_259653_) {
            return Registry.this.getOrCreateTag(p_259653_);
         }
      };
   }
}