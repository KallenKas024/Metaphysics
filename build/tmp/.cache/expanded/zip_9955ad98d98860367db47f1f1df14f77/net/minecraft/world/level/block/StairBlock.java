package net.minecraft.world.level.block;

import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StairBlock extends Block implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
   public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape TOP_AABB = SlabBlock.TOP_AABB;
   protected static final VoxelShape BOTTOM_AABB = SlabBlock.BOTTOM_AABB;
   protected static final VoxelShape OCTET_NNN = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
   protected static final VoxelShape OCTET_NNP = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
   protected static final VoxelShape OCTET_NPN = Block.box(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
   protected static final VoxelShape OCTET_NPP = Block.box(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
   protected static final VoxelShape OCTET_PNN = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
   protected static final VoxelShape OCTET_PNP = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
   protected static final VoxelShape OCTET_PPN = Block.box(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
   protected static final VoxelShape OCTET_PPP = Block.box(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape[] TOP_SHAPES = makeShapes(TOP_AABB, OCTET_NNN, OCTET_PNN, OCTET_NNP, OCTET_PNP);
   protected static final VoxelShape[] BOTTOM_SHAPES = makeShapes(BOTTOM_AABB, OCTET_NPN, OCTET_PPN, OCTET_NPP, OCTET_PPP);
   private static final int[] SHAPE_BY_STATE = new int[]{12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8};
   private final Block base;
   private final BlockState baseState;

   private static VoxelShape[] makeShapes(VoxelShape pSlabShape, VoxelShape pNwCorner, VoxelShape pNeCorner, VoxelShape pSwCorner, VoxelShape pSeCorner) {
      return IntStream.range(0, 16).mapToObj((p_56945_) -> {
         return makeStairShape(p_56945_, pSlabShape, pNwCorner, pNeCorner, pSwCorner, pSeCorner);
      }).toArray((p_56949_) -> {
         return new VoxelShape[p_56949_];
      });
   }

   /**
    * Combines the shapes according to the mode set in the bitfield
    */
   private static VoxelShape makeStairShape(int pBitfield, VoxelShape pSlabShape, VoxelShape pNwCorner, VoxelShape pNeCorner, VoxelShape pSwCorner, VoxelShape pSeCorner) {
      VoxelShape voxelshape = pSlabShape;
      if ((pBitfield & 1) != 0) {
         voxelshape = Shapes.or(pSlabShape, pNwCorner);
      }

      if ((pBitfield & 2) != 0) {
         voxelshape = Shapes.or(voxelshape, pNeCorner);
      }

      if ((pBitfield & 4) != 0) {
         voxelshape = Shapes.or(voxelshape, pSwCorner);
      }

      if ((pBitfield & 8) != 0) {
         voxelshape = Shapes.or(voxelshape, pSeCorner);
      }

      return voxelshape;
   }

   @Deprecated // Forge: Use the other constructor that takes a Supplier
   public StairBlock(BlockState pBaseState, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, Boolean.valueOf(false)));
      this.base = pBaseState.getBlock();
      this.baseState = pBaseState;
      this.stateSupplier = () -> pBaseState;
   }

   public StairBlock(java.util.function.Supplier<BlockState> state, BlockBehaviour.Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, Boolean.valueOf(false)));
      this.base = Blocks.AIR; // These are unused, fields are redirected
      this.baseState = Blocks.AIR.defaultBlockState();
      this.stateSupplier = state;
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return (pState.getValue(HALF) == Half.TOP ? TOP_SHAPES : BOTTOM_SHAPES)[SHAPE_BY_STATE[this.getShapeIndex(pState)]];
   }

   private int getShapeIndex(BlockState pState) {
      return pState.getValue(SHAPE).ordinal() * 4 + pState.getValue(FACING).get2DDataValue();
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      this.base.animateTick(pState, pLevel, pPos, pRandom);
   }

   public void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
      this.baseState.attack(pLevel, pPos, pPlayer);
   }

   /**
    * Called after this block has been removed by a player.
    */
   public void destroy(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      this.base.destroy(pLevel, pPos, pState);
   }

   /**
    * @return how much this block resists an explosion
    */
   public float getExplosionResistance() {
      return this.base.getExplosionResistance();
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pState.is(pState.getBlock())) {
         pLevel.neighborChanged(this.baseState, pPos, Blocks.AIR, pPos, false);
         this.base.onPlace(this.baseState, pLevel, pPos, pOldState, false);
      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         this.baseState.onRemove(pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
      this.base.stepOn(pLevel, pPos, pState, pEntity);
   }

   /**
    * @return whether this block needs random ticking.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return this.base.isRandomlyTicking(pState);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      this.base.randomTick(pState, pLevel, pPos, pRandom);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      this.base.tick(pState, pLevel, pPos, pRandom);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      return this.baseState.use(pLevel, pPlayer, pHand, pHit);
   }

   /**
    * Called when this Block is destroyed by an Explosion
    */
   public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
      this.base.wasExploded(pLevel, pPos, pExplosion);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Direction direction = pContext.getClickedFace();
      BlockPos blockpos = pContext.getClickedPos();
      FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
      BlockState blockstate = this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection()).setValue(HALF, direction != Direction.DOWN && (direction == Direction.UP || !(pContext.getClickLocation().y - (double)blockpos.getY() > 0.5D)) ? Half.BOTTOM : Half.TOP).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
      return blockstate.setValue(SHAPE, getStairsShape(blockstate, pContext.getLevel(), blockpos));
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return pFacing.getAxis().isHorizontal() ? pState.setValue(SHAPE, getStairsShape(pState, pLevel, pCurrentPos)) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   /**
    * Returns a stair shape property based on the surrounding stairs from the given blockstate and position
    */
   private static StairsShape getStairsShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      Direction direction = pState.getValue(FACING);
      BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
      if (isStairs(blockstate) && pState.getValue(HALF) == blockstate.getValue(HALF)) {
         Direction direction1 = blockstate.getValue(FACING);
         if (direction1.getAxis() != pState.getValue(FACING).getAxis() && canTakeShape(pState, pLevel, pPos, direction1.getOpposite())) {
            if (direction1 == direction.getCounterClockWise()) {
               return StairsShape.OUTER_LEFT;
            }

            return StairsShape.OUTER_RIGHT;
         }
      }

      BlockState blockstate1 = pLevel.getBlockState(pPos.relative(direction.getOpposite()));
      if (isStairs(blockstate1) && pState.getValue(HALF) == blockstate1.getValue(HALF)) {
         Direction direction2 = blockstate1.getValue(FACING);
         if (direction2.getAxis() != pState.getValue(FACING).getAxis() && canTakeShape(pState, pLevel, pPos, direction2)) {
            if (direction2 == direction.getCounterClockWise()) {
               return StairsShape.INNER_LEFT;
            }

            return StairsShape.INNER_RIGHT;
         }
      }

      return StairsShape.STRAIGHT;
   }

   private static boolean canTakeShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pFace) {
      BlockState blockstate = pLevel.getBlockState(pPos.relative(pFace));
      return !isStairs(blockstate) || blockstate.getValue(FACING) != pState.getValue(FACING) || blockstate.getValue(HALF) != pState.getValue(HALF);
   }

   public static boolean isStairs(BlockState pState) {
      return pState.getBlock() instanceof StairBlock;
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
      Direction direction = pState.getValue(FACING);
      StairsShape stairsshape = pState.getValue(SHAPE);
      switch (pMirror) {
         case LEFT_RIGHT:
            if (direction.getAxis() == Direction.Axis.Z) {
               switch (stairsshape) {
                  case INNER_LEFT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case INNER_RIGHT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case OUTER_LEFT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case OUTER_RIGHT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  default:
                     return pState.rotate(Rotation.CLOCKWISE_180);
               }
            }
            break;
         case FRONT_BACK:
            if (direction.getAxis() == Direction.Axis.X) {
               switch (stairsshape) {
                  case INNER_LEFT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
                  case INNER_RIGHT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
                  case OUTER_LEFT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
                  case OUTER_RIGHT:
                     return pState.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
                  case STRAIGHT:
                     return pState.rotate(Rotation.CLOCKWISE_180);
               }
            }
      }

      return super.mirror(pState, pMirror);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, HALF, SHAPE, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   // Forge Start
   private final java.util.function.Supplier<BlockState> stateSupplier;
   private Block getModelBlock() {
       return getModelState().getBlock();
   }
   private BlockState getModelState() {
       return stateSupplier.get();
   }
   // Forge end
}
