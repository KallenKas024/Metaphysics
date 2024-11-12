package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EnderChestBlock extends AbstractChestBlock<EnderChestBlockEntity> implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");

   public EnderChestBlock(BlockBehaviour.Properties p_53121_) {
      super(p_53121_, () -> {
         return BlockEntityType.ENDER_CHEST;
      });
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState pState, Level pLevel, BlockPos pPos, boolean pOverride) {
      return DoubleBlockCombiner.Combiner::acceptNone;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.ENTITYBLOCK_ANIMATED;
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      PlayerEnderChestContainer playerenderchestcontainer = pPlayer.getEnderChestInventory();
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (playerenderchestcontainer != null && blockentity instanceof EnderChestBlockEntity) {
         BlockPos blockpos = pPos.above();
         if (pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
         } else if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
         } else {
            EnderChestBlockEntity enderchestblockentity = (EnderChestBlockEntity)blockentity;
            playerenderchestcontainer.setActiveChest(enderchestblockentity);
            pPlayer.openMenu(new SimpleMenuProvider((p_53124_, p_53125_, p_53126_) -> {
               return ChestMenu.threeRows(p_53124_, p_53125_, playerenderchestcontainer);
            }, CONTAINER_TITLE));
            pPlayer.awardStat(Stats.OPEN_ENDERCHEST);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.sidedSuccess(pLevel.isClientSide);
      }
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new EnderChestBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, BlockEntityType.ENDER_CHEST, EnderChestBlockEntity::lidAnimateTick) : null;
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      for(int i = 0; i < 3; ++i) {
         int j = pRandom.nextInt(2) * 2 - 1;
         int k = pRandom.nextInt(2) * 2 - 1;
         double d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)j;
         double d1 = (double)((float)pPos.getY() + pRandom.nextFloat());
         double d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)k;
         double d3 = (double)(pRandom.nextFloat() * (float)j);
         double d4 = ((double)pRandom.nextFloat() - 0.5D) * 0.125D;
         double d5 = (double)(pRandom.nextFloat() * (float)k);
         pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
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

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
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

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof EnderChestBlockEntity) {
         ((EnderChestBlockEntity)blockentity).recheckOpen();
      }

   }
}