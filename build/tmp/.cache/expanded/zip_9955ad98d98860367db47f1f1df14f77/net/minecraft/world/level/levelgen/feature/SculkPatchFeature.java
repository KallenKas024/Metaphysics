package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;

public class SculkPatchFeature extends Feature<SculkPatchConfiguration> {
   public SculkPatchFeature(Codec<SculkPatchConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<SculkPatchConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      if (!this.canSpreadFrom(worldgenlevel, blockpos)) {
         return false;
      } else {
         SculkPatchConfiguration sculkpatchconfiguration = pContext.config();
         RandomSource randomsource = pContext.random();
         SculkSpreader sculkspreader = SculkSpreader.createWorldGenSpreader();
         int i = sculkpatchconfiguration.spreadRounds() + sculkpatchconfiguration.growthRounds();

         for(int j = 0; j < i; ++j) {
            for(int k = 0; k < sculkpatchconfiguration.chargeCount(); ++k) {
               sculkspreader.addCursors(blockpos, sculkpatchconfiguration.amountPerCharge());
            }

            boolean flag = j < sculkpatchconfiguration.spreadRounds();

            for(int l = 0; l < sculkpatchconfiguration.spreadAttempts(); ++l) {
               sculkspreader.updateCursors(worldgenlevel, blockpos, randomsource, flag);
            }

            sculkspreader.clear();
         }

         BlockPos blockpos2 = blockpos.below();
         if (randomsource.nextFloat() <= sculkpatchconfiguration.catalystChance() && worldgenlevel.getBlockState(blockpos2).isCollisionShapeFullBlock(worldgenlevel, blockpos2)) {
            worldgenlevel.setBlock(blockpos, Blocks.SCULK_CATALYST.defaultBlockState(), 3);
         }

         int i1 = sculkpatchconfiguration.extraRareGrowths().sample(randomsource);

         for(int j1 = 0; j1 < i1; ++j1) {
            BlockPos blockpos1 = blockpos.offset(randomsource.nextInt(5) - 2, 0, randomsource.nextInt(5) - 2);
            if (worldgenlevel.getBlockState(blockpos1).isAir() && worldgenlevel.getBlockState(blockpos1.below()).isFaceSturdy(worldgenlevel, blockpos1.below(), Direction.UP)) {
               worldgenlevel.setBlock(blockpos1, Blocks.SCULK_SHRIEKER.defaultBlockState().setValue(SculkShriekerBlock.CAN_SUMMON, Boolean.valueOf(true)), 3);
            }
         }

         return true;
      }
   }

   private boolean canSpreadFrom(LevelAccessor pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (blockstate.getBlock() instanceof SculkBehaviour) {
         return true;
      } else {
         return !blockstate.isAir() && (!blockstate.is(Blocks.WATER) || !blockstate.getFluidState().isSource()) ? false : Direction.stream().map(pPos::relative).anyMatch((p_225245_) -> {
            return pLevel.getBlockState(p_225245_).isCollisionShapeFullBlock(pLevel, p_225245_);
         });
      }
   }
}