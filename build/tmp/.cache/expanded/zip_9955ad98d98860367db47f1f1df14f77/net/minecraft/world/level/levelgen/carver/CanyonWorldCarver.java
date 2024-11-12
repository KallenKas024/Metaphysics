package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

/**
 * A carver responsible for creating ravines, or canyons.
 */
public class CanyonWorldCarver extends WorldCarver<CanyonCarverConfiguration> {
   public CanyonWorldCarver(Codec<CanyonCarverConfiguration> pCodec) {
      super(pCodec);
   }

   public boolean isStartChunk(CanyonCarverConfiguration pConfig, RandomSource pRandom) {
      return pRandom.nextFloat() <= pConfig.probability;
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
   public boolean carve(CarvingContext pContext, CanyonCarverConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, RandomSource pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask) {
      int i = (this.getRange() * 2 - 1) * 16;
      double d0 = (double)pChunkPos.getBlockX(pRandom.nextInt(16));
      int j = pConfig.y.sample(pRandom, pContext);
      double d1 = (double)pChunkPos.getBlockZ(pRandom.nextInt(16));
      float f = pRandom.nextFloat() * ((float)Math.PI * 2F);
      float f1 = pConfig.verticalRotation.sample(pRandom);
      double d2 = (double)pConfig.yScale.sample(pRandom);
      float f2 = pConfig.shape.thickness.sample(pRandom);
      int k = (int)((float)i * pConfig.shape.distanceFactor.sample(pRandom));
      int l = 0;
      this.doCarve(pContext, pConfig, pChunk, pBiomeAccessor, pRandom.nextLong(), pAquifer, d0, (double)j, d1, f2, f, f1, 0, k, d2, pCarvingMask);
      return true;
   }

   private void doCarve(CarvingContext pContext, CanyonCarverConfiguration pConfig, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, long pSeed, Aquifer pAquifer, double pX, double pY, double pZ, float pThickness, float pYaw, float pPitch, int pBranchIndex, int pBranchCount, double pHorizontalVerticalRatio, CarvingMask pCarvingMask) {
      RandomSource randomsource = RandomSource.create(pSeed);
      float[] afloat = this.initWidthFactors(pContext, pConfig, randomsource);
      float f = 0.0F;
      float f1 = 0.0F;

      for(int i = pBranchIndex; i < pBranchCount; ++i) {
         double d0 = 1.5D + (double)(Mth.sin((float)i * (float)Math.PI / (float)pBranchCount) * pThickness);
         double d1 = d0 * pHorizontalVerticalRatio;
         d0 *= (double)pConfig.shape.horizontalRadiusFactor.sample(randomsource);
         d1 = this.updateVerticalRadius(pConfig, randomsource, d1, (float)pBranchCount, (float)i);
         float f2 = Mth.cos(pPitch);
         float f3 = Mth.sin(pPitch);
         pX += (double)(Mth.cos(pYaw) * f2);
         pY += (double)f3;
         pZ += (double)(Mth.sin(pYaw) * f2);
         pPitch *= 0.7F;
         pPitch += f1 * 0.05F;
         pYaw += f * 0.05F;
         f1 *= 0.8F;
         f *= 0.5F;
         f1 += (randomsource.nextFloat() - randomsource.nextFloat()) * randomsource.nextFloat() * 2.0F;
         f += (randomsource.nextFloat() - randomsource.nextFloat()) * randomsource.nextFloat() * 4.0F;
         if (randomsource.nextInt(4) != 0) {
            if (!canReach(pChunk.getPos(), pX, pZ, i, pBranchCount, pThickness)) {
               return;
            }

            this.carveEllipsoid(pContext, pConfig, pChunk, pBiomeAccessor, pAquifer, pX, pY, pZ, d0, d1, pCarvingMask, (p_159082_, p_159083_, p_159084_, p_159085_, p_159086_) -> {
               return this.shouldSkip(p_159082_, afloat, p_159083_, p_159084_, p_159085_, p_159086_);
            });
         }
      }

   }

   /**
    * Generates a random array full of width factors which are used to create the uneven walls of a ravine.
    * @return An array of length {@code context.getGenDepth()}, populated with values between 1.0 and 2.0 inclusive.
    */
   private float[] initWidthFactors(CarvingContext pContext, CanyonCarverConfiguration pConfig, RandomSource pRandom) {
      int i = pContext.getGenDepth();
      float[] afloat = new float[i];
      float f = 1.0F;

      for(int j = 0; j < i; ++j) {
         if (j == 0 || pRandom.nextInt(pConfig.shape.widthSmoothness) == 0) {
            f = 1.0F + pRandom.nextFloat() * pRandom.nextFloat();
         }

         afloat[j] = f * f;
      }

      return afloat;
   }

   private double updateVerticalRadius(CanyonCarverConfiguration pConfig, RandomSource pRandom, double pVerticalRadius, float pBranchCount, float pCurrentBranch) {
      float f = 1.0F - Mth.abs(0.5F - pCurrentBranch / pBranchCount) * 2.0F;
      float f1 = pConfig.shape.verticalRadiusDefaultFactor + pConfig.shape.verticalRadiusCenterFactor * f;
      return (double)f1 * pVerticalRadius * (double)Mth.randomBetween(pRandom, 0.75F, 1.0F);
   }

   private boolean shouldSkip(CarvingContext pContext, float[] pWidthFactors, double pRelativeX, double pRelativeY, double pRelativeZ, int pY) {
      int i = pY - pContext.getMinGenY();
      return (pRelativeX * pRelativeX + pRelativeZ * pRelativeZ) * (double)pWidthFactors[i - 1] + pRelativeY * pRelativeY / 6.0D >= 1.0D;
   }
}