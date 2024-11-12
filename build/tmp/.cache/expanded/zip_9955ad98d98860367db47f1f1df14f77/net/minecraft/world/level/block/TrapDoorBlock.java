package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TrapDoorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final int AABB_THICKNESS = 3;
   protected static final VoxelShape EAST_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_OPEN_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape SOUTH_OPEN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
   protected static final VoxelShape NORTH_OPEN_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape BOTTOM_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);
   protected static final VoxelShape TOP_AABB = Block.box(0.0D, 13.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private final BlockSetType type;

   public TrapDoorBlock(BlockBehaviour.Properties pProperties, BlockSetType pType) {
      super(pProperties.sound(pType.soundType()));
      this.type = pType;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, Boolean.valueOf(false)).setValue(HALF, Half.BOTTOM).setValue(POWERED, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (!pState.getValue(OPEN)) {
         return pState.getValue(HALF) == Half.TOP ? TOP_AABB : BOTTOM_AABB;
      } else {
         switch ((Direction)pState.getValue(FACING)) {
            case NORTH:
            default:
               return NORTH_OPEN_AABB;
            case SOUTH:
               return SOUTH_OPEN_AABB;
            case WEST:
               return WEST_OPEN_AABB;
            case EAST:
               return EAST_OPEN_AABB;
         }
      }
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      switch (pType) {
         case LAND:
            return pState.getValue(OPEN);
         case WATER:
            return pState.getValue(WATERLOGGED);
         case AIR:
            return pState.getValue(OPEN);
         default:
            return false;
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (!this.type.canOpenByHand()) {
         return InteractionResult.PASS;
      } else {
         pState = pState.cycle(OPEN);
         pLevel.setBlock(pPos, pState, 2);
         if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
         }

         this.playSound(pPlayer, pLevel, pPos, pState.getValue(OPEN));
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      }
   }

   protected void playSound(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, boolean pIsOpened) {
      pLevel.playSound(pPlayer, pPos, pIsOpened ? this.type.trapdoorOpen() : this.type.trapdoorClose(), SoundSource.BLOCKS, 1.0F, pLevel.getRandom().nextFloat() * 0.1F + 0.9F);
      pLevel.gameEvent(pPlayer, pIsOpened ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         boolean flag = pLevel.hasNeighborSignal(pPos);
         if (flag != pState.getValue(POWERED)) {
            if (pState.getValue(OPEN) != flag) {
               pState = pState.setValue(OPEN, Boolean.valueOf(flag));
               this.playSound((Player)null, pLevel, pPos, flag);
            }

            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 2);
            if (pState.getValue(WATERLOGGED)) {
               pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
            }
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = this.defaultBlockState();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      Direction direction = pContext.getClickedFace();
      if (!pContext.replacingClickedOnBlock() && direction.getAxis().isHorizontal()) {
         blockstate = blockstate.setValue(FACING, direction).setValue(HALF, pContext.getClickLocation().y - (double)pContext.getClickedPos().getY() > 0.5D ? Half.TOP : Half.BOTTOM);
      } else {
         blockstate = blockstate.setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP);
      }

      if (pContext.getLevel().hasNeighborSignal(pContext.getClickedPos())) {
         blockstate = blockstate.setValue(OPEN, Boolean.valueOf(true)).setValue(POWERED, Boolean.valueOf(true));
      }

      return blockstate.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, OPEN, HALF, POWERED, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
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

   //Forge Start
   @Override
   public boolean isLadder(BlockState state, net.minecraft.world.level.LevelReader world, BlockPos pos, net.minecraft.world.entity.LivingEntity entity) {
      if (state.getValue(OPEN)) {
         BlockPos downPos = pos.below();
         BlockState down = world.getBlockState(downPos);
         return down.getBlock().makesOpenTrapdoorAboveClimbable(down, world, downPos, state);
      }
      return false;
   }
   //Forge End

}
