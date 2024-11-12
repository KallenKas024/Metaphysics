package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoorBlock extends Block {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
   protected static final float AABB_DOOR_THICKNESS = 3.0F;
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);
   private final BlockSetType type;

   public DoorBlock(BlockBehaviour.Properties pProperties, BlockSetType pType) {
      super(pProperties.sound(pType.soundType()));
      this.type = pType;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, Boolean.valueOf(false)).setValue(HINGE, DoorHingeSide.LEFT).setValue(POWERED, Boolean.valueOf(false)).setValue(HALF, DoubleBlockHalf.LOWER));
   }

   public BlockSetType type() {
      return this.type;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Direction direction = pState.getValue(FACING);
      boolean flag = !pState.getValue(OPEN);
      boolean flag1 = pState.getValue(HINGE) == DoorHingeSide.RIGHT;
      switch (direction) {
         case EAST:
         default:
            return flag ? EAST_AABB : (flag1 ? NORTH_AABB : SOUTH_AABB);
         case SOUTH:
            return flag ? SOUTH_AABB : (flag1 ? EAST_AABB : WEST_AABB);
         case WEST:
            return flag ? WEST_AABB : (flag1 ? SOUTH_AABB : NORTH_AABB);
         case NORTH:
            return flag ? NORTH_AABB : (flag1 ? WEST_AABB : EAST_AABB);
      }
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      DoubleBlockHalf doubleblockhalf = pState.getValue(HALF);
      if (pFacing.getAxis() == Direction.Axis.Y && doubleblockhalf == DoubleBlockHalf.LOWER == (pFacing == Direction.UP)) {
         return pFacingState.is(this) && pFacingState.getValue(HALF) != doubleblockhalf ? pState.setValue(FACING, pFacingState.getValue(FACING)).setValue(OPEN, pFacingState.getValue(OPEN)).setValue(HINGE, pFacingState.getValue(HINGE)).setValue(POWERED, pFacingState.getValue(POWERED)) : Blocks.AIR.defaultBlockState();
      } else {
         return doubleblockhalf == DoubleBlockHalf.LOWER && pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide && pPlayer.isCreative()) {
         DoublePlantBlock.preventCreativeDropFromBottomPart(pLevel, pPos, pState, pPlayer);
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      switch (pType) {
         case LAND:
            return pState.getValue(OPEN);
         case WATER:
            return false;
         case AIR:
            return pState.getValue(OPEN);
         default:
            return false;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      Level level = pContext.getLevel();
      if (blockpos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(blockpos.above()).canBeReplaced(pContext)) {
         boolean flag = level.hasNeighborSignal(blockpos) || level.hasNeighborSignal(blockpos.above());
         return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection()).setValue(HINGE, this.getHinge(pContext)).setValue(POWERED, Boolean.valueOf(flag)).setValue(OPEN, Boolean.valueOf(flag)).setValue(HALF, DoubleBlockHalf.LOWER);
      } else {
         return null;
      }
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      pLevel.setBlock(pPos.above(), pState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
   }

   private DoorHingeSide getHinge(BlockPlaceContext pContext) {
      BlockGetter blockgetter = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      Direction direction = pContext.getHorizontalDirection();
      BlockPos blockpos1 = blockpos.above();
      Direction direction1 = direction.getCounterClockWise();
      BlockPos blockpos2 = blockpos.relative(direction1);
      BlockState blockstate = blockgetter.getBlockState(blockpos2);
      BlockPos blockpos3 = blockpos1.relative(direction1);
      BlockState blockstate1 = blockgetter.getBlockState(blockpos3);
      Direction direction2 = direction.getClockWise();
      BlockPos blockpos4 = blockpos.relative(direction2);
      BlockState blockstate2 = blockgetter.getBlockState(blockpos4);
      BlockPos blockpos5 = blockpos1.relative(direction2);
      BlockState blockstate3 = blockgetter.getBlockState(blockpos5);
      int i = (blockstate.isCollisionShapeFullBlock(blockgetter, blockpos2) ? -1 : 0) + (blockstate1.isCollisionShapeFullBlock(blockgetter, blockpos3) ? -1 : 0) + (blockstate2.isCollisionShapeFullBlock(blockgetter, blockpos4) ? 1 : 0) + (blockstate3.isCollisionShapeFullBlock(blockgetter, blockpos5) ? 1 : 0);
      boolean flag = blockstate.is(this) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER;
      boolean flag1 = blockstate2.is(this) && blockstate2.getValue(HALF) == DoubleBlockHalf.LOWER;
      if ((!flag || flag1) && i <= 0) {
         if ((!flag1 || flag) && i >= 0) {
            int j = direction.getStepX();
            int k = direction.getStepZ();
            Vec3 vec3 = pContext.getClickLocation();
            double d0 = vec3.x - (double)blockpos.getX();
            double d1 = vec3.z - (double)blockpos.getZ();
            return (j >= 0 || !(d1 < 0.5D)) && (j <= 0 || !(d1 > 0.5D)) && (k >= 0 || !(d0 > 0.5D)) && (k <= 0 || !(d0 < 0.5D)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
         } else {
            return DoorHingeSide.LEFT;
         }
      } else {
         return DoorHingeSide.RIGHT;
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (!this.type.canOpenByHand()) {
         return InteractionResult.PASS;
      } else {
         pState = pState.cycle(OPEN);
         pLevel.setBlock(pPos, pState, 10);
         this.playSound(pPlayer, pLevel, pPos, pState.getValue(OPEN));
         pLevel.gameEvent(pPlayer, this.isOpen(pState) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      }
   }

   public boolean isOpen(BlockState pState) {
      return pState.getValue(OPEN);
   }

   public void setOpen(@Nullable Entity pEntity, Level pLevel, BlockState pState, BlockPos pPos, boolean pOpen) {
      if (pState.is(this) && pState.getValue(OPEN) != pOpen) {
         pLevel.setBlock(pPos, pState.setValue(OPEN, Boolean.valueOf(pOpen)), 10);
         this.playSound(pEntity, pLevel, pPos, pOpen);
         pLevel.gameEvent(pEntity, pOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);
      }
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      boolean flag = pLevel.hasNeighborSignal(pPos) || pLevel.hasNeighborSignal(pPos.relative(pState.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN));
      if (!this.defaultBlockState().is(pBlock) && flag != pState.getValue(POWERED)) {
         if (flag != pState.getValue(OPEN)) {
            this.playSound((Entity)null, pLevel, pPos, flag);
            pLevel.gameEvent((Entity)null, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);
         }

         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)).setValue(OPEN, Boolean.valueOf(flag)), 2);
      }

   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return pState.getValue(HALF) == DoubleBlockHalf.LOWER ? blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP) : blockstate.is(this);
   }

   private void playSound(@Nullable Entity pSource, Level pLevel, BlockPos pPos, boolean pIsOpening) {
      pLevel.playSound(pSource, pPos, pIsOpening ? this.type.doorOpen() : this.type.doorClose(), SoundSource.BLOCKS, 1.0F, pLevel.getRandom().nextFloat() * 0.1F + 0.9F);
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
      return pMirror == Mirror.NONE ? pState : pState.rotate(pMirror.getRotation(pState.getValue(FACING))).cycle(HINGE);
   }

   /**
    * Return a random long to be passed to {@link net.minecraft.client.resources.model.BakedModel#getQuads}, used for
    * random model rotations
    */
   public long getSeed(BlockState pState, BlockPos pPos) {
      return Mth.getSeed(pPos.getX(), pPos.below(pState.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), pPos.getZ());
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HALF, FACING, OPEN, HINGE, POWERED);
   }

   public static boolean isWoodenDoor(Level pLevel, BlockPos pPos) {
      return isWoodenDoor(pLevel.getBlockState(pPos));
   }

   public static boolean isWoodenDoor(BlockState pState) {
      Block block = pState.getBlock();
      if (block instanceof DoorBlock doorblock) {
         if (doorblock.type().canOpenByHand()) {
            return true;
         }
      }

      return false;
   }
}