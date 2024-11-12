package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
   public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      LargeDripstoneConfiguration largedripstoneconfiguration = pContext.config();
      RandomSource randomsource = pContext.random();
      if (!DripstoneUtils.isEmptyOrWater(worldgenlevel, blockpos)) {
         return false;
      } else {
         Optional<Column> optional = Column.scan(worldgenlevel, blockpos, largedripstoneconfiguration.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava);
         if (optional.isPresent() && optional.get() instanceof Column.Range) {
            Column.Range column$range = (Column.Range)optional.get();
            if (column$range.height() < 4) {
               return false;
            } else {
               int i = (int)((float)column$range.height() * largedripstoneconfiguration.maxColumnRadiusToCaveHeightRatio);
               int j = Mth.clamp(i, largedripstoneconfiguration.columnRadius.getMinValue(), largedripstoneconfiguration.columnRadius.getMaxValue());
               int k = Mth.randomBetweenInclusive(randomsource, largedripstoneconfiguration.columnRadius.getMinValue(), j);
               LargeDripstoneFeature.LargeDripstone largedripstonefeature$largedripstone = makeDripstone(blockpos.atY(column$range.ceiling() - 1), false, randomsource, k, largedripstoneconfiguration.stalactiteBluntness, largedripstoneconfiguration.heightScale);
               LargeDripstoneFeature.LargeDripstone largedripstonefeature$largedripstone1 = makeDripstone(blockpos.atY(column$range.floor() + 1), true, randomsource, k, largedripstoneconfiguration.stalagmiteBluntness, largedripstoneconfiguration.heightScale);
               LargeDripstoneFeature.WindOffsetter largedripstonefeature$windoffsetter;
               if (largedripstonefeature$largedripstone.isSuitableForWind(largedripstoneconfiguration) && largedripstonefeature$largedripstone1.isSuitableForWind(largedripstoneconfiguration)) {
                  largedripstonefeature$windoffsetter = new LargeDripstoneFeature.WindOffsetter(blockpos.getY(), randomsource, largedripstoneconfiguration.windSpeed);
               } else {
                  largedripstonefeature$windoffsetter = LargeDripstoneFeature.WindOffsetter.noWind();
               }

               boolean flag = largedripstonefeature$largedripstone.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldgenlevel, largedripstonefeature$windoffsetter);
               boolean flag1 = largedripstonefeature$largedripstone1.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(worldgenlevel, largedripstonefeature$windoffsetter);
               if (flag) {
                  largedripstonefeature$largedripstone.placeBlocks(worldgenlevel, randomsource, largedripstonefeature$windoffsetter);
               }

               if (flag1) {
                  largedripstonefeature$largedripstone1.placeBlocks(worldgenlevel, randomsource, largedripstonefeature$windoffsetter);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static LargeDripstoneFeature.LargeDripstone makeDripstone(BlockPos pRoot, boolean pPointingUp, RandomSource pRandom, int pRadius, FloatProvider pBluntnessBase, FloatProvider pScaleBase) {
      return new LargeDripstoneFeature.LargeDripstone(pRoot, pPointingUp, pRadius, (double)pBluntnessBase.sample(pRandom), (double)pScaleBase.sample(pRandom));
   }

   private void placeDebugMarkers(WorldGenLevel pLevel, BlockPos pPos, Column.Range pRange, LargeDripstoneFeature.WindOffsetter pWindOffsetter) {
      pLevel.setBlock(pWindOffsetter.offset(pPos.atY(pRange.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
      pLevel.setBlock(pWindOffsetter.offset(pPos.atY(pRange.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

      for(BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.atY(pRange.floor() + 2).mutable(); blockpos$mutableblockpos.getY() < pRange.ceiling() - 1; blockpos$mutableblockpos.move(Direction.UP)) {
         BlockPos blockpos = pWindOffsetter.offset(blockpos$mutableblockpos);
         if (DripstoneUtils.isEmptyOrWater(pLevel, blockpos) || pLevel.getBlockState(blockpos).is(Blocks.DRIPSTONE_BLOCK)) {
            pLevel.setBlock(blockpos, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
         }
      }

   }

   static final class LargeDripstone {
      private BlockPos root;
      private final boolean pointingUp;
      private int radius;
      private final double bluntness;
      private final double scale;

      LargeDripstone(BlockPos pRoot, boolean pPointingUp, int pRadius, double pBluntness, double pScale) {
         this.root = pRoot;
         this.pointingUp = pPointingUp;
         this.radius = pRadius;
         this.bluntness = pBluntness;
         this.scale = pScale;
      }

      private int getHeight() {
         return this.getHeightAtRadius(0.0F);
      }

      private int getMinY() {
         return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
      }

      private int getMaxY() {
         return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
      }

      boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(WorldGenLevel pLevel, LargeDripstoneFeature.WindOffsetter pWindOffsetter) {
         while(this.radius > 1) {
            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.root.mutable();
            int i = Math.min(10, this.getHeight());

            for(int j = 0; j < i; ++j) {
               if (pLevel.getBlockState(blockpos$mutableblockpos).is(Blocks.LAVA)) {
                  return false;
               }

               if (DripstoneUtils.isCircleMostlyEmbeddedInStone(pLevel, pWindOffsetter.offset(blockpos$mutableblockpos), this.radius)) {
                  this.root = blockpos$mutableblockpos;
                  return true;
               }

               blockpos$mutableblockpos.move(this.pointingUp ? Direction.DOWN : Direction.UP);
            }

            this.radius /= 2;
         }

         return false;
      }

      private int getHeightAtRadius(float pRadius) {
         return (int)DripstoneUtils.getDripstoneHeight((double)pRadius, (double)this.radius, this.scale, this.bluntness);
      }

      void placeBlocks(WorldGenLevel pLevel, RandomSource pRandom, LargeDripstoneFeature.WindOffsetter pWindOffsetter) {
         for(int i = -this.radius; i <= this.radius; ++i) {
            for(int j = -this.radius; j <= this.radius; ++j) {
               float f = Mth.sqrt((float)(i * i + j * j));
               if (!(f > (float)this.radius)) {
                  int k = this.getHeightAtRadius(f);
                  if (k > 0) {
                     if ((double)pRandom.nextFloat() < 0.2D) {
                        k = (int)((float)k * Mth.randomBetween(pRandom, 0.8F, 1.0F));
                     }

                     BlockPos.MutableBlockPos blockpos$mutableblockpos = this.root.offset(i, 0, j).mutable();
                     boolean flag = false;
                     int l = this.pointingUp ? pLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, blockpos$mutableblockpos.getX(), blockpos$mutableblockpos.getZ()) : Integer.MAX_VALUE;

                     for(int i1 = 0; i1 < k && blockpos$mutableblockpos.getY() < l; ++i1) {
                        BlockPos blockpos = pWindOffsetter.offset(blockpos$mutableblockpos);
                        if (DripstoneUtils.isEmptyOrWaterOrLava(pLevel, blockpos)) {
                           flag = true;
                           Block block = Blocks.DRIPSTONE_BLOCK;
                           pLevel.setBlock(blockpos, block.defaultBlockState(), 2);
                        } else if (flag && pLevel.getBlockState(blockpos).is(BlockTags.BASE_STONE_OVERWORLD)) {
                           break;
                        }

                        blockpos$mutableblockpos.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                     }
                  }
               }
            }
         }

      }

      boolean isSuitableForWind(LargeDripstoneConfiguration pConfig) {
         return this.radius >= pConfig.minRadiusForWind && this.bluntness >= (double)pConfig.minBluntnessForWind;
      }
   }

   static final class WindOffsetter {
      private final int originY;
      @Nullable
      private final Vec3 windSpeed;

      WindOffsetter(int pOriginY, RandomSource pRandom, FloatProvider pMagnitude) {
         this.originY = pOriginY;
         float f = pMagnitude.sample(pRandom);
         float f1 = Mth.randomBetween(pRandom, 0.0F, (float)Math.PI);
         this.windSpeed = new Vec3((double)(Mth.cos(f1) * f), 0.0D, (double)(Mth.sin(f1) * f));
      }

      private WindOffsetter() {
         this.originY = 0;
         this.windSpeed = null;
      }

      static LargeDripstoneFeature.WindOffsetter noWind() {
         return new LargeDripstoneFeature.WindOffsetter();
      }

      BlockPos offset(BlockPos pPos) {
         if (this.windSpeed == null) {
            return pPos;
         } else {
            int i = this.originY - pPos.getY();
            Vec3 vec3 = this.windSpeed.scale((double)i);
            return pPos.offset(Mth.floor(vec3.x), 0, Mth.floor(vec3.z));
         }
      }
   }
}