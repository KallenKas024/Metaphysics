package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseRailBlock extends Block implements SimpleWaterloggedBlock, net.minecraftforge.common.extensions.IForgeBaseRailBlock {
   protected static final VoxelShape FLAT_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   private final boolean isStraight;

   public static boolean isRail(Level pLevel, BlockPos pPos) {
      return isRail(pLevel.getBlockState(pPos));
   }

   public static boolean isRail(BlockState pState) {
      return pState.is(BlockTags.RAILS) && pState.getBlock() instanceof BaseRailBlock;
   }

   protected BaseRailBlock(boolean pIsStraight, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.isStraight = pIsStraight;
   }

   public boolean isStraight() {
      return this.isStraight;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      RailShape railshape = pState.is(this) ? pState.getValue(this.getShapeProperty()) : null;
      RailShape railShape2 = pState.is(this) ? getRailDirection(pState, pLevel, pPos, null) : null;
      return railshape != null && railshape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return canSupportRigidBlock(pLevel, pPos.below());
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.updateState(pState, pLevel, pPos, pIsMoving);
      }
   }

   protected BlockState updateState(BlockState pState, Level pLevel, BlockPos pPos, boolean pMovedByPiston) {
      pState = this.updateDir(pLevel, pPos, pState, true);
      if (this.isStraight) {
         pLevel.neighborChanged(pState, pPos, this, pPos, pMovedByPiston);
      }

      return pState;
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide && pLevel.getBlockState(pPos).is(this)) {
         RailShape railshape = getRailDirection(pState, pLevel, pPos, null);
         if (shouldBeRemoved(pPos, pLevel, railshape)) {
            dropResources(pState, pLevel, pPos);
            pLevel.removeBlock(pPos, pIsMoving);
         } else {
            this.updateState(pState, pLevel, pPos, pBlock);
         }

      }
   }

   private static boolean shouldBeRemoved(BlockPos pPos, Level pLevel, RailShape pShape) {
      if (!canSupportRigidBlock(pLevel, pPos.below())) {
         return true;
      } else {
         switch (pShape) {
            case ASCENDING_EAST:
               return !canSupportRigidBlock(pLevel, pPos.east());
            case ASCENDING_WEST:
               return !canSupportRigidBlock(pLevel, pPos.west());
            case ASCENDING_NORTH:
               return !canSupportRigidBlock(pLevel, pPos.north());
            case ASCENDING_SOUTH:
               return !canSupportRigidBlock(pLevel, pPos.south());
            default:
               return false;
         }
      }
   }

   protected void updateState(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock) {
   }

   protected BlockState updateDir(Level pLevel, BlockPos pPos, BlockState pState, boolean pAlwaysPlace) {
      if (pLevel.isClientSide) {
         return pState;
      } else {
         RailShape railshape = pState.getValue(this.getShapeProperty());
         return (new RailState(pLevel, pPos, pState)).place(pLevel.hasNeighborSignal(pPos), pAlwaysPlace, railshape).getState();
      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving) {
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
         if (getRailDirection(pState, pLevel, pPos, null).isAscending()) {
            pLevel.updateNeighborsAt(pPos.above(), this);
         }

         if (this.isStraight) {
            pLevel.updateNeighborsAt(pPos, this);
            pLevel.updateNeighborsAt(pPos.below(), this);
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      boolean flag = fluidstate.getType() == Fluids.WATER;
      BlockState blockstate = super.defaultBlockState();
      Direction direction = pContext.getHorizontalDirection();
      boolean flag1 = direction == Direction.EAST || direction == Direction.WEST;
      return blockstate.setValue(this.getShapeProperty(), flag1 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
   }

   /**
    * @deprecated Forge: Use {@link BaseRailBlock#getRailDirection(BlockState, BlockGetter, BlockPos, net.minecraft.world.entity.vehicle.AbstractMinecart)} for enhanced ability
    * If you do change this property be aware that other functions in this/subclasses may break as they can make assumptions about this property
    */
   @Deprecated
   public abstract Property<RailShape> getShapeProperty();

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   @Override
   public boolean isFlexibleRail(BlockState state, BlockGetter world, BlockPos pos) {
       return  !this.isStraight;
   }

   @Override
   public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.vehicle.AbstractMinecart cart) {
       return state.getValue(getShapeProperty());
   }
}
