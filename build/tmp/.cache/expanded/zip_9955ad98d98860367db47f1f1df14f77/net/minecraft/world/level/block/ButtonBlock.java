package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ButtonBlock extends FaceAttachedHorizontalDirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int PRESSED_DEPTH = 1;
   private static final int UNPRESSED_DEPTH = 2;
   protected static final int HALF_AABB_HEIGHT = 2;
   protected static final int HALF_AABB_WIDTH = 3;
   protected static final VoxelShape CEILING_AABB_X = Block.box(6.0D, 14.0D, 5.0D, 10.0D, 16.0D, 11.0D);
   protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0D, 14.0D, 6.0D, 11.0D, 16.0D, 10.0D);
   protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0D, 0.0D, 5.0D, 10.0D, 2.0D, 11.0D);
   protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 2.0D, 10.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 6.0D, 14.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 6.0D, 0.0D, 11.0D, 10.0D, 2.0D);
   protected static final VoxelShape WEST_AABB = Block.box(14.0D, 6.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 6.0D, 5.0D, 2.0D, 10.0D, 11.0D);
   protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0D, 15.0D, 5.0D, 10.0D, 16.0D, 11.0D);
   protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0D, 15.0D, 6.0D, 11.0D, 16.0D, 10.0D);
   protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0D, 0.0D, 5.0D, 10.0D, 1.0D, 11.0D);
   protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 1.0D, 10.0D);
   protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0D, 6.0D, 15.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0D, 6.0D, 0.0D, 11.0D, 10.0D, 1.0D);
   protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0D, 6.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0D, 6.0D, 5.0D, 1.0D, 10.0D, 11.0D);
   private final BlockSetType type;
   private final int ticksToStayPressed;
   private final boolean arrowsCanPress;

   public ButtonBlock(BlockBehaviour.Properties pProperties, BlockSetType pType, int pTicksToStayPressed, boolean pArrowsCanPress) {
      super(pProperties.sound(pType.soundType()));
      this.type = pType;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL));
      this.ticksToStayPressed = pTicksToStayPressed;
      this.arrowsCanPress = pArrowsCanPress;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      Direction direction = pState.getValue(FACING);
      boolean flag = pState.getValue(POWERED);
      switch ((AttachFace)pState.getValue(FACE)) {
         case FLOOR:
            if (direction.getAxis() == Direction.Axis.X) {
               return flag ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
            }

            return flag ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
         case WALL:
            VoxelShape voxelshape;
            switch (direction) {
               case EAST:
                  voxelshape = flag ? PRESSED_EAST_AABB : EAST_AABB;
                  break;
               case WEST:
                  voxelshape = flag ? PRESSED_WEST_AABB : WEST_AABB;
                  break;
               case SOUTH:
                  voxelshape = flag ? PRESSED_SOUTH_AABB : SOUTH_AABB;
                  break;
               case NORTH:
               case UP:
               case DOWN:
                  voxelshape = flag ? PRESSED_NORTH_AABB : NORTH_AABB;
                  break;
               default:
                  throw new IncompatibleClassChangeError();
            }

            return voxelshape;
         case CEILING:
         default:
            if (direction.getAxis() == Direction.Axis.X) {
               return flag ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
            } else {
               return flag ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
            }
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pState.getValue(POWERED)) {
         return InteractionResult.CONSUME;
      } else {
         this.press(pState, pLevel, pPos);
         this.playSound(pPlayer, pLevel, pPos, true);
         pLevel.gameEvent(pPlayer, GameEvent.BLOCK_ACTIVATE, pPos);
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      }
   }

   public void press(BlockState pState, Level pLevel, BlockPos pPos) {
      pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 3);
      this.updateNeighbours(pState, pLevel, pPos);
      pLevel.scheduleTick(pPos, this, this.ticksToStayPressed);
   }

   protected void playSound(@Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos, boolean pHitByArrow) {
      pLevel.playSound(pHitByArrow ? pPlayer : null, pPos, this.getSound(pHitByArrow), SoundSource.BLOCKS);
   }

   protected SoundEvent getSound(boolean pIsOn) {
      return pIsOn ? this.type.buttonClickOn() : this.type.buttonClickOff();
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

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(POWERED)) {
         this.checkPressed(pState, pLevel, pPos);
      }
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide && this.arrowsCanPress && !pState.getValue(POWERED)) {
         this.checkPressed(pState, pLevel, pPos);
      }
   }

   protected void checkPressed(BlockState pState, Level pLevel, BlockPos pPos) {
      AbstractArrow abstractarrow = this.arrowsCanPress ? pLevel.getEntitiesOfClass(AbstractArrow.class, pState.getShape(pLevel, pPos).bounds().move(pPos)).stream().findFirst().orElse((AbstractArrow)null) : null;
      boolean flag = abstractarrow != null;
      boolean flag1 = pState.getValue(POWERED);
      if (flag != flag1) {
         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 3);
         this.updateNeighbours(pState, pLevel, pPos);
         this.playSound((Player)null, pLevel, pPos, flag);
         pLevel.gameEvent(abstractarrow, flag ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pPos);
      }

      if (flag) {
         pLevel.scheduleTick(new BlockPos(pPos), this, this.ticksToStayPressed);
      }

   }

   private void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
      pLevel.updateNeighborsAt(pPos, this);
      pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, POWERED, FACE);
   }
}