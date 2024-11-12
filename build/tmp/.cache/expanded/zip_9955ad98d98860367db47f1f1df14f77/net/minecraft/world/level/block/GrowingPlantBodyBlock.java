package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrowingPlantBodyBlock extends GrowingPlantBlock implements BonemealableBlock {
   protected GrowingPlantBodyBlock(BlockBehaviour.Properties pProperties, Direction pGrowthDirection, VoxelShape pShape, boolean pScheduleFluidTicks) {
      super(pProperties, pGrowthDirection, pShape, pScheduleFluidTicks);
   }

   protected BlockState updateHeadAfterConvertedFromBody(BlockState pHead, BlockState pBody) {
      return pBody;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == this.growthDirection.getOpposite() && !pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.scheduleTick(pCurrentPos, this, 1);
      }

      GrowingPlantHeadBlock growingplantheadblock = this.getHeadBlock();
      if (pFacing == this.growthDirection && !pFacingState.is(this) && !pFacingState.is(growingplantheadblock)) {
         return this.updateHeadAfterConvertedFromBody(pState, growingplantheadblock.getStateForPlacement(pLevel));
      } else {
         if (this.scheduleFluidTicks) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
         }

         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(this.getHeadBlock());
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      Optional<BlockPos> optional = this.getHeadPos(pLevel, pPos, pState.getBlock());
      return optional.isPresent() && this.getHeadBlock().canGrowInto(pLevel.getBlockState(optional.get().relative(this.growthDirection)));
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      Optional<BlockPos> optional = this.getHeadPos(pLevel, pPos, pState.getBlock());
      if (optional.isPresent()) {
         BlockState blockstate = pLevel.getBlockState(optional.get());
         ((GrowingPlantHeadBlock)blockstate.getBlock()).performBonemeal(pLevel, pRandom, optional.get(), blockstate);
      }

   }

   private Optional<BlockPos> getHeadPos(BlockGetter pLevel, BlockPos pPos, Block pBlock) {
      return BlockUtil.getTopConnectedBlock(pLevel, pPos, pBlock, this.growthDirection, this.getHeadBlock());
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      boolean flag = super.canBeReplaced(pState, pUseContext);
      return flag && pUseContext.getItemInHand().is(this.getHeadBlock().asItem()) ? false : flag;
   }

   protected Block getBodyBlock() {
      return this;
   }
}