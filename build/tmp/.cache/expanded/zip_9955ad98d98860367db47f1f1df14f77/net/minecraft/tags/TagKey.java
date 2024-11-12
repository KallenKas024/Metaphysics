package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
   private static final Interner<TagKey<?>> VALUES = Interners.newWeakInterner();

   public static <T> Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> pRegistry) {
      return ResourceLocation.CODEC.xmap((p_203893_) -> {
         return create(pRegistry, p_203893_);
      }, TagKey::location);
   }

   public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> pRegistry) {
      return Codec.STRING.comapFlatMap((p_274844_) -> {
         return p_274844_.startsWith("#") ? ResourceLocation.read(p_274844_.substring(1)).map((p_203890_) -> {
            return create(pRegistry, p_203890_);
         }) : DataResult.error(() -> {
            return "Not a tag id";
         });
      }, (p_203876_) -> {
         return "#" + p_203876_.location;
      });
   }

   public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> pRegistry, ResourceLocation pLocation) {
      return (TagKey<T>)VALUES.intern(new TagKey<>(pRegistry, pLocation));
   }

   public boolean isFor(ResourceKey<? extends Registry<?>> pRegistry) {
      return this.registry == pRegistry;
   }

   public <E> Optional<TagKey<E>> cast(ResourceKey<? extends Registry<E>> pRegistry) {
      return this.isFor(pRegistry) ? Optional.of((TagKey<E>)this) : Optional.empty();
   }

   public String toString() {
      return "TagKey[" + this.registry.location() + " / " + this.location + "]";
   }
}