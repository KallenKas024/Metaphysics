package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

public class SculkBlock extends DropExperienceBlock implements SculkBehaviour {
   public SculkBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties, ConstantInt.of(1));
   }

   public int attemptUseCharge(SculkSpreader.ChargeCursor pCursor, LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, SculkSpreader pSpreader, boolean pShouldConvertBlocks) {
      int i = pCursor.getCharge();
      if (i != 0 && pRandom.nextInt(pSpreader.chargeDecayRate()) == 0) {
         BlockPos blockpos = pCursor.getPos();
         boolean flag = blockpos.closerThan(pPos, (double)pSpreader.noGrowthRadius());
         if (!flag && canPlaceGrowth(pLevel, blockpos)) {
            int j = pSpreader.growthSpawnCost();
            if (pRandom.nextInt(j) < i) {
               BlockPos blockpos1 = blockpos.above();
               BlockState blockstate = this.getRandomGrowthState(pLevel, blockpos1, pRandom, pSpreader.isWorldGeneration());
               pLevel.setBlock(blockpos1, blockstate, 3);
               pLevel.playSound((Player)null, blockpos, blockstate.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return Math.max(0, i - j);
         } else {
            return pRandom.nextInt(pSpreader.additionalDecayRate()) != 0 ? i : i - (flag ? 1 : getDecayPenalty(pSpreader, blockpos, pPos, i));
         }
      } else {
         return i;
      }
   }

   private static int getDecayPenalty(SculkSpreader pSpreader, BlockPos pCursorPos, BlockPos pRootPos, int pCharge) {
      int i = pSpreader.noGrowthRadius();
      float f = Mth.square((float)Math.sqrt(pCursorPos.distSqr(pRootPos)) - (float)i);
      int j = Mth.square(24 - i);
      float f1 = Math.min(1.0F, f / (float)j);
      return Math.max(1, (int)((float)pCharge * f1 * 0.5F));
   }

   private BlockState getRandomGrowthState(LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom, boolean pIsWorldGeneration) {
      BlockState blockstate;
      if (pRandom.nextInt(11) == 0) {
         blockstate = Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(pIsWorldGeneration));
      } else {
         blockstate = Blocks.SCULK_SENSOR.defaultBlockState();
      }

      return blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && !pLevel.getFluidState(pPos).isEmpty() ? blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)) : blockstate;
   }

   private static boolean canPlaceGrowth(LevelAccessor pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.above());
      if (blockstate.isAir() || blockstate.is(Blocks.WATER) && blockstate.getFluidState().is(Fluids.WATER)) {
         int i = 0;

         for(BlockPos blockpos : BlockPos.betweenClosed(pPos.offset(-4, 0, -4), pPos.offset(4, 2, 4))) {
            BlockState blockstate1 = pLevel.getBlockState(blockpos);
            if (blockstate1.is(Blocks.SCULK_SENSOR) || blockstate1.is(Blocks.SCULK_SHRIEKER)) {
               ++i;
            }

            if (i > 2) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean canChangeBlockStateOnSpread() {
      return false;
   }
}