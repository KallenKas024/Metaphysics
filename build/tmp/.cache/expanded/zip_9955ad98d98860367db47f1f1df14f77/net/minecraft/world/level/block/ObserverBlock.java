package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class ObserverBlock extends DirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public ObserverBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(POWERED, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, POWERED);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRot) {
      return pState.setValue(FACING, pRot.rotate(pState.getValue(FACING)));
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

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(POWERED)) {
         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)), 2);
      } else {
         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 2);
         pLevel.scheduleTick(pPos, this, 2);
      }

      this.updateNeighborsInFront(pLevel, pPos, pState);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(FACING) == pFacing && !pState.getValue(POWERED)) {
         this.startSignal(pLevel, pCurrentPos);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   private void startSignal(LevelAccessor pLevel, BlockPos pPos) {
      if (!pLevel.isClientSide() && !pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
         pLevel.scheduleTick(pPos, this, 2);
      }

   }

   protected void updateNeighborsInFront(Level pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING);
      BlockPos blockpos = pPos.relative(direction.getOpposite());
      pLevel.neighborChanged(blockpos, this, pPos);
      pLevel.updateNeighborsAtExceptFromFacing(blockpos, this, direction);
   }

   /**
    * Returns whether this block is capable of emitting redstone signals.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#isSignalSource}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   /**
    * Returns the direct signal this block emits in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getDirectSignal}
    * whenever possible. Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getSignal(pBlockAccess, pPos, pSide);
   }

   /**
    * Returns the signal this block emits in the given direction.
    * 
    * <p>
    * NOTE: directions in redstone signal related methods are backwards, so this method
    * checks for the signal emitted in the <i>opposite</i> direction of the one given.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getSignal}
    * whenever possible. Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getValue(POWERED) && pBlockState.getValue(FACING) == pSide ? 15 : 0;
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pState.is(pOldState.getBlock())) {
         if (!pLevel.isClientSide() && pState.getValue(POWERED) && !pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
            BlockState blockstate = pState.setValue(POWERED, Boolean.valueOf(false));
            pLevel.setBlock(pPos, blockstate, 18);
            this.updateNeighborsInFront(pLevel, pPos, blockstate);
         }

      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         if (!pLevel.isClientSide && pState.getValue(POWERED) && pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
            this.updateNeighborsInFront(pLevel, pPos, pState.setValue(POWERED, Boolean.valueOf(false)));
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite().getOpposite());
   }
}