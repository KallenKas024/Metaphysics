package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FenceGateBlock extends HorizontalDirectionalBlock {
   public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty IN_WALL = BlockStateProperties.IN_WALL;
   protected static final VoxelShape Z_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   protected static final VoxelShape X_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
   protected static final VoxelShape Z_SHAPE_LOW = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 13.0D, 10.0D);
   protected static final VoxelShape X_SHAPE_LOW = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 13.0D, 16.0D);
   protected static final VoxelShape Z_COLLISION_SHAPE = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 24.0D, 10.0D);
   protected static final VoxelShape X_COLLISION_SHAPE = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 24.0D, 16.0D);
   protected static final VoxelShape Z_SUPPORT_SHAPE = Block.box(0.0D, 5.0D, 6.0D, 16.0D, 24.0D, 10.0D);
   protected static final VoxelShape X_SUPPORT_SHAPE = Block.box(6.0D, 5.0D, 0.0D, 10.0D, 24.0D, 16.0D);
   protected static final VoxelShape Z_OCCLUSION_SHAPE = Shapes.or(Block.box(0.0D, 5.0D, 7.0D, 2.0D, 16.0D, 9.0D), Block.box(14.0D, 5.0D, 7.0D, 16.0D, 16.0D, 9.0D));
   protected static final VoxelShape X_OCCLUSION_SHAPE = Shapes.or(Block.box(7.0D, 5.0D, 0.0D, 9.0D, 16.0D, 2.0D), Block.box(7.0D, 5.0D, 14.0D, 9.0D, 16.0D, 16.0D));
   protected static final VoxelShape Z_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(0.0D, 2.0D, 7.0D, 2.0D, 13.0D, 9.0D), Block.box(14.0D, 2.0D, 7.0D, 16.0D, 13.0D, 9.0D));
   protected static final VoxelShape X_OCCLUSION_SHAPE_LOW = Shapes.or(Block.box(7.0D, 2.0D, 0.0D, 9.0D, 13.0D, 2.0D), Block.box(7.0D, 2.0D, 14.0D, 9.0D, 13.0D, 16.0D));
   private final net.minecraft.sounds.SoundEvent openSound;
   private final net.minecraft.sounds.SoundEvent closeSound;

   public FenceGateBlock(BlockBehaviour.Properties pProperties, WoodType pType) {
      this(pProperties.sound(pType.soundType()), pType.fenceGateOpen(), pType.fenceGateClose());
   }
   public FenceGateBlock(BlockBehaviour.Properties props, net.minecraft.sounds.SoundEvent openSound, net.minecraft.sounds.SoundEvent closeSound) {
      super(props);
      this.openSound = openSound;
      this.closeSound = closeSound;
      this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)).setValue(IN_WALL, Boolean.valueOf(false)));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (pState.getValue(IN_WALL)) {
         return pState.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE_LOW : Z_SHAPE_LOW;
      } else {
         return pState.getValue(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
      }
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      Direction.Axis direction$axis = pFacing.getAxis();
      if (pState.getValue(FACING).getClockWise().getAxis() != direction$axis) {
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         boolean flag = this.isWall(pFacingState) || this.isWall(pLevel.getBlockState(pCurrentPos.relative(pFacing.getOpposite())));
         return pState.setValue(IN_WALL, Boolean.valueOf(flag));
      }
   }

   public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      if (pState.getValue(OPEN)) {
         return Shapes.empty();
      } else {
         return pState.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_SUPPORT_SHAPE : X_SUPPORT_SHAPE;
      }
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (pState.getValue(OPEN)) {
         return Shapes.empty();
      } else {
         return pState.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_COLLISION_SHAPE : X_COLLISION_SHAPE;
      }
   }

   public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      if (pState.getValue(IN_WALL)) {
         return pState.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE_LOW : Z_OCCLUSION_SHAPE_LOW;
      } else {
         return pState.getValue(FACING).getAxis() == Direction.Axis.X ? X_OCCLUSION_SHAPE : Z_OCCLUSION_SHAPE;
      }
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

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      boolean flag = level.hasNeighborSignal(blockpos);
      Direction direction = pContext.getHorizontalDirection();
      Direction.Axis direction$axis = direction.getAxis();
      boolean flag1 = direction$axis == Direction.Axis.Z && (this.isWall(level.getBlockState(blockpos.west())) || this.isWall(level.getBlockState(blockpos.east()))) || direction$axis == Direction.Axis.X && (this.isWall(level.getBlockState(blockpos.north())) || this.isWall(level.getBlockState(blockpos.south())));
      return this.defaultBlockState().setValue(FACING, direction).setValue(OPEN, Boolean.valueOf(flag)).setValue(POWERED, Boolean.valueOf(flag)).setValue(IN_WALL, Boolean.valueOf(flag1));
   }

   private boolean isWall(BlockState pState) {
      return pState.is(BlockTags.WALLS);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pState.getValue(OPEN)) {
         pState = pState.setValue(OPEN, Boolean.valueOf(false));
         pLevel.setBlock(pPos, pState, 10);
      } else {
         Direction direction = pPlayer.getDirection();
         if (pState.getValue(FACING) == direction.getOpposite()) {
            pState = pState.setValue(FACING, direction);
         }

         pState = pState.setValue(OPEN, Boolean.valueOf(true));
         pLevel.setBlock(pPos, pState, 10);
      }

      boolean flag = pState.getValue(OPEN);
      pLevel.playSound(pPlayer, pPos, flag ? this.openSound : this.closeSound, SoundSource.BLOCKS, 1.0F, pLevel.getRandom().nextFloat() * 0.1F + 0.9F);
      pLevel.gameEvent(pPlayer, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);
      return InteractionResult.sidedSuccess(pLevel.isClientSide);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         boolean flag = pLevel.hasNeighborSignal(pPos);
         if (pState.getValue(POWERED) != flag) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)).setValue(OPEN, Boolean.valueOf(flag)), 2);
            if (pState.getValue(OPEN) != flag) {
               pLevel.playSound((Player)null, pPos, flag ? this.openSound : this.closeSound, SoundSource.BLOCKS, 1.0F, pLevel.getRandom().nextFloat() * 0.1F + 0.9F);
               pLevel.gameEvent((Entity)null, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pPos);
            }
         }

      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, OPEN, POWERED, IN_WALL);
   }

   public static boolean connectsToDirection(BlockState pState, Direction pDirection) {
      return pState.getValue(FACING).getAxis() == pDirection.getClockWise().getAxis();
   }
}
