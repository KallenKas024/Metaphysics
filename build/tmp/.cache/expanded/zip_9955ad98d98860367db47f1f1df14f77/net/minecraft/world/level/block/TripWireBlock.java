package net.minecraft.world.level.block;

import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireBlock extends Block {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   public static final BooleanProperty DISARMED = BlockStateProperties.DISARMED;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = CrossCollisionBlock.PROPERTY_BY_DIRECTION;
   protected static final VoxelShape AABB = Block.box(0.0D, 1.0D, 0.0D, 16.0D, 2.5D, 16.0D);
   protected static final VoxelShape NOT_ATTACHED_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private static final int RECHECK_PERIOD = 10;
   private final TripWireHookBlock hook;

   public TripWireBlock(TripWireHookBlock pHook, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false)).setValue(DISARMED, Boolean.valueOf(false)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)));
      this.hook = pHook;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return pState.getValue(ATTACHED) ? AABB : NOT_ATTACHED_AABB;
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockGetter blockgetter = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      return this.defaultBlockState().setValue(NORTH, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.north()), Direction.NORTH))).setValue(EAST, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.east()), Direction.EAST))).setValue(SOUTH, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.south()), Direction.SOUTH))).setValue(WEST, Boolean.valueOf(this.shouldConnectTo(blockgetter.getBlockState(blockpos.west()), Direction.WEST)));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing.getAxis().isHorizontal() ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(this.shouldConnectTo(pFacingState, pFacing))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.updateSource(pLevel, pPos, pState);
      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         this.updateSource(pLevel, pPos, pState.setValue(POWERED, Boolean.valueOf(true)));
      }
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide && !pPlayer.getMainHandItem().isEmpty() && pPlayer.getMainHandItem().canPerformAction(net.minecraftforge.common.ToolActions.SHEARS_DISARM)) {
         pLevel.setBlock(pPos, pState.setValue(DISARMED, Boolean.valueOf(true)), 4);
         pLevel.gameEvent(pPlayer, GameEvent.SHEAR, pPos);
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   private void updateSource(Level pLevel, BlockPos pPos, BlockState pState) {
      for(Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST}) {
         for(int i = 1; i < 42; ++i) {
            BlockPos blockpos = pPos.relative(direction, i);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (blockstate.is(this.hook)) {
               if (blockstate.getValue(TripWireHookBlock.FACING) == direction.getOpposite()) {
                  this.hook.calculateState(pLevel, blockpos, blockstate, false, true, i, pState);
               }
               break;
            }

            if (!blockstate.is(this)) {
               break;
            }
         }
      }

   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide) {
         if (!pState.getValue(POWERED)) {
            this.checkPressed(pLevel, pPos);
         }
      }
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.getBlockState(pPos).getValue(POWERED)) {
         this.checkPressed(pLevel, pPos);
      }
   }

   private void checkPressed(Level pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      boolean flag = blockstate.getValue(POWERED);
      boolean flag1 = false;
      List<? extends Entity> list = pLevel.getEntities((Entity)null, blockstate.getShape(pLevel, pPos).bounds().move(pPos));
      if (!list.isEmpty()) {
         for(Entity entity : list) {
            if (!entity.isIgnoringBlockTriggers()) {
               flag1 = true;
               break;
            }
         }
      }

      if (flag1 != flag) {
         blockstate = blockstate.setValue(POWERED, Boolean.valueOf(flag1));
         pLevel.setBlock(pPos, blockstate, 3);
         this.updateSource(pLevel, pPos, blockstate);
      }

      if (flag1) {
         pLevel.scheduleTick(new BlockPos(pPos), this, 10);
      }

   }

   public boolean shouldConnectTo(BlockState pState, Direction pDirection) {
      if (pState.is(this.hook)) {
         return pState.getValue(TripWireHookBlock.FACING) == pDirection.getOpposite();
      } else {
         return pState.is(this);
      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRot) {
      switch (pRot) {
         case CLOCKWISE_180:
            return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(EAST, pState.getValue(WEST)).setValue(SOUTH, pState.getValue(NORTH)).setValue(WEST, pState.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return pState.setValue(NORTH, pState.getValue(EAST)).setValue(EAST, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(WEST)).setValue(WEST, pState.getValue(NORTH));
         case CLOCKWISE_90:
            return pState.setValue(NORTH, pState.getValue(WEST)).setValue(EAST, pState.getValue(NORTH)).setValue(SOUTH, pState.getValue(EAST)).setValue(WEST, pState.getValue(SOUTH));
         default:
            return pState;
      }
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      switch (pMirror) {
         case LEFT_RIGHT:
            return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(NORTH));
         case FRONT_BACK:
            return pState.setValue(EAST, pState.getValue(WEST)).setValue(WEST, pState.getValue(EAST));
         default:
            return super.mirror(pState, pMirror);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
   }
}
