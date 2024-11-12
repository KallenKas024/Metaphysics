package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SpongeBlock extends Block {
   public static final int MAX_DEPTH = 6;
   public static final int MAX_COUNT = 64;
   private static final Direction[] ALL_DIRECTIONS = Direction.values();

   public SpongeBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.tryAbsorbWater(pLevel, pPos);
      }
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      this.tryAbsorbWater(pLevel, pPos);
      super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
   }

   protected void tryAbsorbWater(Level pLevel, BlockPos pPos) {
      if (this.removeWaterBreadthFirstSearch(pLevel, pPos)) {
         pLevel.setBlock(pPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
         pLevel.levelEvent(2001, pPos, Block.getId(Blocks.WATER.defaultBlockState()));
      }

   }

   private boolean removeWaterBreadthFirstSearch(Level pLevel, BlockPos pPos) {
      BlockState spongeState = pLevel.getBlockState(pPos);
      return BlockPos.breadthFirstTraversal(pPos, 6, 65, (p_277519_, p_277492_) -> {
         for(Direction direction : ALL_DIRECTIONS) {
            p_277492_.accept(p_277519_.relative(direction));
         }

      }, (p_279054_) -> {
         if (p_279054_.equals(pPos)) {
            return true;
         } else {
            BlockState blockstate = pLevel.getBlockState(p_279054_);
            FluidState fluidstate = pLevel.getFluidState(p_279054_);
            if (!spongeState.canBeHydrated(pLevel, pPos, fluidstate, p_279054_)) {
               return false;
            } else {
               Block block = blockstate.getBlock();
               if (block instanceof BucketPickup) {
                  BucketPickup bucketpickup = (BucketPickup)block;
                  if (!bucketpickup.pickupBlock(pLevel, p_279054_, blockstate).isEmpty()) {
                     return true;
                  }
               }

               if (blockstate.getBlock() instanceof LiquidBlock) {
                  pLevel.setBlock(p_279054_, Blocks.AIR.defaultBlockState(), 3);
               } else {
                  if (!blockstate.is(Blocks.KELP) && !blockstate.is(Blocks.KELP_PLANT) && !blockstate.is(Blocks.SEAGRASS) && !blockstate.is(Blocks.TALL_SEAGRASS)) {
                     return false;
                  }

                  BlockEntity blockentity = blockstate.hasBlockEntity() ? pLevel.getBlockEntity(p_279054_) : null;
                  dropResources(blockstate, pLevel, p_279054_, blockentity);
                  pLevel.setBlock(p_279054_, Blocks.AIR.defaultBlockState(), 3);
               }

               return true;
            }
         }
      }) > 1;
   }
}
