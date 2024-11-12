package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WallHangingSignBlock extends SignBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final VoxelShape PLANK_NORTHSOUTH = Block.box(0.0D, 14.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   public static final VoxelShape PLANK_EASTWEST = Block.box(6.0D, 14.0D, 0.0D, 10.0D, 16.0D, 16.0D);
   public static final VoxelShape SHAPE_NORTHSOUTH = Shapes.or(PLANK_NORTHSOUTH, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D));
   public static final VoxelShape SHAPE_EASTWEST = Shapes.or(PLANK_EASTWEST, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D));
   private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, SHAPE_NORTHSOUTH, Direction.SOUTH, SHAPE_NORTHSOUTH, Direction.EAST, SHAPE_EASTWEST, Direction.WEST, SHAPE_EASTWEST));

   public WallHangingSignBlock(BlockBehaviour.Properties pProperties, WoodType pType) {
      super(pProperties.sound(pType.hangingSignSoundType()), pType);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      BlockEntity $$7 = pLevel.getBlockEntity(pPos);
      if ($$7 instanceof SignBlockEntity signblockentity) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (this.shouldTryToChainAnotherHangingSign(pState, pPlayer, pHit, signblockentity, itemstack)) {
            return InteractionResult.PASS;
         }
      }

      return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
   }

   private boolean shouldTryToChainAnotherHangingSign(BlockState pState, Player pPlayer, BlockHitResult pHitResult, SignBlockEntity pSign, ItemStack pStack) {
      return !pSign.canExecuteClickCommands(pSign.isFacingFrontText(pPlayer), pPlayer) && pStack.getItem() instanceof HangingSignItem && !this.isHittingEditableSide(pHitResult, pState);
   }

   private boolean isHittingEditableSide(BlockHitResult pHitResult, BlockState pState) {
      return pHitResult.getDirection().getAxis() == pState.getValue(FACING).getAxis();
   }

   /**
    * @return the description ID of this block, for use with language files.
    */
   public String getDescriptionId() {
      return this.asItem().getDescriptionId();
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return AABBS.get(pState.getValue(FACING));
   }

   public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return this.getShape(pState, pLevel, pPos, CollisionContext.empty());
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      switch ((Direction)pState.getValue(FACING)) {
         case EAST:
         case WEST:
            return PLANK_EASTWEST;
         default:
            return PLANK_NORTHSOUTH;
      }
   }

   public boolean canPlace(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      Direction direction = pState.getValue(FACING).getClockWise();
      Direction direction1 = pState.getValue(FACING).getCounterClockWise();
      return this.canAttachTo(pLevel, pState, pPos.relative(direction), direction1) || this.canAttachTo(pLevel, pState, pPos.relative(direction1), direction);
   }

   public boolean canAttachTo(LevelReader pLevel, BlockState pState, BlockPos pPos, Direction pDirection) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return blockstate.is(BlockTags.WALL_HANGING_SIGNS) ? blockstate.getValue(FACING).getAxis().test(pState.getValue(FACING)) : blockstate.isFaceSturdy(pLevel, pPos, pDirection, SupportType.FULL);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      BlockState blockstate = this.defaultBlockState();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      LevelReader levelreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();

      for(Direction direction : pContext.getNearestLookingDirections()) {
         if (direction.getAxis().isHorizontal() && !direction.getAxis().test(pContext.getClickedFace())) {
            Direction direction1 = direction.getOpposite();
            blockstate = blockstate.setValue(FACING, direction1);
            if (blockstate.canSurvive(levelreader, blockpos) && this.canPlace(blockstate, levelreader, blockpos)) {
               return blockstate.setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
            }
         }
      }

      return null;
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing.getAxis() == pState.getValue(FACING).getClockWise().getAxis() && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public float getYRotationDegrees(BlockState pState) {
      return pState.getValue(FACING).toYRot();
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
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
      pBuilder.add(FACING, WATERLOGGED);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new HangingSignBlockEntity(pPos, pState);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }
}