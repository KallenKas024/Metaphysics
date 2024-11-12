package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock extends Block implements BucketPickup {
   public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;
   private static final int CHECK_PERIOD = 5;

   public BubbleColumnBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      BlockState blockstate = pLevel.getBlockState(pPos.above());
      if (blockstate.isAir()) {
         pEntity.onAboveBubbleCol(pState.getValue(DRAG_DOWN));
         if (!pLevel.isClientSide) {
            ServerLevel serverlevel = (ServerLevel)pLevel;

            for(int i = 0; i < 2; ++i) {
               serverlevel.sendParticles(ParticleTypes.SPLASH, (double)pPos.getX() + pLevel.random.nextDouble(), (double)(pPos.getY() + 1), (double)pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
               serverlevel.sendParticles(ParticleTypes.BUBBLE, (double)pPos.getX() + pLevel.random.nextDouble(), (double)(pPos.getY() + 1), (double)pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.01D, 0.0D, 0.2D);
            }
         }
      } else {
         pEntity.onInsideBubbleColumn(pState.getValue(DRAG_DOWN));
      }

   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      updateColumn(pLevel, pPos, pState, pLevel.getBlockState(pPos.below()));
   }

   public FluidState getFluidState(BlockState pState) {
      return Fluids.WATER.getSource(false);
   }

   public static void updateColumn(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      updateColumn(pLevel, pPos, pLevel.getBlockState(pPos), pState);
   }

   public static void updateColumn(LevelAccessor pLevel, BlockPos pPos, BlockState pFluid, BlockState pState) {
      if (canExistIn(pFluid)) {
         BlockState blockstate = getColumnState(pState);
         pLevel.setBlock(pPos, blockstate, 2);
         BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable().move(Direction.UP);

         while(canExistIn(pLevel.getBlockState(blockpos$mutableblockpos))) {
            if (!pLevel.setBlock(blockpos$mutableblockpos, blockstate, 2)) {
               return;
            }

            blockpos$mutableblockpos.move(Direction.UP);
         }

      }
   }

   private static boolean canExistIn(BlockState pBlockState) {
      return pBlockState.is(Blocks.BUBBLE_COLUMN) || pBlockState.is(Blocks.WATER) && pBlockState.getFluidState().getAmount() >= 8 && pBlockState.getFluidState().isSource();
   }

   private static BlockState getColumnState(BlockState pBlockState) {
      if (pBlockState.is(Blocks.BUBBLE_COLUMN)) {
         return pBlockState;
      } else if (pBlockState.is(Blocks.SOUL_SAND)) {
         return Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(false));
      } else {
         return pBlockState.is(Blocks.MAGMA_BLOCK) ? Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(true)) : Blocks.WATER.defaultBlockState();
      }
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      double d0 = (double)pPos.getX();
      double d1 = (double)pPos.getY();
      double d2 = (double)pPos.getZ();
      if (pState.getValue(DRAG_DOWN)) {
         pLevel.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d0 + 0.5D, d1 + 0.8D, d2, 0.0D, 0.0D, 0.0D);
         if (pRandom.nextInt(200) == 0) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundSource.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
         }
      } else {
         pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
         pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + (double)pRandom.nextFloat(), d1 + (double)pRandom.nextFloat(), d2 + (double)pRandom.nextFloat(), 0.0D, 0.04D, 0.0D);
         if (pRandom.nextInt(200) == 0) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2F + pRandom.nextFloat() * 0.2F, 0.9F + pRandom.nextFloat() * 0.15F, false);
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
      pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      if (!pState.canSurvive(pLevel, pCurrentPos) || pFacing == Direction.DOWN || pFacing == Direction.UP && !pFacingState.is(Blocks.BUBBLE_COLUMN) && canExistIn(pFacingState)) {
         pLevel.scheduleTick(pCurrentPos, this, 5);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      return blockstate.is(Blocks.BUBBLE_COLUMN) || blockstate.is(Blocks.MAGMA_BLOCK) || blockstate.is(Blocks.SOUL_SAND);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return Shapes.empty();
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

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(DRAG_DOWN);
   }

   public ItemStack pickupBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
      return new ItemStack(Items.WATER_BUCKET);
   }

   public Optional<SoundEvent> getPickupSound() {
      return Fluids.WATER.getPickupSound();
   }
}