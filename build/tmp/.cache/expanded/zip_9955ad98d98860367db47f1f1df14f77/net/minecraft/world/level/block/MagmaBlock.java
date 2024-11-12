package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MagmaBlock extends Block {
   private static final int BUBBLE_COLUMN_CHECK_DELAY = 20;

   public MagmaBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
      if (!pEntity.isSteppingCarefully() && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity)) {
         pEntity.hurt(pLevel.damageSources().hotFloor(), 1.0F);
      }

      super.stepOn(pLevel, pPos, pState, pEntity);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BubbleColumnBlock.updateColumn(pLevel, pPos.above(), pState);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.UP && pFacingState.is(Blocks.WATER)) {
         pLevel.scheduleTick(pCurrentPos, this, 20);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      pLevel.scheduleTick(pPos, this, 20);
   }
}