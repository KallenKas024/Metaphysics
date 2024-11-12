package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
   @Deprecated // Use getFluid
   private final FlowingFluid fluid;
   private final List<FluidState> stateCache;
   public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST);

   @Deprecated  // Forge: Use the constructor that takes a supplier
   public LiquidBlock(FlowingFluid pFluid, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.fluid = pFluid;
      this.stateCache = Lists.newArrayList();
      this.stateCache.add(pFluid.getSource(false));

      for(int i = 1; i < 8; ++i) {
         this.stateCache.add(pFluid.getFlowing(8 - i, false));
      }

      this.stateCache.add(pFluid.getFlowing(8, true));
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
      fluidStateCacheInitialized = true;
      supplier = net.minecraftforge.registries.ForgeRegistries.FLUIDS.getDelegateOrThrow(pFluid);
   }

   /**
    * @param pFluid A fluid supplier such as {@link net.minecraftforge.registries.RegistryObject<FlowingFluid>}
    */
   public LiquidBlock(java.util.function.Supplier<? extends FlowingFluid> pFluid, BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.fluid = null;
      this.stateCache = Lists.newArrayList();
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
      this.supplier = pFluid;
   }

   public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return pContext.isAbove(STABLE_SHAPE, pPos, true) && pState.getValue(LEVEL) == 0 && pContext.canStandOnFluid(pLevel.getFluidState(pPos.above()), pState.getFluidState()) ? STABLE_SHAPE : Shapes.empty();
   }

   /**
    * @return whether this block needs random ticking.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getFluidState().isRandomlyTicking();
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      pState.getFluidState().randomTick(pLevel, pPos, pRandom);
   }

   public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
      return false;
   }

   public boolean isPathfindable(BlockState pState, BlockGetter pLevel, BlockPos pPos, PathComputationType pType) {
      return !this.fluid.is(FluidTags.LAVA);
   }

   public FluidState getFluidState(BlockState pState) {
      int i = pState.getValue(LEVEL);
      if (!fluidStateCacheInitialized) initFluidStateCache();
      return this.stateCache.get(Math.min(i, 8));
   }

   public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
      return pAdjacentBlockState.getFluidState().getType().isSame(this.fluid);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getRenderShape}
    * whenever possible. Implementing/overriding is fine.
    */
   public RenderShape getRenderShape(BlockState pState) {
      return RenderShape.INVISIBLE;
   }

   public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
      return Collections.emptyList();
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return Shapes.empty();
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!net.minecraftforge.fluids.FluidInteractionRegistry.canInteract(pLevel, pPos)) {
         pLevel.scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getFluidState().isSource() || pFacingState.getFluidState().isSource()) {
         pLevel.scheduleTick(pCurrentPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!net.minecraftforge.fluids.FluidInteractionRegistry.canInteract(pLevel, pPos)) {
         pLevel.scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

   }

   @Deprecated // FORGE: Use FluidInteractionRegistry#canInteract instead
   private boolean shouldSpreadLiquid(Level pLevel, BlockPos pPos, BlockState pState) {
      if (this.fluid.is(FluidTags.LAVA)) {
         boolean flag = pLevel.getBlockState(pPos.below()).is(Blocks.SOUL_SOIL);

         for(Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
            BlockPos blockpos = pPos.relative(direction.getOpposite());
            if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
               Block block = pLevel.getFluidState(pPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
               pLevel.setBlockAndUpdate(pPos, block.defaultBlockState());
               this.fizz(pLevel, pPos);
               return false;
            }

            if (flag && pLevel.getBlockState(blockpos).is(Blocks.BLUE_ICE)) {
               pLevel.setBlockAndUpdate(pPos, Blocks.BASALT.defaultBlockState());
               this.fizz(pLevel, pPos);
               return false;
            }
         }
      }

      return true;
   }

   private void fizz(LevelAccessor pLevel, BlockPos pPos) {
      pLevel.levelEvent(1501, pPos, 0);
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      if (pState.getValue(LEVEL) == 0) {
         pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
         return new ItemStack(this.fluid.getBucket());
      } else {
         return ItemStack.EMPTY;
      }
   }

   // Forge start
   private final java.util.function.Supplier<? extends net.minecraft.world.level.material.Fluid> supplier;
   public FlowingFluid getFluid() {
      return (FlowingFluid)supplier.get();
   }

   private boolean fluidStateCacheInitialized = false;
   protected synchronized void initFluidStateCache() {
      if (fluidStateCacheInitialized == false) {
         this.stateCache.add(getFluid().getSource(false));

         for (int i = 1; i < 8; ++i)
            this.stateCache.add(getFluid().getFlowing(8 - i, false));

         this.stateCache.add(getFluid().getFlowing(8, true));
         fluidStateCacheInitialized = true;
      }
   }

   public Optional<SoundEvent> getPickupSound() {
      return this.fluid.getPickupSound();
   }
}
