package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class WaterFluid extends FlowingFluid {
   public Fluid getFlowing() {
      return Fluids.FLOWING_WATER;
   }

   public Fluid getSource() {
      return Fluids.WATER;
   }

   public Item getBucket() {
      return Items.WATER_BUCKET;
   }

   public void animateTick(Level pLevel, BlockPos pPos, FluidState pState, RandomSource pRandom) {
      if (!pState.isSource() && !pState.getValue(FALLING)) {
         if (pRandom.nextInt(64) == 0) {
            pLevel.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, pRandom.nextFloat() * 0.25F + 0.75F, pRandom.nextFloat() + 0.5F, false);
         }
      } else if (pRandom.nextInt(10) == 0) {
         pLevel.addParticle(ParticleTypes.UNDERWATER, (double)pPos.getX() + pRandom.nextDouble(), (double)pPos.getY() + pRandom.nextDouble(), (double)pPos.getZ() + pRandom.nextDouble(), 0.0D, 0.0D, 0.0D);
      }

   }

   @Nullable
   public ParticleOptions getDripParticle() {
      return ParticleTypes.DRIPPING_WATER;
   }

   protected boolean canConvertToSource(Level pLevel) {
      return pLevel.getGameRules().getBoolean(GameRules.RULE_WATER_SOURCE_CONVERSION);
   }

   protected void beforeDestroyingBlock(LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
      BlockEntity blockentity = pState.hasBlockEntity() ? pLevel.getBlockEntity(pPos) : null;
      Block.dropResources(pState, pLevel, pPos, blockentity);
   }

   public int getSlopeFindDistance(LevelReader pLevel) {
      return 4;
   }

   public BlockState createLegacyBlock(FluidState pState) {
      return Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(pState)));
   }

   public boolean isSame(Fluid pFluid) {
      return pFluid == Fluids.WATER || pFluid == Fluids.FLOWING_WATER;
   }

   public int getDropOff(LevelReader pLevel) {
      return 1;
   }

   public int getTickDelay(LevelReader pLevel) {
      return 5;
   }

   public boolean canBeReplacedWith(FluidState pFluidState, BlockGetter pBlockReader, BlockPos pPos, Fluid pFluid, Direction pDirection) {
      return pDirection == Direction.DOWN && !pFluid.is(FluidTags.WATER);
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL);
   }

   public static class Flowing extends WaterFluid {
      protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> pBuilder) {
         super.createFluidStateDefinition(pBuilder);
         pBuilder.add(LEVEL);
      }

      public int getAmount(FluidState pState) {
         return pState.getValue(LEVEL);
      }

      public boolean isSource(FluidState pState) {
         return false;
      }
   }

   public static class Source extends WaterFluid {
      public int getAmount(FluidState pState) {
         return 8;
      }

      public boolean isSource(FluidState pState) {
         return true;
      }
   }
}