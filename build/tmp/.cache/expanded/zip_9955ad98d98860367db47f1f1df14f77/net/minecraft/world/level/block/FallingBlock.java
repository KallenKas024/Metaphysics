package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class FallingBlock extends Block implements Fallable {
   public FallingBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      pLevel.scheduleTick(pPos, this, this.getDelayAfterPlace());
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      pLevel.scheduleTick(pCurrentPos, this, this.getDelayAfterPlace());
      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (isFree(pLevel.getBlockState(pPos.below())) && pPos.getY() >= pLevel.getMinBuildHeight()) {
         FallingBlockEntity fallingblockentity = FallingBlockEntity.fall(pLevel, pPos, pState);
         this.falling(fallingblockentity);
      }
   }

   protected void falling(FallingBlockEntity pEntity) {
   }

   protected int getDelayAfterPlace() {
      return 2;
   }

   public static boolean isFree(BlockState pState) {
      return pState.isAir() || pState.is(BlockTags.FIRE) || pState.liquid() || pState.canBeReplaced();
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pRandom.nextInt(16) == 0) {
         BlockPos blockpos = pPos.below();
         if (isFree(pLevel.getBlockState(blockpos))) {
            ParticleUtils.spawnParticleBelow(pLevel, pPos, pRandom, new BlockParticleOption(ParticleTypes.FALLING_DUST, pState));
         }
      }

   }

   public int getDustColor(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return -16777216;
   }
}