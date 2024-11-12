package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlabBlock extends Block implements SimpleWaterloggedBlock {
   public static final EnumProperty<SlabType> TYPE = BlockStateProperties.SLAB_TYPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   protected static final VoxelShape TOP_AABB = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);

   public SlabBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return pState.getValue(TYPE) != SlabType.DOUBLE;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(TYPE, WATERLOGGED);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      SlabType slabtype = pState.getValue(TYPE);
      switch (slabtype) {
         case DOUBLE:
            return Shapes.block();
         case TOP:
            return TOP_AABB;
         default:
            return BOTTOM_AABB;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = pContext.getLevel().getBlockState(blockpos);
      if (blockstate.is(this)) {
         return blockstate.setValue(TYPE, SlabType.DOUBLE).setValue(WATERLOGGED, Boolean.valueOf(false));
      } else {
         FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
         BlockState blockstate1 = this.defaultBlockState().setValue(TYPE, SlabType.BOTTOM).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
         Direction direction = pContext.getClickedFace();
         return direction != Direction.DOWN && (direction == Direction.UP || !(pContext.getClickLocation().y - (double)blockpos.getY() > 0.5D)) ? blockstate1 : blockstate1.setValue(TYPE, SlabType.TOP);
      }
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      ItemStack itemstack = pUseContext.getItemInHand();
      SlabType slabtype = pState.getValue(TYPE);
      if (slabtype != SlabType.DOUBLE && itemstack.is(this.asItem())) {
         if (pUseContext.replacingClickedOnBlock()) {
            boolean flag = pUseContext.getClickLocation().y - (double)pUseContext.getClickedPos().getY() > 0.5D;
            Direction direction = pUseContext.getClickedFace();
            if (slabtype == SlabType.BOTTOM) {
               return direction == Direction.UP || flag && direction.getAxis().isHorizontal();
            } else {
               return direction == Direction.DOWN || !flag && direction.getAxis().isHorizontal();
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
      return pState.getValue(TYPE) != SlabType.DOUBLE ? SimpleWaterloggedBlock.super.placeLiquid(pLevel, pPos, pState, pFluidState) : false;
   }

   public boolean canPlaceLiquid(BlockGetter pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
      return pState.getValue(TYPE) != SlabType.DOUBLE ? SimpleWaterloggedBlock.super.canPlaceLiquid(pLevel, pPos, pState, pFluid) : false;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      switch (pType) {
         case LAND:
            return false;
         case WATER:
            return pLevel.getFluidState(pPos).is(FluidTags.WATER);
         case AIR:
            return false;
         default:
            return false;
      }
   }
}