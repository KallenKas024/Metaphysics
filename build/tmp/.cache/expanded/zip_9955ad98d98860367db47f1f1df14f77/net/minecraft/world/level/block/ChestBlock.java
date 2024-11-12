package net.minecraft.world.level.block;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {
   public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final int EVENT_SET_OPEN_COUNT = 1;
   protected static final int AABB_OFFSET = 1;
   protected static final int AABB_HEIGHT = 14;
   protected static final VoxelShape NORTH_AABB = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   protected static final VoxelShape EAST_AABB = Block.box(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D);
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> CHEST_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>>() {
      public Optional<Container> acceptDouble(ChestBlockEntity p_51591_, ChestBlockEntity p_51592_) {
         return Optional.of(new CompoundContainer(p_51591_, p_51592_));
      }

      public Optional<Container> acceptSingle(ChestBlockEntity p_51589_) {
         return Optional.of(p_51589_);
      }

      public Optional<Container> acceptNone() {
         return Optional.empty();
      }
   };
   private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>() {
      public Optional<MenuProvider> acceptDouble(final ChestBlockEntity p_51604_, final ChestBlockEntity p_51605_) {
         final Container container = new CompoundContainer(p_51604_, p_51605_);
         return Optional.of(new MenuProvider() {
            @Nullable
            public AbstractContainerMenu createMenu(int p_51622_, Inventory p_51623_, Player p_51624_) {
               if (p_51604_.canOpen(p_51624_) && p_51605_.canOpen(p_51624_)) {
                  p_51604_.unpackLootTable(p_51623_.player);
                  p_51605_.unpackLootTable(p_51623_.player);
                  return ChestMenu.sixRows(p_51622_, p_51623_, container);
               } else {
                  return null;
               }
            }

            public Component getDisplayName() {
               if (p_51604_.hasCustomName()) {
                  return p_51604_.getDisplayName();
               } else {
                  return (Component)(p_51605_.hasCustomName() ? p_51605_.getDisplayName() : Component.translatable("container.chestDouble"));
               }
            }
         });
      }

      public Optional<MenuProvider> acceptSingle(ChestBlockEntity p_51602_) {
         return Optional.of(p_51602_);
      }

      public Optional<MenuProvider> acceptNone() {
         return Optional.empty();
      }
   };

   public ChestBlock(BlockBehaviour.Properties pProperties, Supplier<BlockEntityType<? extends ChestBlockEntity>> pBlockEntityType) {
      super(pProperties, pBlockEntityType);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public static DoubleBlockCombiner.BlockType getBlockType(BlockState p_51583_) {
      ChestType chesttype = p_51583_.getValue(TYPE);
      if (chesttype == ChestType.SINGLE) {
         return DoubleBlockCombiner.BlockType.SINGLE;
      } else {
         return chesttype == ChestType.RIGHT ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
      }
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

      if (pFacingState.is(this) && pFacing.getAxis().isHorizontal()) {
         ChestType chesttype = pFacingState.getValue(TYPE);
         if (pState.getValue(TYPE) == ChestType.SINGLE && chesttype != ChestType.SINGLE && pState.getValue(FACING) == pFacingState.getValue(FACING) && getConnectedDirection(pFacingState) == pFacing.getOpposite()) {
            return pState.setValue(TYPE, chesttype.getOpposite());
         }
      } else if (getConnectedDirection(pState) == pFacing) {
         return pState.setValue(TYPE, ChestType.SINGLE);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      if (pState.getValue(TYPE) == ChestType.SINGLE) {
         return AABB;
      } else {
         switch (getConnectedDirection(pState)) {
            case NORTH:
            default:
               return NORTH_AABB;
            case SOUTH:
               return SOUTH_AABB;
            case WEST:
               return WEST_AABB;
            case EAST:
               return EAST_AABB;
         }
      }
   }

   /**
    * @return the Direction pointing from the given state to its attached double chest
    */
   public static Direction getConnectedDirection(BlockState p_51585_) {
      Direction direction = p_51585_.getValue(FACING);
      return p_51585_.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
   }

   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      ChestType chesttype = ChestType.SINGLE;
      Direction direction = pContext.getHorizontalDirection().getOpposite();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      boolean flag = pContext.isSecondaryUseActive();
      Direction direction1 = pContext.getClickedFace();
      if (direction1.getAxis().isHorizontal() && flag) {
         Direction direction2 = this.candidatePartnerFacing(pContext, direction1.getOpposite());
         if (direction2 != null && direction2.getAxis() != direction1.getAxis()) {
            direction = direction2;
            chesttype = direction2.getCounterClockWise() == direction1.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
         }
      }

      if (chesttype == ChestType.SINGLE && !flag) {
         if (direction == this.candidatePartnerFacing(pContext, direction.getClockWise())) {
            chesttype = ChestType.LEFT;
         } else if (direction == this.candidatePartnerFacing(pContext, direction.getCounterClockWise())) {
            chesttype = ChestType.RIGHT;
         }
      }

      return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chesttype).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   @Nullable
   private Direction candidatePartnerFacing(BlockPlaceContext pContext, Direction pDirection) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().relative(pDirection));
      return blockstate.is(this) && blockstate.getValue(TYPE) == ChestType.SINGLE ? blockstate.getValue(FACING) : null;
   }

   /**
    * Called by BlockItem after this block has been placed.
    */
   public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         BlockEntity blockentity = pLevel.getBlockEntity(pPos);
         if (blockentity instanceof Container) {
            Containers.dropContents(pLevel, pPos, (Container)blockentity);
            pLevel.updateNeighbourForOutputSignal(pPos, this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
      if (pLevel.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         MenuProvider menuprovider = this.getMenuProvider(pState, pLevel, pPos);
         if (menuprovider != null) {
            pPlayer.openMenu(menuprovider);
            pPlayer.awardStat(this.getOpenChestStat());
            PiglinAi.angerNearbyPiglins(pPlayer, true);
         }

         return InteractionResult.CONSUME;
      }
   }

   protected Stat<ResourceLocation> getOpenChestStat() {
      return Stats.CUSTOM.get(Stats.OPEN_CHEST);
   }

   public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
      return this.blockEntityType.get();
   }

   @Nullable
   public static Container getContainer(ChestBlock pChest, BlockState pState, Level pLevel, BlockPos pPos, boolean pOverride) {
      return pChest.combine(pState, pLevel, pPos, pOverride).apply(CHEST_COMBINER).orElse((Container)null);
   }

   public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState pState, Level pLevel, BlockPos pPos, boolean pOverride) {
      BiPredicate<LevelAccessor, BlockPos> bipredicate;
      if (pOverride) {
         bipredicate = (p_51578_, p_51579_) -> {
            return false;
         };
      } else {
         bipredicate = ChestBlock::isChestBlockedAt;
      }

      return DoubleBlockCombiner.combineWithNeigbour(this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, pState, pLevel, pPos, bipredicate);
   }

   @Nullable
   public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
      return this.combine(pState, pLevel, pPos, false).apply(MENU_PROVIDER_COMBINER).orElse((MenuProvider)null);
   }

   public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final LidBlockEntity pLid) {
      return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() {
         public Float2FloatFunction acceptDouble(ChestBlockEntity p_51633_, ChestBlockEntity p_51634_) {
            return (p_51638_) -> {
               return Math.max(p_51633_.getOpenNess(p_51638_), p_51634_.getOpenNess(p_51638_));
            };
         }

         public Float2FloatFunction acceptSingle(ChestBlockEntity p_51631_) {
            return p_51631_::getOpenNess;
         }

         public Float2FloatFunction acceptNone() {
            return pLid::getOpenNess;
         }
      };
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new ChestBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) : null;
   }

   public static boolean isChestBlockedAt(LevelAccessor p_51509_, BlockPos p_51510_) {
      return isBlockedChestByBlock(p_51509_, p_51510_) || isCatSittingOnChest(p_51509_, p_51510_);
   }

   private static boolean isBlockedChestByBlock(BlockGetter pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.above();
      return pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos);
   }

   private static boolean isCatSittingOnChest(LevelAccessor pLevel, BlockPos pPos) {
      List<Cat> list = pLevel.getEntitiesOfClass(Cat.class, new AABB((double)pPos.getX(), (double)(pPos.getY() + 1), (double)pPos.getZ(), (double)(pPos.getX() + 1), (double)(pPos.getY() + 2), (double)(pPos.getZ() + 1)));
      if (!list.isEmpty()) {
         for(Cat cat : list) {
            if (cat.isInSittingPose()) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#hasAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * Returns the analog signal this block emits. This is the signal a comparator can read from it.
    * 
    * @deprecated call via {@link
    * net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getAnalogOutputSignal} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
      return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(this, pBlockState, pLevel, pPos, false));
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
      BlockState rotated = pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
      return pMirror == Mirror.NONE ? rotated : rotated.setValue(TYPE, rotated.getValue(TYPE).getOpposite());  // Forge: Fixed MC-134110 Structure mirroring breaking apart double chests
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, TYPE, WATERLOGGED);
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return false;
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof ChestBlockEntity) {
         ((ChestBlockEntity)blockentity).recheckOpen();
      }

   }
}
