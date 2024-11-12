package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;

public record WorldGenSettings(WorldOptions options, WorldDimensions dimensions) {
   public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create((p_248477_) -> {
      return p_248477_.group(WorldOptions.CODEC.forGetter(WorldGenSettings::options), WorldDimensions.CODEC.forGetter(WorldGenSettings::dimensions)).apply(p_248477_, p_248477_.stable(WorldGenSettings::new));
   });

   public static <T> DataResult<T> encode(DynamicOps<T> pOps, WorldOptions pOptions, WorldDimensions pDimensions) {
      return CODEC.encodeStart(pOps, new WorldGenSettings(pOptions, pDimensions));
   }

   public static <T> DataResult<T> encode(DynamicOps<T> pOps, WorldOptions pOptions, RegistryAccess pAccess) {
      return encode(pOps, pOptions, new WorldDimensions(pAccess.registryOrThrow(Registries.LEVEL_STEM)));
   }
}