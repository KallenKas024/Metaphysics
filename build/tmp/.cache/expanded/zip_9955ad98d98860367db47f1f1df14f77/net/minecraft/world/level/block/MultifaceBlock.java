package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MultifaceBlock extends Block {
   private static final float AABB_OFFSET = 1.0F;
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
   private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (p_153923_) -> {
      p_153923_.put(Direction.NORTH, NORTH_AABB);
      p_153923_.put(Direction.EAST, EAST_AABB);
      p_153923_.put(Direction.SOUTH, SOUTH_AABB);
      p_153923_.put(Direction.WEST, WEST_AABB);
      p_153923_.put(Direction.UP, UP_AABB);
      p_153923_.put(Direction.DOWN, DOWN_AABB);
   });
   protected static final Direction[] DIRECTIONS = Direction.values();
   private final ImmutableMap<BlockState, VoxelShape> shapesCache;
   private final boolean canRotate;
   private final boolean canMirrorX;
   private final boolean canMirrorZ;

   public MultifaceBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
      this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
      this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
      this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
      this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
   }

   public static Set<Direction> availableFaces(BlockState pState) {
      if (!(pState.getBlock() instanceof MultifaceBlock)) {
         return Set.of();
      } else {
         Set<Direction> set = EnumSet.noneOf(Direction.class);

         for(Direction direction : Direction.values()) {
            if (hasFace(pState, direction)) {
               set.add(direction);
            }
         }

         return set;
      }
   }

   public static Set<Direction> unpack(byte pPackedDirections) {
      Set<Direction> set = EnumSet.noneOf(Direction.class);

      for(Direction direction : Direction.values()) {
         if ((pPackedDirections & (byte)(1 << direction.ordinal())) > 0) {
            set.add(direction);
         }
      }

      return set;
   }

   public static byte pack(Collection<Direction> pDirections) {
      byte b0 = 0;

      for(Direction direction : pDirections) {
         b0 = (byte)(b0 | 1 << direction.ordinal());
      }

      return b0;
   }

   protected boolean isFaceSupported(Direction p_153921_) {
      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      for(Direction direction : DIRECTIONS) {
         if (this.isFaceSupported(direction)) {
            pBuilder.add(getFaceProperty(direction));
         }
      }

   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (!hasAnyFace(pState)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return hasFace(pState, pDirection) && !canAttachTo(pLevel, pDirection, pNeighborPos, pNeighborState) ? removeFace(pState, getFaceProperty(pDirection)) : pState;
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapesCache.get(pState);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      boolean flag = false;

      for(Direction direction : DIRECTIONS) {
         if (hasFace(pState, direction)) {
            BlockPos blockpos = pPos.relative(direction);
            if (!canAttachTo(pLevel, direction, blockpos, pLevel.getBlockState(blockpos))) {
               return false;
            }

            flag = true;
         }
      }

      return flag;
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      return hasAnyVacantFace(pState);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      return Arrays.stream(pContext.getNearestLookingDirections()).map((p_153865_) -> {
         return this.getStateForPlacement(blockstate, level, blockpos, p_153865_);
      }).filter(Objects::nonNull).findFirst().orElse((BlockState)null);
   }

   public boolean isValidStateForPlacement(BlockGetter pLevel, BlockState pState, BlockPos pPos, Direction pDirection) {
      if (this.isFaceSupported(pDirection) && (!pState.is(this) || !hasFace(pState, pDirection))) {
         BlockPos blockpos = pPos.relative(pDirection);
         return canAttachTo(pLevel, pDirection, blockpos, pLevel.getBlockState(blockpos));
      } else {
         return false;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockState pCurrentState, BlockGetter pLevel, BlockPos pPos, Direction pLookingDirection) {
      if (!this.isValidStateForPlacement(pLevel, pCurrentState, pPos, pLookingDirection)) {
         return null;
      } else {
         BlockState blockstate;
         if (pCurrentState.is(this)) {
            blockstate = pCurrentState;
         } else if (this.isWaterloggable() && pCurrentState.getFluidState().isSourceOfType(Fluids.WATER)) {
            blockstate = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
         } else {
            blockstate = this.defaultBlockState();
         }

         return blockstate.setValue(getFaceProperty(pLookingDirection), Boolean.valueOf(true));
      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return !this.canRotate ? pState : this.mapDirections(pState, pRotation::rotate);
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      if (pMirror == Mirror.FRONT_BACK && !this.canMirrorX) {
         return pState;
      } else {
         return pMirror == Mirror.LEFT_RIGHT && !this.canMirrorZ ? pState : this.mapDirections(pState, pMirror::mirror);
      }
   }

   private BlockState mapDirections(BlockState pState, Function<Direction, Direction> pDirectionalFunction) {
      BlockState blockstate = pState;

      for(Direction direction : DIRECTIONS) {
         if (this.isFaceSupported(direction)) {
            blockstate = blockstate.setValue(getFaceProperty(pDirectionalFunction.apply(direction)), pState.getValue(getFaceProperty(direction)));
         }
      }

      return blockstate;
   }

   public static boolean hasFace(BlockState pState, Direction pDirection) {
      BooleanProperty booleanproperty = getFaceProperty(pDirection);
      return pState.hasProperty(booleanproperty) && pState.getValue(booleanproperty);
   }

   public static boolean canAttachTo(BlockGetter pLevel, Direction pDirection, BlockPos pPos, BlockState pState) {
      return Block.isFaceFull(pState.getBlockSupportShape(pLevel, pPos), pDirection.getOpposite()) || Block.isFaceFull(pState.getCollisionShape(pLevel, pPos), pDirection.getOpposite());
   }

   private boolean isWaterloggable() {
      return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
   }

   private static BlockState removeFace(BlockState pState, BooleanProperty pFaceProp) {
      BlockState blockstate = pState.setValue(pFaceProp, Boolean.valueOf(false));
      return hasAnyFace(blockstate) ? blockstate : Blocks.AIR.defaultBlockState();
   }

   public static BooleanProperty getFaceProperty(Direction pDirection) {
      return PROPERTY_BY_DIRECTION.get(pDirection);
   }

   private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> pStateDefinition) {
      BlockState blockstate = pStateDefinition.any();

      for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
         if (blockstate.hasProperty(booleanproperty)) {
            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
         }
      }

      return blockstate;
   }

   private static VoxelShape calculateMultifaceShape(BlockState p_153959_) {
      VoxelShape voxelshape = Shapes.empty();

      for(Direction direction : DIRECTIONS) {
         if (hasFace(p_153959_, direction)) {
            voxelshape = Shapes.or(voxelshape, SHAPE_BY_DIRECTION.get(direction));
         }
      }

      return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
   }

   protected static boolean hasAnyFace(BlockState pState) {
      return Arrays.stream(DIRECTIONS).anyMatch((p_221583_) -> {
         return hasFace(pState, p_221583_);
      });
   }

   private static boolean hasAnyVacantFace(BlockState pState) {
      return Arrays.stream(DIRECTIONS).anyMatch((p_221580_) -> {
         return !hasFace(pState, p_221580_);
      });
   }

   public abstract MultifaceSpreader getSpreader();
}