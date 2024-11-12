package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeverBlock extends FaceAttachedHorizontalDirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   protected static final int DEPTH = 6;
   protected static final int WIDTH = 6;
   protected static final int HEIGHT = 8;
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 4.0D, 10.0D, 11.0D, 12.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 4.0D, 0.0D, 11.0D, 12.0D, 6.0D);
   protected static final VoxelShape WEST_AABB = Block.box(10.0D, 4.0D, 5.0D, 16.0D, 12.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 4.0D, 5.0D, 6.0D, 12.0D, 11.0D);
   protected static final VoxelShape UP_AABB_Z = Block.box(5.0D, 0.0D, 4.0D, 11.0D, 6.0D, 12.0D);
   protected static final VoxelShape UP_AABB_X = Block.box(4.0D, 0.0D, 5.0D, 12.0D, 6.0D, 11.0D);
   protected static final VoxelShape DOWN_AABB_Z = Block.box(5.0D, 10.0D, 4.0D, 11.0D, 16.0D, 12.0D);
   protected static final VoxelShape DOWN_AABB_X = Block.box(4.0D, 10.0D, 5.0D, 12.0D, 16.0D, 11.0D);

   public LeverBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch ((AttachFace)pState.getValue(FACE)) {
         case FLOOR:
            switch (pState.getValue(FACING).getAxis()) {
               case X:
                  return UP_AABB_X;
               case Z:
               default:
                  return UP_AABB_Z;
            }
         case WALL:
            switch ((Direction)pState.getValue(FACING)) {
               case EAST:
                  return EAST_AABB;
               case WEST:
                  return WEST_AABB;
               case SOUTH:
                  return SOUTH_AABB;
               case NORTH:
               default:
                  return NORTH_AABB;
            }
         case CEILING:
         default:
            switch (pState.getValue(FACING).getAxis()) {
               case X:
                  return DOWN_AABB_X;
               case Z:
               default:
                  return DOWN_AABB_Z;
            }
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         BlockState blockstate1 = pState.cycle(POWERED);
         if (blockstate1.getValue(POWERED)) {
            makeParticle(blockstate1, pLevel, pPos, 1.0F);
         }

         return InteractionResult.SUCCESS;
      } else {
         BlockState blockstate = this.pull(pState, pLevel, pPos);
         float f = blockstate.getValue(POWERED) ? 0.6F : 0.5F;
         pLevel.playSound((Player)null, pPos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, f);
         pLevel.gameEvent(pPlayer, blockstate.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pPos);
         return InteractionResult.CONSUME;
      }
   }

   public BlockState pull(BlockState pState, Level pLevel, BlockPos pPos) {
      pState = pState.cycle(POWERED);
      pLevel.setBlock(pPos, pState, 3);
      this.updateNeighbours(pState, pLevel, pPos);
      return pState;
   }

   private static void makeParticle(BlockState pState, LevelAccessor pLevel, BlockPos pPos, float pAlpha) {
      Direction direction = pState.getValue(FACING).getOpposite();
      Direction direction1 = getConnectedDirection(pState).getOpposite();
      double d0 = (double)pPos.getX() + 0.5D + 0.1D * (double)direction.getStepX() + 0.2D * (double)direction1.getStepX();
      double d1 = (double)pPos.getY() + 0.5D + 0.1D * (double)direction.getStepY() + 0.2D * (double)direction1.getStepY();
      double d2 = (double)pPos.getZ() + 0.5D + 0.1D * (double)direction.getStepZ() + 0.2D * (double)direction1.getStepZ();
      pLevel.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, pAlpha), d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(POWERED) && pRandom.nextFloat() < 0.25F) {
         makeParticle(pState, pLevel, pPos, 0.5F);
      }

   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         if (pState.getValue(POWERED)) {
            this.updateNeighbours(pState, pLevel, pPos);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
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
      return pBlockState.getValue(POWERED) ? 15 : 0;
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
      return pBlockState.getValue(POWERED) && getConnectedDirection(pBlockState) == pSide ? 15 : 0;
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

   private void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
      pLevel.updateNeighborsAt(pPos, this);
      pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACE, FACING, POWERED);
   }
}