package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CeilingHangingSignBlock extends SignBlock {
   public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
   public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
   protected static final float AABB_OFFSET = 5.0F;
   protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
   private static final Map<Integer, VoxelShape> AABBS = Maps.newHashMap(ImmutableMap.of(0, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D), 4, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D), 8, Block.box(1.0D, 0.0D, 7.0D, 15.0D, 10.0D, 9.0D), 12, Block.box(7.0D, 0.0D, 1.0D, 9.0D, 10.0D, 15.0D)));

   public CeilingHangingSignBlock(BlockBehaviour.Properties pProperties, WoodType pType) {
      super(pProperties.sound(pType.hangingSignSoundType()), pType);
      this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION, Integer.valueOf(0)).setValue(ATTACHED, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      BlockEntity $$7 = pLevel.getBlockEntity(pPos);
      if ($$7 instanceof SignBlockEntity signblockentity) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (this.shouldTryToChainAnotherHangingSign(pPlayer, pHit, signblockentity, itemstack)) {
            return InteractionResult.PASS;
         }
      }

      return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
   }

   private boolean shouldTryToChainAnotherHangingSign(Player pPlayer, BlockHitResult pHitResult, SignBlockEntity pSign, ItemStack pStack) {
      return !pSign.canExecuteClickCommands(pSign.isFacingFrontText(pPlayer), pPlayer) && pStack.getItem() instanceof HangingSignItem && pHitResult.getDirection().equals(Direction.DOWN);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos.above()).isFaceSturdy(pLevel, pPos.above(), Direction.DOWN, SupportType.CENTER);
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Level level = pContext.getLevel();
      FluidState fluidstate = level.getFluidState(pContext.getClickedPos());
      BlockPos blockpos = pContext.getClickedPos().above();
      BlockState blockstate = level.getBlockState(blockpos);
      boolean flag = blockstate.is(BlockTags.ALL_HANGING_SIGNS);
      Direction direction = Direction.fromYRot((double)pContext.getRotation());
      boolean flag1 = !Block.isFaceFull(blockstate.getCollisionShape(level, blockpos), Direction.DOWN) || pContext.isSecondaryUseActive();
      if (flag && !pContext.isSecondaryUseActive()) {
         if (blockstate.hasProperty(WallHangingSignBlock.FACING)) {
            Direction direction1 = blockstate.getValue(WallHangingSignBlock.FACING);
            if (direction1.getAxis().test(direction)) {
               flag1 = false;
            }
         } else if (blockstate.hasProperty(ROTATION)) {
            Optional<Direction> optional = RotationSegment.convertToDirection(blockstate.getValue(ROTATION));
            if (optional.isPresent() && optional.get().getAxis().test(direction)) {
               flag1 = false;
            }
         }
      }

      int i = !flag1 ? RotationSegment.convertToSegment(direction.getOpposite()) : RotationSegment.convertToSegment(pContext.getRotation() + 180.0F);
      return this.defaultBlockState().setValue(ATTACHED, Boolean.valueOf(flag1)).setValue(ROTATION, Integer.valueOf(i)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      VoxelShape voxelshape = AABBS.get(pState.getValue(ROTATION));
      return voxelshape == null ? SHAPE : voxelshape;
   }

   public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return this.getShape(pState, pLevel, pPos, CollisionContext.empty());
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.UP && !this.canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public float getYRotationDegrees(BlockState pState) {
      return RotationSegment.convertToDegrees(pState.getValue(ROTATION));
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(ROTATION, Integer.valueOf(pRotation.rotate(pState.getValue(ROTATION), 16)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(ROTATION, Integer.valueOf(pMirror.mirror(pState.getValue(ROTATION), 16)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(ROTATION, ATTACHED, WATERLOGGED);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new HangingSignBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createTickerHelper(pBlockEntityType, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
   }
}