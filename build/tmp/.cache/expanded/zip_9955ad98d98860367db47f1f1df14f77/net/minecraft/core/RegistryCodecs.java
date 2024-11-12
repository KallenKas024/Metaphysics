package net.minecraft.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;

public class RegistryCodecs {
   private static <T> MapCodec<RegistryCodecs.RegistryEntry<T>> withNameAndId(ResourceKey<? extends Registry<T>> pRegistryKey, MapCodec<T> pElementCodec) {
      return RecordCodecBuilder.mapCodec((p_206309_) -> {
         return p_206309_.group(ResourceKey.codec(pRegistryKey).fieldOf("name").forGetter(RegistryCodecs.RegistryEntry::key), Codec.INT.fieldOf("id").forGetter(RegistryCodecs.RegistryEntry::id), pElementCodec.forGetter(RegistryCodecs.RegistryEntry::value)).apply(p_206309_, RegistryCodecs.RegistryEntry::new);
      });
   }

   public static <T> Codec<Registry<T>> networkCodec(ResourceKey<? extends Registry<T>> pRegistryKey, Lifecycle pLifecycle, Codec<T> pElementCodec) {
      return withNameAndId(pRegistryKey, pElementCodec.fieldOf("element")).codec().listOf().xmap((p_258188_) -> {
         WritableRegistry<T> writableregistry = new MappedRegistry<>(pRegistryKey, pLifecycle);

         for(RegistryCodecs.RegistryEntry<T> registryentry : p_258188_) {
            writableregistry.registerMapping(registryentry.id(), registryentry.key(), registryentry.value(), pLifecycle);
         }

         return writableregistry;
      }, (p_258185_) -> {
         ImmutableList.Builder<RegistryCodecs.RegistryEntry<T>> builder = ImmutableList.builder();

         for(T t : p_258185_) {
            builder.add(new RegistryCodecs.RegistryEntry<>(p_258185_.getResourceKey(t).get(), p_258185_.getId(t), t));
         }

         return builder.build();
      });
   }

   public static <E> Codec<Registry<E>> fullCodec(ResourceKey<? extends Registry<E>> pRegistryKey, Lifecycle pLifecycle, Codec<E> pElementCodec) {
      // FORGE: Fix MC-197860
      Codec<Map<ResourceKey<E>, E>> codec = new net.minecraftforge.common.LenientUnboundedMapCodec<>(ResourceKey.codec(pRegistryKey), pElementCodec);
      return codec.xmap((p_258184_) -> {
         WritableRegistry<E> writableregistry = new MappedRegistry<>(pRegistryKey, pLifecycle);
         p_258184_.forEach((p_258191_, p_258192_) -> {
            writableregistry.register(p_258191_, p_258192_, pLifecycle);
         });
         return writableregistry.freeze();
      }, (p_258193_) -> {
         return ImmutableMap.copyOf(p_258193_.entrySet());
      });
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec) {
      return homogeneousList(pRegistryKey, pElementCodec, false);
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> pRegistryKey, Codec<E> pElementCodec, boolean pDisallowInline) {
      return HolderSetCodec.create(pRegistryKey, RegistryFileCodec.create(pRegistryKey, pElementCodec), pDisallowInline);
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> pRegistryKey) {
      return homogeneousList(pRegistryKey, false);
   }

   public static <E> Codec<HolderSet<E>> homogeneousList(ResourceKey<? extends Registry<E>> pRegistryKey, boolean pDisallowInline) {
      return HolderSetCodec.create(pRegistryKey, RegistryFixedCodec.create(pRegistryKey), pDisallowInline);
   }

   static record RegistryEntry<T>(ResourceKey<T> key, int id, T value) {
   }
}
