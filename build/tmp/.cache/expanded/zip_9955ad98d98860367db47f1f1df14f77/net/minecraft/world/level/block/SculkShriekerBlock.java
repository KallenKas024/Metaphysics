package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
   public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty CAN_SUMMON = BlockStateProperties.CAN_SUMMON;
   protected static final VoxelShape COLLIDER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final double TOP_Y = COLLIDER.max(Direction.Axis.Y);

   public SculkShriekerBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHRIEKING, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(CAN_SUMMON, Boolean.valueOf(false)));
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(SHRIEKING);
      pBuilder.add(WATERLOGGED);
      pBuilder.add(CAN_SUMMON);
   }

   public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
      if (pLevel instanceof ServerLevel serverlevel) {
         ServerPlayer serverplayer = SculkShriekerBlockEntity.tryGetPlayer(pEntity);
         if (serverplayer != null) {
            serverlevel.getBlockEntity(pPos, BlockEntityType.SCULK_SHRIEKER).ifPresent((p_222163_) -> {
               p_222163_.tryShriek(serverlevel, serverplayer);
            });
         }
      }

      super.stepOn(pLevel, pPos, pState, pEntity);
   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
      if (pLevel instanceof ServerLevel serverlevel) {
         if (pState.getValue(SHRIEKING) && !pState.is(pNewState.getBlock())) {
            serverlevel.getBlockEntity(pPos, BlockEntityType.SCULK_SHRIEKER).ifPresent((p_222217_) -> {
               p_222217_.tryRespond(serverlevel);
            });
         }
      }

      super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(SHRIEKING)) {
         pLevel.setBlock(pPos, pState.setValue(SHRIEKING, Boolean.valueOf(false)), 3);
         pLevel.getBlockEntity(pPos, BlockEntityType.SCULK_SHRIEKER).ifPresent((p_222169_) -> {
            p_222169_.tryRespond(pLevel);
         });
      }

   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.MODEL;
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return COLLIDER;
   }

   public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return COLLIDER;
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   @Nullable
   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SculkShriekerBlockEntity(pPos, pState);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(pContext.getLevel().getFluidState(pContext.getClickedPos()).getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack, pDropExperience);

   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
      return silkTouchLevel == 0 ? 5 : 0;
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return !pLevel.isClientSide ? BaseEntityBlock.createTickerHelper(pBlockEntityType, BlockEntityType.SCULK_SHRIEKER, (p_281134_, p_281135_, p_281136_, p_281137_) -> {
         VibrationSystem.Ticker.tick(p_281134_, p_281137_.getVibrationData(), p_281137_.getVibrationUser());
      }) : null;
   }
}
