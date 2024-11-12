package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
   public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
   public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
   protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
   protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
   protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
   private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

   private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String pKey, F pCarver) {
      return Registry.register(BuiltInRegistries.CARVER, pKey, pCarver);
   }

   public WorldCarver(Codec<C> pCodec) {
      this.configuredCodec = pCodec.fieldOf("config").xmap(this::configured, ConfiguredWorldCarver::config).codec();
   }

   public ConfiguredWorldCarver<C> configured(C p_65064_) {
      return new ConfiguredWorldCarver<>(this, p_65064_);
   }

   public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
      return this.configuredCodec;
   }

   public int getRange() {
      return 4;
   }

   /**
    * Carves blocks in an ellipsoid (more accurately a spheroid), defined by a center (x, y, z) position, with a
    * horizontal and vertical radius (the semi-axes)
    * @param pSkipChecker Used to skip certain blocks within the carved region.
    */
   protected boolean carveEllipsoid(CarvingContext pContext, C pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, Aquifer pAquifer, double pX, double pY, double pZ, double pHorizontalRadius, double pVerticalRadius, CarvingMask pCarvingMask, WorldCarver.CarveSkipChecker pSkipChecker) {
      ChunkPos chunkpos = pChunk.getPos();
      double d0 = (double)chunkpos.getMiddleBlockX();
      double d1 = (double)chunkpos.getMiddleBlockZ();
      double d2 = 16.0D + pHorizontalRadius * 2.0D;
      if (!(Math.abs(pX - d0) > d2) && !(Math.abs(pZ - d1) > d2)) {
         int i = chunkpos.getMinBlockX();
         int j = chunkpos.getMinBlockZ();
         int k = Math.max(Mth.floor(pX - pHorizontalRadius) - i - 1, 0);
         int l = Math.min(Mth.floor(pX + pHorizontalRadius) - i, 15);
         int i1 = Math.max(Mth.floor(pY - pVerticalRadius) - 1, pContext.getMinGenY() + 1);
         int j1 = pChunk.isUpgrading() ? 0 : 7;
         int k1 = Math.min(Mth.floor(pY + pVerticalRadius) + 1, pContext.getMinGenY() + pContext.getGenDepth() - 1 - j1);
         int l1 = Math.max(Mth.floor(pZ - pHorizontalRadius) - j - 1, 0);
         int i2 = Math.min(Mth.floor(pZ + pHorizontalRadius) - j, 15);
         boolean flag = false;
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
         BlockPos.MutableBlockPos blockpos$mutableblockpos1 = new BlockPos.MutableBlockPos();

         for(int j2 = k; j2 <= l; ++j2) {
            int k2 = chunkpos.getBlockX(j2);
            double d3 = ((double)k2 + 0.5D - pX) / pHorizontalRadius;

            for(int l2 = l1; l2 <= i2; ++l2) {
               int i3 = chunkpos.getBlockZ(l2);
               double d4 = ((double)i3 + 0.5D - pZ) / pHorizontalRadius;
               if (!(d3 * d3 + d4 * d4 >= 1.0D)) {
                  MutableBoolean mutableboolean = new MutableBoolean(false);

                  for(int j3 = k1; j3 > i1; --j3) {
                     double d5 = ((double)j3 - 0.5D - pY) / pVerticalRadius;
                     if (!pSkipChecker.shouldSkip(pContext, d3, d5, d4, j3) && (!pCarvingMask.get(j2, j3, l2) || isDebugEnabled(pConfig))) {
                        pCarvingMask.set(j2, j3, l2);
                        blockpos$mutableblockpos.set(k2, j3, i3);
                        flag |= this.carveBlock(pContext, pConfig, pChunk, pBiomeAccessor, pCarvingMask, blockpos$mutableblockpos, blockpos$mutableblockpos1, pAquifer, mutableboolean);
                     }
                  }
               }
            }
         }

         return flag;
      } else {
         return false;
      }
   }

   /**
    * Carves a single block, replacing it with the appropriate state if possible, and handles replacing exposed dirt
    * with grass.
    * @param pPos The position to carve at. The method does not mutate this position.
    * @param pCheckPos An additional mutable block position object to be used and modified by the method
    * @param pReachedSurface Set to true if the block carved was the surface, which is checked as being either grass or
    * mycelium
    */
   protected boolean carveBlock(CarvingContext pContext, C pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeGetter, CarvingMask pCarvingMask, BlockPos.MutableBlockPos pPos, BlockPos.MutableBlockPos pCheckPos, Aquifer pAquifer, MutableBoolean pReachedSurface) {
      BlockState blockstate = pChunk.getBlockState(pPos);
      if (blockstate.is(Blocks.GRASS_BLOCK) || blockstate.is(Blocks.MYCELIUM)) {
         pReachedSurface.setTrue();
      }

      if (!this.canReplaceBlock(pConfig, blockstate) && !isDebugEnabled(pConfig)) {
         return false;
      } else {
         BlockState blockstate1 = this.getCarveState(pContext, pConfig, pPos, pAquifer);
         if (blockstate1 == null) {
            return false;
         } else {
            pChunk.setBlockState(pPos, blockstate1, false);
            if (pAquifer.shouldScheduleFluidUpdate() && !blockstate1.getFluidState().isEmpty()) {
               pChunk.markPosForPostprocessing(pPos);
            }

            if (pReachedSurface.isTrue()) {
               pCheckPos.setWithOffset(pPos, Direction.DOWN);
               if (pChunk.getBlockState(pCheckPos).is(Blocks.DIRT)) {
                  pContext.topMaterial(pBiomeGetter, pChunk, pCheckPos, !blockstate1.getFluidState().isEmpty()).ifPresent((p_284918_) -> {
                     pChunk.setBlockState(pCheckPos, p_284918_, false);
                     if (!p_284918_.getFluidState().isEmpty()) {
                        pChunk.markPosForPostprocessing(pCheckPos);
                     }

                  });
               }
            }

            return true;
         }
      }
   }

   @Nullable
   private BlockState getCarveState(CarvingContext pContext, C pConfig, BlockPos pPos, Aquifer pAquifer) {
      if (pPos.getY() <= pConfig.lavaLevel.resolveY(pContext)) {
         return LAVA.createLegacyBlock();
      } else {
         BlockState blockstate = pAquifer.computeSubstance(new DensityFunction.SinglePointContext(pPos.getX(), pPos.getY(), pPos.getZ()), 0.0D);
         if (blockstate == null) {
            return isDebugEnabled(pConfig) ? pConfig.debugSettings.getBarrierState() : null;
         } else {
            return isDebugEnabled(pConfig) ? getDebugState(pConfig, blockstate) : blockstate;
         }
      }
   }

   private static BlockState getDebugState(CarverConfiguration pConfig, BlockState pState) {
      if (pState.is(Blocks.AIR)) {
         return pConfig.debugSettings.getAirState();
      } else if (pState.is(Blocks.WATER)) {
         BlockState blockstate = pConfig.debugSettings.getWaterState();
         return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) ? blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)) : blockstate;
      } else {
         return pState.is(Blocks.LAVA) ? pConfig.debugSettings.getLavaState() : pState;
      }
   }

   /**
    * Carves the given chunk with caves that originate from the given {@code chunkPos}.
    * This method is invoked 289 times in order to generate each chunk (once for every position in an 8 chunk radius, or
    * 17x17 chunk area, centered around the target chunk).
    * 
    * @see net.minecraft.world.level.chunk.ChunkGenerator#applyCarvers
    * @param pChunk The chunk to be carved
    * @param pChunkPos The chunk position this carver is being called from
    */
   public abstract boolean carve(CarvingContext pContext, C pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, RandomSource pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask);

   public abstract boolean isStartChunk(C pConfig, RandomSource pRandom);

   protected boolean canReplaceBlock(C pConfig, BlockState pState) {
      return pState.is(pConfig.replaceable);
   }

   protected static boolean canReach(ChunkPos pChunkPos, double pX, double pZ, int pBranchIndex, int pBranchCount, float pWidth) {
      double d0 = (double)pChunkPos.getMiddleBlockX();
      double d1 = (double)pChunkPos.getMiddleBlockZ();
      double d2 = pX - d0;
      double d3 = pZ - d1;
      double d4 = (double)(pBranchCount - pBranchIndex);
      double d5 = (double)(pWidth + 2.0F + 16.0F);
      return d2 * d2 + d3 * d3 - d4 * d4 <= d5 * d5;
   }

   private static boolean isDebugEnabled(CarverConfiguration pConfig) {
      return pConfig.debugSettings.isDebugMode();
   }

   /**
    * Used to define certain positions to skip or ignore when carving.
    */
   public interface CarveSkipChecker {
      boolean shouldSkip(CarvingContext pContext, double pRelativeX, double pRelativeY, double pRelativeZ, int pY);
   }
}