package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HopperBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
   public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
   private static final VoxelShape TOP = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape FUNNEL = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
   private static final VoxelShape CONVEX_BASE = Shapes.or(FUNNEL, TOP);
   private static final VoxelShape BASE = Shapes.join(CONVEX_BASE, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
   private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
   private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
   private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
   private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
   private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
   private static final VoxelShape DOWN_INTERACTION_SHAPE = Hopper.INSIDE;
   private static final VoxelShape EAST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
   private static final VoxelShape NORTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
   private static final VoxelShape SOUTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
   private static final VoxelShape WEST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));

   public HopperBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch ((Direction)pState.getValue(FACING)) {
         case DOWN:
            return DOWN_SHAPE;
         case NORTH:
            return NORTH_SHAPE;
         case SOUTH:
            return SOUTH_SHAPE;
         case WEST:
            return WEST_SHAPE;
         case EAST:
            return EAST_SHAPE;
         default:
            return BASE;
      }
   }

   public VoxelShape getInteractionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      switch ((Direction)pState.getValue(FACING)) {
         case DOWN:
            return DOWN_INTERACTION_SHAPE;
         case NORTH:
            return NORTH_INTERACTION_SHAPE;
         case SOUTH:
            return SOUTH_INTERACTION_SHAPE;
         case WEST:
            return WEST_INTERACTION_SHAPE;
         case EAST:
            return EAST_INTERACTION_SHAPE;
         default:
            return Hopper.INSIDE;
      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getClickedFace().getOpposite();
      return this.defaultBlockState().setValue(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction).setValue(ENABLED, Boolean.valueOf(true));
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new HopperBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? null : createTickerHelper(pBlockEntityType, BlockEntityType.HOPPER, HopperBlockEntity::pushItemsTick);
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof HopperBlockEntity) {
            ((HopperBlockEntity)blockentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.checkPoweredState(pLevel, pPos, pState, 2);
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof HopperBlockEntity) {
            pPlayer.openMenu((HopperBlockEntity)blockentity);
            pPlayer.awardStat(Stats.INSPECT_HOPPER);
         }

         return InteractionResult.CONSUME;
      }
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      this.checkPoweredState(pLevel, pPos, pState, 4);
   }

   private void checkPoweredState(Level pLevel, BlockPos pPos, BlockState pState, int pFlags) {
      boolean flag = !pLevel.hasNeighborSignal(pPos);
      if (flag != pState.getValue(ENABLED)) {
         pLevel.setBlock(pPos, pState.setValue(ENABLED, Boolean.valueOf(flag)), pFlags);
      }

   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof HopperBlockEntity) {
            Containers.dropContents(pLevel, pPos, (HopperBlockEntity)blockentity);
            pLevel.updateNeighbourForOutputSignal(pPos, this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(pLevel.getBlockEntity(pPos));
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, ENABLED);
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof HopperBlockEntity) {
         HopperBlockEntity.entityInside(pLevel, pPos, pState, pEntity, (HopperBlockEntity)blockentity);
      }

   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}