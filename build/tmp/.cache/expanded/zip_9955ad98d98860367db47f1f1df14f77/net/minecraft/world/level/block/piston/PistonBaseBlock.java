package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock extends DirectionalBlock {
   public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
   public static final int TRIGGER_EXTEND = 0;
   public static final int TRIGGER_CONTRACT = 1;
   public static final int TRIGGER_DROP = 2;
   public static final float PLATFORM_THICKNESS = 4.0F;
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape UP_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   /** Whether this is a sticky piston */
   private final boolean isSticky;

   public PistonBaseBlock(boolean pIsSticky, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = pIsSticky;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (pState.getValue(EXTENDED)) {
         switch ((Direction)pState.getValue(FACING)) {
            case DOWN:
               return DOWN_AABB;
            case UP:
            default:
               return UP_AABB;
            case NORTH:
               return NORTH_AABB;
            case SOUTH:
               return SOUTH_AABB;
            case WEST:
               return WEST_AABB;
            case EAST:
               return EAST_AABB;
         }
      } else {
         return Shapes.block();
      }
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (!pLevel.isClientSide) {
         this.checkIfExtend(pLevel, pPos, pState);
      }

   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         this.checkIfExtend(pLevel, pPos, pState);
      }

   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) == null) {
            this.checkIfExtend(pLevel, pPos, pState);
         }

      }
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
   }

   private void checkIfExtend(Level pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING);
      boolean flag = this.getNeighborSignal(pLevel, pPos, direction);
      if (flag && !pState.getValue(EXTENDED)) {
         if ((new PistonStructureResolver(pLevel, pPos, direction, true)).resolve()) {
            pLevel.blockEvent(pPos, this, 0, direction.get3DDataValue());
         }
      } else if (!flag && pState.getValue(EXTENDED)) {
         BlockPos blockpos = pPos.relative(direction, 2);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         int i = 1;
         if (blockstate.is(Blocks.MOVING_PISTON) && blockstate.getValue(FACING) == direction) {
            BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
            if (blockentity instanceof PistonMovingBlockEntity) {
               PistonMovingBlockEntity pistonmovingblockentity = (PistonMovingBlockEntity)blockentity;
               if (pistonmovingblockentity.isExtending() && (pistonmovingblockentity.getProgress(0.0F) < 0.5F || pLevel.getGameTime() == pistonmovingblockentity.getLastTicked() || ((ServerLevel)pLevel).isHandlingTick())) {
                  i = 2;
               }
            }
         }

         pLevel.blockEvent(pPos, this, i, direction.get3DDataValue());
      }

   }

   private boolean getNeighborSignal(SignalGetter pSignalGetter, BlockPos pPos, Direction pDirection) {
      for(Direction direction : Direction.values()) {
         if (direction != pDirection && pSignalGetter.hasSignal(pPos.relative(direction), direction)) {
            return true;
         }
      }

      if (pSignalGetter.hasSignal(pPos, Direction.DOWN)) {
         return true;
      } else {
         BlockPos blockpos = pPos.above();

         for(Direction direction1 : Direction.values()) {
            if (direction1 != Direction.DOWN && pSignalGetter.hasSignal(blockpos.relative(direction1), direction1)) {
               return true;
            }
         }

         return false;
      }
   }

   /**
    * Called on server when {@link net.minecraft.world.level.Level#blockEvent} is called. If server returns true, then
    * also called on the client. On the Server, this may perform additional changes to the world, like pistons replacing
    * the block with an extended base. On the client, the update may involve replacing block entities or effects such as
    * sounds or particles
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#triggerEvent}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
      Direction direction = pState.getValue(FACING);
      BlockState blockstate = pState.setValue(EXTENDED, Boolean.valueOf(true));
      if (!pLevel.isClientSide) {
         boolean flag = this.getNeighborSignal(pLevel, pPos, direction);
         if (flag && (pId == 1 || pId == 2)) {
            pLevel.setBlock(pPos, blockstate, 2);
            return false;
         }

         if (!flag && pId == 0) {
            return false;
         }
      }

      if (pId == 0) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(pLevel, pPos, direction, true)) return false;
         if (!this.moveBlocks(pLevel, pPos, direction, true)) {
            return false;
         }

         pLevel.setBlock(pPos, blockstate, 67);
         pLevel.playSound((Player)null, pPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.25F + 0.6F);
         pLevel.gameEvent(GameEvent.BLOCK_ACTIVATE, pPos, GameEvent.Context.of(blockstate));
      } else if (pId == 1 || pId == 2) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(pLevel, pPos, direction, false)) return false;
         BlockEntity blockentity1 = pLevel.getBlockEntity(pPos.relative(direction));
         if (blockentity1 instanceof PistonMovingBlockEntity) {
            ((PistonMovingBlockEntity)blockentity1).finalTick();
         }

         BlockState blockstate1 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
         pLevel.setBlock(pPos, blockstate1, 20);
         pLevel.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(pPos, blockstate1, this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(pParam & 7)), direction, false, true));
         pLevel.blockUpdated(pPos, blockstate1.getBlock());
         blockstate1.updateNeighbourShapes(pLevel, pPos, 2);
         if (this.isSticky) {
            BlockPos blockpos = pPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
            BlockState blockstate2 = pLevel.getBlockState(blockpos);
            boolean flag1 = false;
            if (blockstate2.is(Blocks.MOVING_PISTON)) {
               BlockEntity blockentity = pLevel.getBlockEntity(blockpos);
               if (blockentity instanceof PistonMovingBlockEntity) {
                  PistonMovingBlockEntity pistonmovingblockentity = (PistonMovingBlockEntity)blockentity;
                  if (pistonmovingblockentity.getDirection() == direction && pistonmovingblockentity.isExtending()) {
                     pistonmovingblockentity.finalTick();
                     flag1 = true;
                  }
               }
            }

            if (!flag1) {
               if (pId != 1 || blockstate2.isAir() || !isPushable(blockstate2, pLevel, blockpos, direction.getOpposite(), false, direction) || blockstate2.getPistonPushReaction() != PushReaction.NORMAL && !blockstate2.is(Blocks.PISTON) && !blockstate2.is(Blocks.STICKY_PISTON)) {
                  pLevel.removeBlock(pPos.relative(direction), false);
               } else {
                  this.moveBlocks(pLevel, pPos, direction, false);
               }
            }
         } else {
            pLevel.removeBlock(pPos.relative(direction), false);
         }

         pLevel.playSound((Player)null, pPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.15F + 0.6F);
         pLevel.gameEvent(GameEvent.BLOCK_DEACTIVATE, pPos, GameEvent.Context.of(blockstate1));
      }

      net.minecraftforge.event.ForgeEventFactory.onPistonMovePost(pLevel, pPos, direction, (pId == 0));
      return true;
   }

   /**
    * Checks if the piston can push the given BlockState.
    */
   public static boolean isPushable(BlockState pState, Level pLevel, BlockPos pPos, Direction pMovementDirection, boolean pAllowDestroy, Direction pPistonFacing) {
      if (pPos.getY() >= pLevel.getMinBuildHeight() && pPos.getY() <= pLevel.getMaxBuildHeight() - 1 && pLevel.getWorldBorder().isWithinBounds(pPos)) {
         if (pState.isAir()) {
            return true;
         } else if (!pState.is(Blocks.OBSIDIAN) && !pState.is(Blocks.CRYING_OBSIDIAN) && !pState.is(Blocks.RESPAWN_ANCHOR) && !pState.is(Blocks.REINFORCED_DEEPSLATE)) {
            if (pMovementDirection == Direction.DOWN && pPos.getY() == pLevel.getMinBuildHeight()) {
               return false;
            } else if (pMovementDirection == Direction.UP && pPos.getY() == pLevel.getMaxBuildHeight() - 1) {
               return false;
            } else {
               if (!pState.is(Blocks.PISTON) && !pState.is(Blocks.STICKY_PISTON)) {
                  if (pState.getDestroySpeed(pLevel, pPos) == -1.0F) {
                     return false;
                  }

                  switch (pState.getPistonPushReaction()) {
                     case BLOCK:
                        return false;
                     case DESTROY:
                        return pAllowDestroy;
                     case PUSH_ONLY:
                        return pMovementDirection == pPistonFacing;
                  }
               } else if (pState.getValue(EXTENDED)) {
                  return false;
               }

               return !pState.hasBlockEntity();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean moveBlocks(Level pLevel, BlockPos pPos, Direction pFacing, boolean pExtending) {
      BlockPos blockpos = pPos.relative(pFacing);
      if (!pExtending && pLevel.getBlockState(blockpos).is(Blocks.PISTON_HEAD)) {
         pLevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 20);
      }

      PistonStructureResolver pistonstructureresolver = new PistonStructureResolver(pLevel, pPos, pFacing, pExtending);
      if (!pistonstructureresolver.resolve()) {
         return false;
      } else {
         Map<BlockPos, BlockState> map = Maps.newHashMap();
         List<BlockPos> list = pistonstructureresolver.getToPush();
         List<BlockState> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockpos1 = list.get(i);
            BlockState blockstate = pLevel.getBlockState(blockpos1);
            list1.add(blockstate);
            map.put(blockpos1, blockstate);
         }

         List<BlockPos> list2 = pistonstructureresolver.getToDestroy();
         BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
         Direction direction = pExtending ? pFacing : pFacing.getOpposite();
         int j = 0;

         for(int k = list2.size() - 1; k >= 0; --k) {
            BlockPos blockpos2 = list2.get(k);
            BlockState blockstate1 = pLevel.getBlockState(blockpos2);
            BlockEntity blockentity = blockstate1.hasBlockEntity() ? pLevel.getBlockEntity(blockpos2) : null;
            dropResources(blockstate1, pLevel, blockpos2, blockentity);
            pLevel.setBlock(blockpos2, Blocks.AIR.defaultBlockState(), 18);
            pLevel.gameEvent(GameEvent.BLOCK_DESTROY, blockpos2, GameEvent.Context.of(blockstate1));
            if (!blockstate1.is(BlockTags.FIRE)) {
               pLevel.addDestroyBlockEffect(blockpos2, blockstate1);
            }

            ablockstate[j++] = blockstate1;
         }

         for(int l = list.size() - 1; l >= 0; --l) {
            BlockPos blockpos3 = list.get(l);
            BlockState blockstate5 = pLevel.getBlockState(blockpos3);
            blockpos3 = blockpos3.relative(direction);
            map.remove(blockpos3);
            BlockState blockstate8 = Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, pFacing);
            pLevel.setBlock(blockpos3, blockstate8, 68);
            pLevel.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockpos3, blockstate8, list1.get(l), pFacing, pExtending, false));
            ablockstate[j++] = blockstate5;
         }

         if (pExtending) {
            PistonType pistontype = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockstate4 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, pFacing).setValue(PistonHeadBlock.TYPE, pistontype);
            BlockState blockstate6 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, pFacing).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(blockpos);
            pLevel.setBlock(blockpos, blockstate6, 68);
            pLevel.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockpos, blockstate6, blockstate4, pFacing, true, true));
         }

         BlockState blockstate3 = Blocks.AIR.defaultBlockState();

         for(BlockPos blockpos4 : map.keySet()) {
            pLevel.setBlock(blockpos4, blockstate3, 82);
         }

         for(Map.Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockpos5 = entry.getKey();
            BlockState blockstate2 = entry.getValue();
            blockstate2.updateIndirectNeighbourShapes(pLevel, blockpos5, 2);
            blockstate3.updateNeighbourShapes(pLevel, blockpos5, 2);
            blockstate3.updateIndirectNeighbourShapes(pLevel, blockpos5, 2);
         }

         j = 0;

         for(int i1 = list2.size() - 1; i1 >= 0; --i1) {
            BlockState blockstate7 = ablockstate[j++];
            BlockPos blockpos6 = list2.get(i1);
            blockstate7.updateIndirectNeighbourShapes(pLevel, blockpos6, 2);
            pLevel.updateNeighborsAt(blockpos6, blockstate7.getBlock());
         }

         for(int j1 = list.size() - 1; j1 >= 0; --j1) {
            pLevel.updateNeighborsAt(list.get(j1), ablockstate[j++].getBlock());
         }

         if (pExtending) {
            pLevel.updateNeighborsAt(blockpos, Blocks.PISTON_HEAD);
         }

         return true;
      }
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

   public BlockState rotate(BlockState state, net.minecraft.world.level.LevelAccessor world, BlockPos pos, Rotation direction) {
       return state.getValue(EXTENDED) ? state : super.rotate(state, world, pos, direction);
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
      pBuilder.add(FACING, EXTENDED);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return pState.getValue(EXTENDED);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}
