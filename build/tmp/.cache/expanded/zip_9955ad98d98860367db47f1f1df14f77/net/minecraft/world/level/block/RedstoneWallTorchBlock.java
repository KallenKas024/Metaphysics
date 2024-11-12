package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedstoneWallTorchBlock extends RedstoneTorchBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   public RedstoneWallTorchBlock(BlockBehaviour.Properties p_55744_) {
      super(p_55744_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(true)));
   }

   /**
    * @return the description ID of this block, for use with language files.
    */
   public String getDescriptionId() {
      return this.asItem().getDescriptionId();
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return WallTorchBlock.getShape(pState);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return Blocks.WALL_TORCH.canSurvive(pState, pLevel, pPos);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return Blocks.WALL_TORCH.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = Blocks.WALL_TORCH.getStateForPlacement(pContext);
      return blockstate == null ? null : this.defaultBlockState().setValue(FACING, blockstate.getValue(FACING));
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(LIT)) {
         Direction direction = pState.getValue(FACING).getOpposite();
         double d0 = 0.27D;
         double d1 = (double)pPos.getX() + 0.5D + (pRandom.nextDouble() - 0.5D) * 0.2D + 0.27D * (double)direction.getStepX();
         double d2 = (double)pPos.getY() + 0.7D + (pRandom.nextDouble() - 0.5D) * 0.2D + 0.22D;
         double d3 = (double)pPos.getZ() + 0.5D + (pRandom.nextDouble() - 0.5D) * 0.2D + 0.27D * (double)direction.getStepZ();
         pLevel.addParticle(this.flameParticle, d1, d2, d3, 0.0D, 0.0D, 0.0D);
      }
   }

   protected boolean hasNeighborSignal(Level pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING).getOpposite();
      return pLevel.hasSignal(pPos.relative(direction), direction);
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
      return pBlockState.getValue(LIT) && pBlockState.getValue(FACING) != pSide ? 15 : 0;
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return Blocks.WALL_TORCH.rotate(pState, pRotation);
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return Blocks.WALL_TORCH.mirror(pState, pMirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, LIT);
   }
}