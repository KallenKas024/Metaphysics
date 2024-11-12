package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BellBlock extends BaseEntityBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 12.0D);
   private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
   private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0D, 6.0D, 5.0D, 11.0D, 13.0D, 11.0D);
   private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 6.0D, 12.0D);
   private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
   private static final VoxelShape NORTH_SOUTH_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 16.0D));
   private static final VoxelShape EAST_WEST_BETWEEN = Shapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
   private static final VoxelShape TO_WEST = Shapes.or(BELL_SHAPE, Block.box(0.0D, 13.0D, 7.0D, 13.0D, 15.0D, 9.0D));
   private static final VoxelShape TO_EAST = Shapes.or(BELL_SHAPE, Block.box(3.0D, 13.0D, 7.0D, 16.0D, 15.0D, 9.0D));
   private static final VoxelShape TO_NORTH = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 0.0D, 9.0D, 15.0D, 13.0D));
   private static final VoxelShape TO_SOUTH = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 3.0D, 9.0D, 15.0D, 16.0D));
   private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE, Block.box(7.0D, 13.0D, 7.0D, 9.0D, 16.0D, 9.0D));
   public static final int EVENT_BELL_RING = 1;

   public BellBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ATTACHMENT, BellAttachType.FLOOR).setValue(POWERED, Boolean.valueOf(false)));
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      boolean flag = pLevel.hasNeighborSignal(pPos);
      if (flag != pState.getValue(POWERED)) {
         if (flag) {
            this.attemptToRing(pLevel, pPos, (Direction)null);
         }

         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 3);
      }

   }

   public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
      Entity entity = pProjectile.getOwner();
      Player player = entity instanceof Player ? (Player)entity : null;
      this.onHit(pLevel, pState, pHit, player, true);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      return this.onHit(pLevel, pState, pHit, pPlayer, true) ? InteractionResult.sidedSuccess(pLevel.isClientSide) : InteractionResult.PASS;
   }

   public boolean onHit(Level pLevel, BlockState pState, BlockHitResult pResult, @Nullable Player pPlayer, boolean pCanRingBell) {
      Direction direction = pResult.getDirection();
      BlockPos blockpos = pResult.getBlockPos();
      boolean flag = !pCanRingBell || this.isProperHit(pState, direction, pResult.getLocation().y - (double)blockpos.getY());
      if (flag) {
         boolean flag1 = this.attemptToRing(pPlayer, pLevel, blockpos, direction);
         if (flag1 && pPlayer != null) {
            pPlayer.awardStat(Stats.BELL_RING);
         }

         return true;
      } else {
         return false;
      }
   }

   /**
    * @return true if the bell can be rung from the given side and vertical position. For example, bells attached to
    * their northern neighbor cannot be rung from the south face, since it can't swing north-south.
    */
   private boolean isProperHit(BlockState pPos, Direction pDirection, double pDistanceY) {
      if (pDirection.getAxis() != Direction.Axis.Y && !(pDistanceY > (double)0.8124F)) {
         Direction direction = pPos.getValue(FACING);
         BellAttachType bellattachtype = pPos.getValue(ATTACHMENT);
         switch (bellattachtype) {
            case FLOOR:
               return direction.getAxis() == pDirection.getAxis();
            case SINGLE_WALL:
            case DOUBLE_WALL:
               return direction.getAxis() != pDirection.getAxis();
            case CEILING:
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public boolean attemptToRing(Level pLevel, BlockPos pPos, @Nullable Direction pDirection) {
      return this.attemptToRing((Entity)null, pLevel, pPos, pDirection);
   }

   public boolean attemptToRing(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, @Nullable Direction pDirection) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (!pLevel.isClientSide && blockentity instanceof BellBlockEntity) {
         if (pDirection == null) {
            pDirection = pLevel.getBlockState(pPos).getValue(FACING);
         }

         ((BellBlockEntity)blockentity).onHit(pDirection);
         pLevel.playSound((Player)null, pPos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0F, 1.0F);
         pLevel.gameEvent(pEntity, GameEvent.BLOCK_CHANGE, pPos);
         return true;
      } else {
         return false;
      }
   }

   private VoxelShape getVoxelShape(BlockState pState) {
      Direction direction = pState.getValue(FACING);
      BellAttachType bellattachtype = pState.getValue(ATTACHMENT);
      if (bellattachtype == BellAttachType.FLOOR) {
         return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_FLOOR_SHAPE : NORTH_SOUTH_FLOOR_SHAPE;
      } else if (bellattachtype == BellAttachType.CEILING) {
         return CEILING_SHAPE;
      } else if (bellattachtype == BellAttachType.DOUBLE_WALL) {
         return direction != Direction.NORTH && direction != Direction.SOUTH ? EAST_WEST_BETWEEN : NORTH_SOUTH_BETWEEN;
      } else if (direction == Direction.NORTH) {
         return TO_NORTH;
      } else if (direction == Direction.SOUTH) {
         return TO_SOUTH;
      } else {
         return direction == Direction.EAST ? TO_EAST : TO_WEST;
      }
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.getVoxelShape(pState);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.getVoxelShape(pState);
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

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getClickedFace();
      BlockPos blockpos = pContext.getClickedPos();
      Level level = pContext.getLevel();
      Direction.Axis direction$axis = direction.getAxis();
      if (direction$axis == Direction.Axis.Y) {
         BlockState blockstate = this.defaultBlockState().setValue(ATTACHMENT, direction == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR).setValue(FACING, pContext.getHorizontalDirection());
         if (blockstate.canSurvive(pContext.getLevel(), blockpos)) {
            return blockstate;
         }
      } else {
         boolean flag = direction$axis == Direction.Axis.X && level.getBlockState(blockpos.west()).isFaceSturdy(level, blockpos.west(), Direction.EAST) && level.getBlockState(blockpos.east()).isFaceSturdy(level, blockpos.east(), Direction.WEST) || direction$axis == Direction.Axis.Z && level.getBlockState(blockpos.north()).isFaceSturdy(level, blockpos.north(), Direction.SOUTH) && level.getBlockState(blockpos.south()).isFaceSturdy(level, blockpos.south(), Direction.NORTH);
         BlockState blockstate1 = this.defaultBlockState().setValue(FACING, direction.getOpposite()).setValue(ATTACHMENT, flag ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL);
         if (blockstate1.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
            return blockstate1;
         }

         boolean flag1 = level.getBlockState(blockpos.below()).isFaceSturdy(level, blockpos.below(), Direction.UP);
         blockstate1 = blockstate1.setValue(ATTACHMENT, flag1 ? BellAttachType.FLOOR : BellAttachType.CEILING);
         if (blockstate1.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
            return blockstate1;
         }
      }

      return null;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      BellAttachType bellattachtype = pState.getValue(ATTACHMENT);
      Direction direction = getConnectedDirection(pState).getOpposite();
      if (direction == pFacing && !pState.canSurvive(pLevel, pCurrentPos) && bellattachtype != BellAttachType.DOUBLE_WALL) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (pFacing.getAxis() == pState.getValue(FACING).getAxis()) {
            if (bellattachtype == BellAttachType.DOUBLE_WALL && !pFacingState.isFaceSturdy(pLevel, pFacingPos, pFacing)) {
               return pState.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL).setValue(FACING, pFacing.getOpposite());
            }

            if (bellattachtype == BellAttachType.SINGLE_WALL && direction.getOpposite() == pFacing && pFacingState.isFaceSturdy(pLevel, pFacingPos, pState.getValue(FACING))) {
               return pState.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
            }
         }

         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      Direction direction = getConnectedDirection(pState).getOpposite();
      return direction == Direction.UP ? Block.canSupportCenter(pLevel, pPos.above(), Direction.DOWN) : FaceAttachedHorizontalDirectionalBlock.canAttach(pLevel, pPos, direction);
   }

   private static Direction getConnectedDirection(BlockState pState) {
      switch ((BellAttachType)pState.getValue(ATTACHMENT)) {
         case FLOOR:
            return Direction.UP;
         case CEILING:
            return Direction.DOWN;
         default:
            return pState.getValue(FACING).getOpposite();
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, ATTACHMENT, POWERED);
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new BellBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.BELL, pLevel.isClientSide ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}