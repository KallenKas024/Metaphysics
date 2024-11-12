package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FrogspawnBlock extends Block {
   private static final int MIN_TADPOLES_SPAWN = 2;
   private static final int MAX_TADPOLES_SPAWN = 5;
   private static final int DEFAULT_MIN_HATCH_TICK_DELAY = 3600;
   private static final int DEFAULT_MAX_HATCH_TICK_DELAY = 12000;
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.5D, 16.0D);
   private static int minHatchTickDelay = 3600;
   private static int maxHatchTickDelay = 12000;

   public FrogspawnBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      return mayPlaceOn(pLevel, pPos.below());
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
      pLevel.scheduleTick(pPos, this, getFrogspawnHatchDelay(pLevel.getRandom()));
   }

   private static int getFrogspawnHatchDelay(RandomSource pRandom) {
      return pRandom.nextInt(minHatchTickDelay, maxHatchTickDelay);
   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
      return !this.canSurvive(pState, pLevel, pPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
   }

   public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      if (!this.canSurvive(pState, pLevel, pPos)) {
         this.destroyBlock(pLevel, pPos);
      } else {
         this.hatchFrogspawn(pLevel, pPos, pRandom);
      }
   }

   public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
      if (pEntity.getType().equals(EntityType.FALLING_BLOCK)) {
         this.destroyBlock(pLevel, pPos);
      }

   }

   private static boolean mayPlaceOn(BlockGetter pLevel, BlockPos pPos) {
      FluidState fluidstate = pLevel.getFluidState(pPos);
      FluidState fluidstate1 = pLevel.getFluidState(pPos.above());
      return fluidstate.getType() == Fluids.WATER && fluidstate1.getType() == Fluids.EMPTY;
   }

   private void hatchFrogspawn(ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      this.destroyBlock(pLevel, pPos);
      pLevel.playSound((Player)null, pPos, SoundEvents.FROGSPAWN_HATCH, SoundSource.BLOCKS, 1.0F, 1.0F);
      this.spawnTadpoles(pLevel, pPos, pRandom);
   }

   private void destroyBlock(Level pLevel, BlockPos pPos) {
      pLevel.destroyBlock(pPos, false);
   }

   private void spawnTadpoles(ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
      int i = pRandom.nextInt(2, 6);

      for(int j = 1; j <= i; ++j) {
         Tadpole tadpole = EntityType.TADPOLE.create(pLevel);
         if (tadpole != null) {
            double d0 = (double)pPos.getX() + this.getRandomTadpolePositionOffset(pRandom);
            double d1 = (double)pPos.getZ() + this.getRandomTadpolePositionOffset(pRandom);
            int k = pRandom.nextInt(1, 361);
            tadpole.moveTo(d0, (double)pPos.getY() - 0.5D, d1, (float)k, 0.0F);
            tadpole.setPersistenceRequired();
            pLevel.addFreshEntity(tadpole);
         }
      }

   }

   private double getRandomTadpolePositionOffset(RandomSource pRandom) {
      double d0 = (double)(Tadpole.HITBOX_WIDTH / 2.0F);
      return Mth.clamp(pRandom.nextDouble(), d0, 1.0D - d0);
   }

   @VisibleForTesting
   public static void setHatchDelay(int pMinHatchDelay, int pMaxHatchDelay) {
      minHatchTickDelay = pMinHatchDelay;
      maxHatchTickDelay = pMaxHatchDelay;
   }

   @VisibleForTesting
   public static void setDefaultHatchDelay() {
      minHatchTickDelay = 3600;
      maxHatchTickDelay = 12000;
   }
}