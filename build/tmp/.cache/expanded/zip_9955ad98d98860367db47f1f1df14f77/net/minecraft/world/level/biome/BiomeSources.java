package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public class BiomeSources {
   public static Codec<? extends BiomeSource> bootstrap(Registry<Codec<? extends BiomeSource>> pRegistry) {
      Registry.register(pRegistry, "fixed", FixedBiomeSource.CODEC);
      Registry.register(pRegistry, "multi_noise", MultiNoiseBiomeSource.CODEC);
      Registry.register(pRegistry, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
      return Registry.register(pRegistry, "the_end", TheEndBiomeSource.CODEC);
   }
}