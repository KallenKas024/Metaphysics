package net.minecraft.world.level.biome;

import net.minecraft.core.Holder;

public interface BiomeResolver {
   Holder<Biome> getNoiseBiome(int pX, int pY, int pZ, Climate.Sampler pSampler);
}