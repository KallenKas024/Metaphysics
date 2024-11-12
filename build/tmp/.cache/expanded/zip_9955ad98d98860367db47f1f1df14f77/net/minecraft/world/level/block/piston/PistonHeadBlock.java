package net.minecraft.world.level.block.piston;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonHeadBlock extends DirectionalBlock {
   public static final EnumProperty<PistonType> TYPE = BlockStateProperties.PISTON_TYPE;
   public static final BooleanProperty SHORT = BlockStateProperties.SHORT;
   public static final float PLATFORM = 4.0F;
   protected static final VoxelShape EAST_AABB = Block.box(12.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 4.0D, 16.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 12.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 4.0D);
   protected static final VoxelShape UP_AABB = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
   protected static final float AABB_OFFSET = 2.0F;
   protected static final float EDGE_MIN = 6.0F;
   protected static final float EDGE_MAX = 10.0F;
   protected static final VoxelShape UP_ARM_AABB = Block.box(6.0D, -4.0D, 6.0D, 10.0D, 12.0D, 10.0D);
   protected static final VoxelShape DOWN_ARM_AABB = Block.box(6.0D, 4.0D, 6.0D, 10.0D, 20.0D, 10.0D);
   protected static final VoxelShape SOUTH_ARM_AABB = Block.box(6.0D, 6.0D, -4.0D, 10.0D, 10.0D, 12.0D);
   protected static final VoxelShape NORTH_ARM_AABB = Block.box(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 20.0D);
   protected static final VoxelShape EAST_ARM_AABB = Block.box(-4.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
   protected static final VoxelShape WEST_ARM_AABB = Block.box(4.0D, 6.0D, 6.0D, 20.0D, 10.0D, 10.0D);
   protected static final VoxelShape SHORT_UP_ARM_AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D);
   protected static final VoxelShape SHORT_DOWN_ARM_AABB = Block.box(6.0D, 4.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   protected static final VoxelShape SHORT_SOUTH_ARM_AABB = Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 12.0D);
   protected static final VoxelShape SHORT_NORTH_ARM_AABB = Block.box(6.0D, 6.0D, 4.0D, 10.0D, 10.0D, 16.0D);
   protected static final VoxelShape SHORT_EAST_ARM_AABB = Block.box(0.0D, 6.0D, 6.0D, 12.0D, 10.0D, 10.0D);
   protected static final VoxelShape SHORT_WEST_ARM_AABB = Block.box(4.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
   private static final VoxelShape[] SHAPES_SHORT = makeShapes(true);
   private static final VoxelShape[] SHAPES_LONG = makeShapes(false);

   private static VoxelShape[] makeShapes(boolean pExtended) {
      return Arrays.stream(Direction.values()).map((p_60316_) -> {
         return calculateShape(p_60316_, pExtended);
      }).toArray((p_60318_) -> {
         return new VoxelShape[p_60318_];
      });
   }

   private static VoxelShape calculateShape(Direction pDirection, boolean pShortArm) {
      switch (pDirection) {
         case DOWN:
         default:
            return Shapes.or(DOWN_AABB, pShortArm ? SHORT_DOWN_ARM_AABB : DOWN_ARM_AABB);
         case UP:
            return Shapes.or(UP_AABB, pShortArm ? SHORT_UP_ARM_AABB : UP_ARM_AABB);
         case NORTH:
            return Shapes.or(NORTH_AABB, pShortArm ? SHORT_NORTH_ARM_AABB : NORTH_ARM_AABB);
         case SOUTH:
            return Shapes.or(SOUTH_AABB, pShortArm ? SHORT_SOUTH_ARM_AABB : SOUTH_ARM_AABB);
         case WEST:
            return Shapes.or(WEST_AABB, pShortArm ? SHORT_WEST_ARM_AABB : WEST_ARM_AABB);
         case EAST:
            return Shapes.or(EAST_AABB, pShortArm ? SHORT_EAST_ARM_AABB : EAST_ARM_AABB);
      }
   }

   public PistonHeadBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, PistonType.DEFAULT).setValue(SHORT, Boolean.valueOf(false)));
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return (pState.getValue(SHORT) ? SHAPES_SHORT : SHAPES_LONG)[pState.getValue(FACING).ordinal()];
   }

   private boolean isFittingBase(BlockState pBaseState, BlockState pExtendedState) {
      Block block = pBaseState.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
      return pExtendedState.is(block) && pExtendedState.getValue(PistonBaseBlock.EXTENDED) && pExtendedState.getValue(FACING) == pBaseState.getValue(FACING);
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
      if (!pLevel.isClientSide && pPlayer.getAbilities().instabuild) {
         BlockPos blockpos = pPos.relative(pState.getValue(FACING).getOpposite());
         if (this.isFittingBase(pState, pLevel.getBlockState(blockpos))) {
            pLevel.destroyBlock(blockpos, false);
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
         BlockPos blockpos = pPos.relative(pState.getValue(FACING).getOpposite());
         if (this.isFittingBase(pState, pLevel.getBlockState(blockpos))) {
            pLevel.destroyBlock(blockpos, true);
         }

      }
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite()));
      return this.isFittingBase(pState, blockstate) || blockstate.is(Blocks.MOVING_PISTON) && blockstate.getValue(FACING) == pState.getValue(FACING);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (pState.canSurvive(pLevel, pPos)) {
         pLevel.neighborChanged(pPos.relative(pState.getValue(FACING).getOpposite()), pBlock, pFromPos);
      }

   }

   public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
      return new ItemStack(pState.getValue(TYPE) == PistonType.STICKY ? Blocks.STICKY_PISTON : Blocks.PISTON);
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

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, TYPE, SHORT);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }
}