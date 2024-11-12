package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public abstract class BiomeSource implements BiomeResolver {
   public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
   private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(() -> {
      return this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet());
   });

   protected BiomeSource() {
   }

   protected abstract Codec<? extends BiomeSource> codec();

   protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

   public Set<Holder<Biome>> possibleBiomes() {
      return this.possibleBiomes.get();
   }

   public Set<Holder<Biome>> getBiomesWithin(int pX, int pY, int pZ, int pRadius, Climate.Sampler pSampler) {
      int i = QuartPos.fromBlock(pX - pRadius);
      int j = QuartPos.fromBlock(pY - pRadius);
      int k = QuartPos.fromBlock(pZ - pRadius);
      int l = QuartPos.fromBlock(pX + pRadius);
      int i1 = QuartPos.fromBlock(pY + pRadius);
      int j1 = QuartPos.fromBlock(pZ + pRadius);
      int k1 = l - i + 1;
      int l1 = i1 - j + 1;
      int i2 = j1 - k + 1;
      Set<Holder<Biome>> set = Sets.newHashSet();

      for(int j2 = 0; j2 < i2; ++j2) {
         for(int k2 = 0; k2 < k1; ++k2) {
            for(int l2 = 0; l2 < l1; ++l2) {
               int i3 = i + k2;
               int j3 = j + l2;
               int k3 = k + j2;
               set.add(this.getNoiseBiome(i3, j3, k3, pSampler));
            }
         }
      }

      return set;
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, Predicate<Holder<Biome>> pBiomePredicate, RandomSource pRandom, Climate.Sampler pSampler) {
      return this.findBiomeHorizontal(pX, pY, pZ, pRadius, 1, pBiomePredicate, pRandom, false, pSampler);
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos pPos, int pRadius, int pHorizontalStep, int pVerticalStep, Predicate<Holder<Biome>> pBiomePredicate, Climate.Sampler pSampler, LevelReader pLevel) {
      Set<Holder<Biome>> set = this.possibleBiomes().stream().filter(pBiomePredicate).collect(Collectors.toUnmodifiableSet());
      if (set.isEmpty()) {
         return null;
      } else {
         int i = Math.floorDiv(pRadius, pHorizontalStep);
         int[] aint = Mth.outFromOrigin(pPos.getY(), pLevel.getMinBuildHeight() + 1, pLevel.getMaxBuildHeight(), pVerticalStep).toArray();

         for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.spiralAround(BlockPos.ZERO, i, Direction.EAST, Direction.SOUTH)) {
            int j = pPos.getX() + blockpos$mutableblockpos.getX() * pHorizontalStep;
            int k = pPos.getZ() + blockpos$mutableblockpos.getZ() * pHorizontalStep;
            int l = QuartPos.fromBlock(j);
            int i1 = QuartPos.fromBlock(k);

            for(int j1 : aint) {
               int k1 = QuartPos.fromBlock(j1);
               Holder<Biome> holder = this.getNoiseBiome(l, k1, i1, pSampler);
               if (set.contains(holder)) {
                  return Pair.of(new BlockPos(j, j1, k), holder);
               }
            }
         }

         return null;
      }
   }

   @Nullable
   public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int pX, int pY, int pZ, int pRadius, int pIncrement, Predicate<Holder<Biome>> pBiomePredicate, RandomSource pRandom, boolean pFindClosest, Climate.Sampler pSampler) {
      int i = QuartPos.fromBlock(pX);
      int j = QuartPos.fromBlock(pZ);
      int k = QuartPos.fromBlock(pRadius);
      int l = QuartPos.fromBlock(pY);
      Pair<BlockPos, Holder<Biome>> pair = null;
      int i1 = 0;
      int j1 = pFindClosest ? 0 : k;

      for(int k1 = j1; k1 <= k; k1 += pIncrement) {
         for(int l1 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -k1; l1 <= k1; l1 += pIncrement) {
            boolean flag = Math.abs(l1) == k1;

            for(int i2 = -k1; i2 <= k1; i2 += pIncrement) {
               if (pFindClosest) {
                  boolean flag1 = Math.abs(i2) == k1;
                  if (!flag1 && !flag) {
                     continue;
                  }
               }

               int k2 = i + i2;
               int j2 = j + l1;
               Holder<Biome> holder = this.getNoiseBiome(k2, l, j2, pSampler);
               if (pBiomePredicate.test(holder)) {
                  if (pair == null || pRandom.nextInt(i1 + 1) == 0) {
                     BlockPos blockpos = new BlockPos(QuartPos.toBlock(k2), pY, QuartPos.toBlock(j2));
                     if (pFindClosest) {
                        return Pair.of(blockpos, holder);
                     }

                     pair = Pair.of(blockpos, holder);
                  }

                  ++i1;
               }
            }
         }
      }

      return pair;
   }

   public abstract Holder<Biome> getNoiseBiome(int pX, int pY, int pZ, Climate.Sampler pSampler);

   public void addDebugInfo(List<String> pInfo, BlockPos pPos, Climate.Sampler pSampler) {
   }
}