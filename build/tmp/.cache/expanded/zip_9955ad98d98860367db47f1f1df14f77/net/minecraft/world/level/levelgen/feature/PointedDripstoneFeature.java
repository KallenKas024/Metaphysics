package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;

public class PointedDripstoneFeature extends Feature<PointedDripstoneConfiguration> {
   public PointedDripstoneFeature(Codec<PointedDripstoneConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<PointedDripstoneConfiguration> pContext) {
      LevelAccessor levelaccessor = pContext.level();
      BlockPos blockpos = pContext.origin();
      RandomSource randomsource = pContext.random();
      PointedDripstoneConfiguration pointeddripstoneconfiguration = pContext.config();
      Optional<Direction> optional = getTipDirection(levelaccessor, blockpos, randomsource);
      if (optional.isEmpty()) {
         return false;
      } else {
         BlockPos blockpos1 = blockpos.relative(optional.get().getOpposite());
         createPatchOfDripstoneBlocks(levelaccessor, randomsource, blockpos1, pointeddripstoneconfiguration);
         int i = randomsource.nextFloat() < pointeddripstoneconfiguration.chanceOfTallerDripstone && DripstoneUtils.isEmptyOrWater(levelaccessor.getBlockState(blockpos.relative(optional.get()))) ? 2 : 1;
         DripstoneUtils.growPointedDripstone(levelaccessor, blockpos, optional.get(), i, false);
         return true;
      }
   }

   private static Optional<Direction> getTipDirection(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
      boolean flag = DripstoneUtils.isDripstoneBase(pLevel.getBlockState(pPos.above()));
      boolean flag1 = DripstoneUtils.isDripstoneBase(pLevel.getBlockState(pPos.below()));
      if (flag && flag1) {
         return Optional.of(pRandom.nextBoolean() ? Direction.DOWN : Direction.UP);
      } else if (flag) {
         return Optional.of(Direction.DOWN);
      } else {
         return flag1 ? Optional.of(Direction.UP) : Optional.empty();
      }
   }

   private static void createPatchOfDripstoneBlocks(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos, PointedDripstoneConfiguration pConfig) {
      DripstoneUtils.placeDripstoneBlockIfPossible(pLevel, pPos);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!(pRandom.nextFloat() > pConfig.chanceOfDirectionalSpread)) {
            BlockPos blockpos = pPos.relative(direction);
            DripstoneUtils.placeDripstoneBlockIfPossible(pLevel, blockpos);
            if (!(pRandom.nextFloat() > pConfig.chanceOfSpreadRadius2)) {
               BlockPos blockpos1 = blockpos.relative(Direction.getRandom(pRandom));
               DripstoneUtils.placeDripstoneBlockIfPossible(pLevel, blockpos1);
               if (!(pRandom.nextFloat() > pConfig.chanceOfSpreadRadius3)) {
                  BlockPos blockpos2 = blockpos1.relative(Direction.getRandom(pRandom));
                  DripstoneUtils.placeDripstoneBlockIfPossible(pLevel, blockpos2);
               }
            }
         }
      }

   }
}