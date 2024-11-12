package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VineBlock extends Block implements net.minecraftforge.common.IForgeShearable {
   public static final BooleanProperty UP = PipeBlock.UP;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_57886_) -> {
      return p_57886_.getKey() != Direction.DOWN;
   }).collect(Util.toMap());
   protected static final float AABB_OFFSET = 1.0F;
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private final Map<BlockState, VoxelShape> shapesCache;

   public VineBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(UP, Boolean.valueOf(false)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)));
      this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::calculateShape)));
   }

   private static VoxelShape calculateShape(BlockState p_57906_) {
      VoxelShape voxelshape = Shapes.empty();
      if (p_57906_.getValue(UP)) {
         voxelshape = UP_AABB;
      }

      if (p_57906_.getValue(NORTH)) {
         voxelshape = Shapes.or(voxelshape, NORTH_AABB);
      }

      if (p_57906_.getValue(SOUTH)) {
         voxelshape = Shapes.or(voxelshape, SOUTH_AABB);
      }

      if (p_57906_.getValue(EAST)) {
         voxelshape = Shapes.or(voxelshape, EAST_AABB);
      }

      if (p_57906_.getValue(WEST)) {
         voxelshape = Shapes.or(voxelshape, WEST_AABB);
      }

      return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapesCache.get(pState);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return true;
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return this.hasFaces(this.getUpdatedState(pState, pLevel, pPos));
   }

   private boolean hasFaces(BlockState pState) {
      return this.countFaces(pState) > 0;
   }

   private int countFaces(BlockState pState) {
      int i = 0;

      for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
         if (pState.getValue(booleanproperty)) {
            ++i;
         }
      }

      return i;
   }

   private boolean canSupportAtFace(BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
      if (pDirection == Direction.DOWN) {
         return false;
      } else {
         BlockPos blockpos = pPos.relative(pDirection);
         if (isAcceptableNeighbour(pLevel, blockpos, pDirection)) {
            return true;
         } else if (pDirection.getAxis() == Direction.Axis.Y) {
            return false;
         } else {
            BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(pDirection);
            BlockState blockstate = pLevel.getBlockState(pPos.above());
            return blockstate.is(this) && blockstate.getValue(booleanproperty);
         }
      }
   }

   public static boolean isAcceptableNeighbour(BlockGetter pBlockReader, BlockPos pNeighborPos, Direction pAttachedFace) {
      return MultifaceBlock.canAttachTo(pBlockReader, pAttachedFace, pNeighborPos, pBlockReader.getBlockState(pNeighborPos));
   }

   private BlockState getUpdatedState(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.above();
      if (pState.getValue(UP)) {
         pState = pState.setValue(UP, Boolean.valueOf(isAcceptableNeighbour(pLevel, blockpos, Direction.DOWN)));
      }

      BlockState blockstate = null;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BooleanProperty booleanproperty = getPropertyForFace(direction);
         if (pState.getValue(booleanproperty)) {
            boolean flag = this.canSupportAtFace(pLevel, pPos, direction);
            if (!flag) {
               if (blockstate == null) {
                  blockstate = pLevel.getBlockState(blockpos);
               }

               flag = blockstate.is(this) && blockstate.getValue(booleanproperty);
            }

            pState = pState.setValue(booleanproperty, Boolean.valueOf(flag));
         }
      }

      return pState;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.DOWN) {
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         BlockState blockstate = this.getUpdatedState(pState, pLevel, pCurrentPos);
         return !this.hasFaces(blockstate) ? Blocks.AIR.defaultBlockState() : blockstate;
      }
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DO_VINES_SPREAD)) {
      if (pLevel.random.nextInt(4) == 0 && pLevel.isAreaLoaded(pPos, 4)) { // Forge: check area to prevent loading unloaded chunks
            Direction direction = Direction.getRandom(pRandom);
            BlockPos blockpos = pPos.above();
            if (direction.getAxis().isHorizontal() && !pState.getValue(getPropertyForFace(direction))) {
               if (this.canSpread(pLevel, pPos)) {
                  BlockPos blockpos4 = pPos.relative(direction);
                  BlockState blockstate4 = pLevel.getBlockState(blockpos4);
                  if (blockstate4.isAir()) {
                     Direction direction3 = direction.getClockWise();
                     Direction direction4 = direction.getCounterClockWise();
                     boolean flag = pState.getValue(getPropertyForFace(direction3));
                     boolean flag1 = pState.getValue(getPropertyForFace(direction4));
                     BlockPos blockpos2 = blockpos4.relative(direction3);
                     BlockPos blockpos3 = blockpos4.relative(direction4);
                     if (flag && isAcceptableNeighbour(pLevel, blockpos2, direction3)) {
                        pLevel.setBlock(blockpos4, this.defaultBlockState().setValue(getPropertyForFace(direction3), Boolean.valueOf(true)), 2);
                     } else if (flag1 && isAcceptableNeighbour(pLevel, blockpos3, direction4)) {
                        pLevel.setBlock(blockpos4, this.defaultBlockState().setValue(getPropertyForFace(direction4), Boolean.valueOf(true)), 2);
                     } else {
                        Direction direction1 = direction.getOpposite();
                        if (flag && pLevel.isEmptyBlock(blockpos2) && isAcceptableNeighbour(pLevel, pPos.relative(direction3), direction1)) {
                           pLevel.setBlock(blockpos2, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true)), 2);
                        } else if (flag1 && pLevel.isEmptyBlock(blockpos3) && isAcceptableNeighbour(pLevel, pPos.relative(direction4), direction1)) {
                           pLevel.setBlock(blockpos3, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true)), 2);
                        } else if ((double)pRandom.nextFloat() < 0.05D && isAcceptableNeighbour(pLevel, blockpos4.above(), Direction.UP)) {
                           pLevel.setBlock(blockpos4, this.defaultBlockState().setValue(UP, Boolean.valueOf(true)), 2);
                        }
                     }
                  } else if (isAcceptableNeighbour(pLevel, blockpos4, direction)) {
                     pLevel.setBlock(pPos, pState.setValue(getPropertyForFace(direction), Boolean.valueOf(true)), 2);
                  }

               }
            } else {
               if (direction == Direction.UP && pPos.getY() < pLevel.getMaxBuildHeight() - 1) {
                  if (this.canSupportAtFace(pLevel, pPos, direction)) {
                     pLevel.setBlock(pPos, pState.setValue(UP, Boolean.valueOf(true)), 2);
                     return;
                  }

                  if (pLevel.isEmptyBlock(blockpos)) {
                     if (!this.canSpread(pLevel, pPos)) {
                        return;
                     }

                     BlockState blockstate3 = pState;

                     for(Direction direction2 : Direction.Plane.HORIZONTAL) {
                        if (pRandom.nextBoolean() || !isAcceptableNeighbour(pLevel, blockpos.relative(direction2), direction2)) {
                           blockstate3 = blockstate3.setValue(getPropertyForFace(direction2), Boolean.valueOf(false));
                        }
                     }

                     if (this.hasHorizontalConnection(blockstate3)) {
                        pLevel.setBlock(blockpos, blockstate3, 2);
                     }

                     return;
                  }
               }

               if (pPos.getY() > pLevel.getMinBuildHeight()) {
                  BlockPos blockpos1 = pPos.below();
                  BlockState blockstate = pLevel.getBlockState(blockpos1);
                  if (blockstate.isAir() || blockstate.is(this)) {
                     BlockState blockstate1 = blockstate.isAir() ? this.defaultBlockState() : blockstate;
                     BlockState blockstate2 = this.copyRandomFaces(pState, blockstate1, pRandom);
                     if (blockstate1 != blockstate2 && this.hasHorizontalConnection(blockstate2)) {
                        pLevel.setBlock(blockpos1, blockstate2, 2);
                     }
                  }
               }

            }
         }
      }
   }

   private BlockState copyRandomFaces(BlockState pSourceState, BlockState pSpreadState, RandomSource pRandom) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (pRandom.nextBoolean()) {
            BooleanProperty booleanproperty = getPropertyForFace(direction);
            if (pSourceState.getValue(booleanproperty)) {
               pSpreadState = pSpreadState.setValue(booleanproperty, Boolean.valueOf(true));
            }
         }
      }

      return pSpreadState;
   }

   private boolean hasHorizontalConnection(BlockState pState) {
      return pState.getValue(NORTH) || pState.getValue(EAST) || pState.getValue(SOUTH) || pState.getValue(WEST);
   }

   private boolean canSpread(BlockGetter pBlockReader, BlockPos pPos) {
      int i = 4;
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(pPos.getX() - 4, pPos.getY() - 1, pPos.getZ() - 4, pPos.getX() + 4, pPos.getY() + 1, pPos.getZ() + 4);
      int j = 5;

      for(BlockPos blockpos : iterable) {
         if (pBlockReader.getBlockState(blockpos).is(this)) {
            --j;
            if (j <= 0) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      BlockState blockstate = pUseContext.getLevel().getBlockState(pUseContext.getClickedPos());
      if (blockstate.is(this)) {
         return this.countFaces(blockstate) < PROPERTY_BY_DIRECTION.size();
      } else {
         return super.canBeReplaced(pState, pUseContext);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
      boolean flag = blockstate.is(this);
      BlockState blockstate1 = flag ? blockstate : this.defaultBlockState();

      for(Direction direction : pContext.getNearestLookingDirections()) {
         if (direction != Direction.DOWN) {
            BooleanProperty booleanproperty = getPropertyForFace(direction);
            boolean flag1 = flag && blockstate.getValue(booleanproperty);
            if (!flag1 && this.canSupportAtFace(pContext.getLevel(), pContext.getClickedPos(), direction)) {
               return blockstate1.setValue(booleanproperty, Boolean.valueOf(true));
            }
         }
      }

      return flag ? blockstate1 : null;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(UP, NORTH, EAST, SOUTH, WEST);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotate) {
      switch (pRotate) {
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

   public static BooleanProperty getPropertyForFace(Direction pFace) {
      return PROPERTY_BY_DIRECTION.get(pFace);
   }
}
