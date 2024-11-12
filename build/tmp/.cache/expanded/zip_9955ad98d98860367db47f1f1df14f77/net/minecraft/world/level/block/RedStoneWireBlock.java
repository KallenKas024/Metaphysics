package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RedStoneWireBlock extends Block {
   public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
   public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
   protected static final int H = 1;
   protected static final int W = 3;
   protected static final int E = 13;
   protected static final int N = 3;
   protected static final int S = 13;
   private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
   private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
   private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
   private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
   private static final Vec3[] COLORS = Util.make(new Vec3[16], (p_154319_) -> {
      for(int i = 0; i <= 15; ++i) {
         float f = (float)i / 15.0F;
         float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
         float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
         float f3 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
         p_154319_[i] = new Vec3((double)f1, (double)f2, (double)f3);
      }

   });
   private static final float PARTICLE_DENSITY = 0.2F;
   private final BlockState crossState;
   private boolean shouldSignal = true;

   public RedStoneWireBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
      this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

      for(BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
         if (blockstate.getValue(POWER) == 0) {
            SHAPES_CACHE.put(blockstate, this.calculateShape(blockstate));
         }
      }

   }

   private VoxelShape calculateShape(BlockState pState) {
      VoxelShape voxelshape = SHAPE_DOT;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside == RedstoneSide.SIDE) {
            voxelshape = Shapes.or(voxelshape, SHAPES_FLOOR.get(direction));
         } else if (redstoneside == RedstoneSide.UP) {
            voxelshape = Shapes.or(voxelshape, SHAPES_UP.get(direction));
         }
      }

      return voxelshape;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPES_CACHE.get(pState.setValue(POWER, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.getConnectionState(pContext.getLevel(), this.crossState, pContext.getClickedPos());
   }

   private BlockState getConnectionState(BlockGetter pLevel, BlockState pState, BlockPos pPos) {
      boolean flag = isDot(pState);
      pState = this.getMissingConnections(pLevel, this.defaultBlockState().setValue(POWER, pState.getValue(POWER)), pPos);
      if (flag && isDot(pState)) {
         return pState;
      } else {
         boolean flag1 = pState.getValue(NORTH).isConnected();
         boolean flag2 = pState.getValue(SOUTH).isConnected();
         boolean flag3 = pState.getValue(EAST).isConnected();
         boolean flag4 = pState.getValue(WEST).isConnected();
         boolean flag5 = !flag1 && !flag2;
         boolean flag6 = !flag3 && !flag4;
         if (!flag4 && flag5) {
            pState = pState.setValue(WEST, RedstoneSide.SIDE);
         }

         if (!flag3 && flag5) {
            pState = pState.setValue(EAST, RedstoneSide.SIDE);
         }

         if (!flag1 && flag6) {
            pState = pState.setValue(NORTH, RedstoneSide.SIDE);
         }

         if (!flag2 && flag6) {
            pState = pState.setValue(SOUTH, RedstoneSide.SIDE);
         }

         return pState;
      }
   }

   private BlockState getMissingConnections(BlockGetter pLevel, BlockState pState, BlockPos pPos) {
      boolean flag = !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!pState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
            RedstoneSide redstoneside = this.getConnectingSide(pLevel, pPos, direction, flag);
            pState = pState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
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
         return pState;
      } else if (pFacing == Direction.UP) {
         return this.getConnectionState(pLevel, pState, pCurrentPos);
      } else {
         RedstoneSide redstoneside = this.getConnectingSide(pLevel, pCurrentPos, pFacing);
         return redstoneside.isConnected() == pState.getValue(PROPERTY_BY_DIRECTION.get(pFacing)).isConnected() && !isCross(pState) ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside) : this.getConnectionState(pLevel, this.crossState.setValue(POWER, pState.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside), pCurrentPos);
      }
   }

   private static boolean isCross(BlockState pState) {
      return pState.getValue(NORTH).isConnected() && pState.getValue(SOUTH).isConnected() && pState.getValue(EAST).isConnected() && pState.getValue(WEST).isConnected();
   }

   private static boolean isDot(BlockState pState) {
      return !pState.getValue(NORTH).isConnected() && !pState.getValue(SOUTH).isConnected() && !pState.getValue(EAST).isConnected() && !pState.getValue(WEST).isConnected();
   }

   /**
    * Updates the shapes of indirect neighbors of this block. This method is analogous to
    * {@link net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase#updateNeighbourShapes}
    * but where that method affects the 6 direct neighbors of this block, this method affects
    * the indirect neighbors, if any.
    * 
    * <p>
    * Currently the only implementation of this method is {@link
    * net.minecraft.world.level.block.RedStoneWireBlock#updateIndirectNeighbourShapes}
    * where it is used to validate diagonal connections of redstone wire blocks.
    */
   public void updateIndirectNeighbourShapes(BlockState pState, LevelAccessor pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside != RedstoneSide.NONE && !pLevel.getBlockState(blockpos$mutableblockpos.setWithOffset(pPos, direction)).is(this)) {
            blockpos$mutableblockpos.move(Direction.DOWN);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutableblockpos);
            if (blockstate.is(this)) {
               BlockPos blockpos = blockpos$mutableblockpos.relative(direction.getOpposite());
               pLevel.neighborShapeChanged(direction.getOpposite(), pLevel.getBlockState(blockpos), blockpos$mutableblockpos, blockpos, pFlags, pRecursionLeft);
            }

            blockpos$mutableblockpos.setWithOffset(pPos, direction).move(Direction.UP);
            BlockState blockstate1 = pLevel.getBlockState(blockpos$mutableblockpos);
            if (blockstate1.is(this)) {
               BlockPos blockpos1 = blockpos$mutableblockpos.relative(direction.getOpposite());
               pLevel.neighborShapeChanged(direction.getOpposite(), pLevel.getBlockState(blockpos1), blockpos$mutableblockpos, blockpos1, pFlags, pRecursionLeft);
            }
         }
      }

   }

   private RedstoneSide getConnectingSide(BlockGetter pLevel, BlockPos pPos, Direction pFace) {
      return this.getConnectingSide(pLevel, pPos, pFace, !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos));
   }

   private RedstoneSide getConnectingSide(BlockGetter pLevel, BlockPos pPos, Direction pDirection, boolean pNonNormalCubeAbove) {
      BlockPos blockpos = pPos.relative(pDirection);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (pNonNormalCubeAbove) {
         boolean flag = blockstate.getBlock() instanceof TrapDoorBlock || this.canSurviveOn(pLevel, blockpos, blockstate);
         if (flag && pLevel.getBlockState(blockpos.above()).canRedstoneConnectTo(pLevel, blockpos.above(), null)) {
            if (blockstate.isFaceSturdy(pLevel, blockpos, pDirection.getOpposite())) {
               return RedstoneSide.UP;
            }

            return RedstoneSide.SIDE;
         }
      }

      if (blockstate.canRedstoneConnectTo(pLevel, blockpos, pDirection)) {
          return RedstoneSide.SIDE;
      } else if (blockstate.isRedstoneConductor(pLevel, blockpos)) {
          return RedstoneSide.NONE;
      } else {
          BlockPos blockPosBelow = blockpos.below();
          return pLevel.getBlockState(blockPosBelow).canRedstoneConnectTo(pLevel, blockPosBelow, null) ? RedstoneSide.SIDE : RedstoneSide.NONE;
      }
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return this.canSurviveOn(pLevel, blockpos, blockstate);
   }

   private boolean canSurviveOn(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return pState.isFaceSturdy(pLevel, pPos, Direction.UP) || pState.is(Blocks.HOPPER);
   }

   private void updatePowerStrength(Level pLevel, BlockPos pPos, BlockState pState) {
      int i = this.calculateTargetStrength(pLevel, pPos);
      if (pState.getValue(POWER) != i) {
         if (pLevel.getBlockState(pPos) == pState) {
            pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(i)), 2);
         }

         Set<BlockPos> set = Sets.newHashSet();
         set.add(pPos);

         for(Direction direction : Direction.values()) {
            set.add(pPos.relative(direction));
         }

         for(BlockPos blockpos : set) {
            pLevel.updateNeighborsAt(blockpos, this);
         }
      }

   }

   private int calculateTargetStrength(Level pLevel, BlockPos pPos) {
      this.shouldSignal = false;
      int i = pLevel.getBestNeighborSignal(pPos);
      this.shouldSignal = true;
      int j = 0;
      if (i < 15) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            j = Math.max(j, this.getWireSignal(blockstate));
            BlockPos blockpos1 = pPos.above();
            if (blockstate.isRedstoneConductor(pLevel, blockpos) && !pLevel.getBlockState(blockpos1).isRedstoneConductor(pLevel, blockpos1)) {
               j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.above())));
            } else if (!blockstate.isRedstoneConductor(pLevel, blockpos)) {
               j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.below())));
            }
         }
      }

      return Math.max(i, j - 1);
   }

   private int getWireSignal(BlockState pState) {
      return pState.is(this) ? pState.getValue(POWER) : 0;
   }

   /**
    * Calls {@link net.minecraft.world.level.Level#updateNeighborsAt} for all neighboring blocks, but only if the given
    * block is a redstone wire.
    */
   private void checkCornerChangeAt(Level pLevel, BlockPos pPos) {
      if (pLevel.getBlockState(pPos).is(this)) {
         pLevel.updateNeighborsAt(pPos, this);

         for(Direction direction : Direction.values()) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

      }
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock()) && !pLevel.isClientSide) {
         this.updatePowerStrength(pLevel, pPos, pState);

         for(Direction direction : Direction.Plane.VERTICAL) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

         this.updateNeighborsOfNeighboringWires(pLevel, pPos);
      }
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
         if (!pLevel.isClientSide) {
            for(Direction direction : Direction.values()) {
               pLevel.updateNeighborsAt(pPos.relative(direction), this);
            }

            this.updatePowerStrength(pLevel, pPos, pState);
            this.updateNeighborsOfNeighboringWires(pLevel, pPos);
         }
      }
   }

   private void updateNeighborsOfNeighboringWires(Level pLevel, BlockPos pPos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         this.checkCornerChangeAt(pLevel, pPos.relative(direction));
      }

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction1);
         if (pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            this.checkCornerChangeAt(pLevel, blockpos.above());
         } else {
            this.checkCornerChangeAt(pLevel, blockpos.below());
         }
      }

   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         if (pState.canSurvive(pLevel, pPos)) {
            this.updatePowerStrength(pLevel, pPos, pState);
         } else {
            dropResources(pState, pLevel, pPos);
            pLevel.removeBlock(pPos, false);
         }

      }
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
      return !this.shouldSignal ? 0 : pBlockState.getSignal(pBlockAccess, pPos, pSide);
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
      if (this.shouldSignal && pSide != Direction.DOWN) {
         int i = pBlockState.getValue(POWER);
         if (i == 0) {
            return 0;
         } else {
            return pSide != Direction.UP && !this.getConnectionState(pBlockAccess, pBlockState, pPos).getValue(PROPERTY_BY_DIRECTION.get(pSide.getOpposite())).isConnected() ? 0 : i;
         }
      } else {
         return 0;
      }
   }

   protected static boolean shouldConnectTo(BlockState pState) {
      return shouldConnectTo(pState, (Direction)null);
   }

   protected static boolean shouldConnectTo(BlockState pState, @Nullable Direction pDirection) {
      if (pState.is(Blocks.REDSTONE_WIRE)) {
         return true;
      } else if (pState.is(Blocks.REPEATER)) {
         Direction direction = pState.getValue(RepeaterBlock.FACING);
         return direction == pDirection || direction.getOpposite() == pDirection;
      } else if (pState.is(Blocks.OBSERVER)) {
         return pDirection == pState.getValue(ObserverBlock.FACING);
      } else {
         return pState.isSignalSource() && pDirection != null;
      }
   }

   /**
    * Returns whether this block is capable of emitting redstone signals.
    * 
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#isSignalSource}
    * whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return this.shouldSignal;
   }

   public static int getColorForPower(int pPower) {
      Vec3 vec3 = COLORS[pPower];
      return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
   }

   private void spawnParticlesAlongLine(Level pLevel, RandomSource pRandom, BlockPos pPos, Vec3 pParticleVec, Direction pXDirection, Direction pZDirection, float pMin, float pMax) {
      float f = pMax - pMin;
      if (!(pRandom.nextFloat() >= 0.2F * f)) {
         float f1 = 0.4375F;
         float f2 = pMin + f * pRandom.nextFloat();
         double d0 = 0.5D + (double)(0.4375F * (float)pXDirection.getStepX()) + (double)(f2 * (float)pZDirection.getStepX());
         double d1 = 0.5D + (double)(0.4375F * (float)pXDirection.getStepY()) + (double)(f2 * (float)pZDirection.getStepY());
         double d2 = 0.5D + (double)(0.4375F * (float)pXDirection.getStepZ()) + (double)(f2 * (float)pZDirection.getStepZ());
         pLevel.addParticle(new DustParticleOptions(pParticleVec.toVector3f(), 1.0F), (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, 0.0D, 0.0D, 0.0D);
      }
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      int i = pState.getValue(POWER);
      if (i != 0) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            switch (redstoneside) {
               case UP:
                  this.spawnParticlesAlongLine(pLevel, pRandom, pPos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
               case SIDE:
                  this.spawnParticlesAlongLine(pLevel, pRandom, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
                  break;
               case NONE:
               default:
                  this.spawnParticlesAlongLine(pLevel, pRandom, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
            }
         }

      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      switch (pRotation) {
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
      pBuilder.add(NORTH, EAST, SOUTH, WEST, POWER);
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (!pPlayer.getAbilities().mayBuild) {
         return InteractionResult.PASS;
      } else {
         if (isCross(pState) || isDot(pState)) {
            BlockState blockstate = isCross(pState) ? this.defaultBlockState() : this.crossState;
            blockstate = blockstate.setValue(POWER, pState.getValue(POWER));
            blockstate = this.getConnectionState(pLevel, blockstate, pPos);
            if (blockstate != pState) {
               pLevel.setBlock(pPos, blockstate, 3);
               this.updatesOnShapeChange(pLevel, pPos, pState, blockstate);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      }
   }

   private void updatesOnShapeChange(Level pLevel, BlockPos pPos, BlockState pOldState, BlockState pNewState) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         if (pOldState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != pNewState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            pLevel.updateNeighborsAtExceptFromFacing(blockpos, pNewState.getBlock(), direction.getOpposite());
         }
      }

   }
}
