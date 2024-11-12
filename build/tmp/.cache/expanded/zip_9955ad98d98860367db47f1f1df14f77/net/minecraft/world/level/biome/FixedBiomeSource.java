package net.minecraft.world.level.biome;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public class FixedBiomeSource extends BiomeSource implements BiomeManager.NoiseBiomeSource {
   public static final Codec<FixedBiomeSource> CODEC = Biome.CODEC.fieldOf("biome").xmap(FixedBiomeSource::new, (p_204259_) -> {
      return p_204259_.biome;
   }).stable().codec();
   private final Holder<Biome> biome;

   public FixedBiomeSource(Holder<Biome> p_204257_) {
      this.biome = p_204257_;
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return Stream.of(this.biome);
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ, Climate.Sampler pSampler) {
      return this.biome;
   }

   /**
    * Gets the biome at the given quart positions.
    * Note that the coordinates passed into this method are 1/4 the scale of block coordinates.
    */
   public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ) {
      return this.biome;
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, int pIncrement, Predicate<Holder<Biome>> pBiomePredicate, RandomSource pRandom, boolean pFindClosest, Climate.Sampler pSampler) {
      if (pBiomePredicate.test(this.biome)) {
         return pFindClosest ? Pair.of(new BlockPos(pX, pY, pZ), this.biome) : Pair.of(new BlockPos(pX - pRadius + pRandom.nextInt(pRadius * 2 + 1), pY, pZ - pRadius + pRandom.nextInt(pRadius * 2 + 1)), this.biome);
      } else {
         return null;
      }
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos pPos, int pRadius, int pHorizontalStep, int pVerticalStep, Predicate<Holder<Biome>> pBiomePredicate, Climate.Sampler pSampler, LevelReader pLevel) {
      return pBiomePredicate.test(this.biome) ? Pair.of(pPos, this.biome) : null;
   }

   public Set<Holder<Biome>> getBiomesWithin(int pX, int pY, int pZ, int pRadius, Climate.Sampler pSampler) {
      return Sets.newHashSet(Set.of(this.biome));
   }
}