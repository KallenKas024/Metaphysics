package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractCandleBlock extends Block {
   public static final int LIGHT_PER_CANDLE = 3;
   public static final BooleanProperty LIT = BlockStateProperties.LIT;

   protected AbstractCandleBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   protected abstract Iterable<Vec3> getParticleOffsets(BlockState pState);

   public static boolean isLit(BlockState pState) {
      return pState.hasProperty(LIT) && (pState.is(BlockTags.CANDLES) || pState.is(BlockTags.CANDLE_CAKES)) && pState.getValue(LIT);
   }

   public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
      if (!pLevel.isClientSide && pProjectile.isOnFire() && this.canBeLit(pState)) {
         setLit(pLevel, pState, pHit.getBlockPos(), true);
      }

   }

   protected boolean canBeLit(BlockState pState) {
      return !pState.getValue(LIT);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(LIT)) {
         this.getParticleOffsets(pState).forEach((p_220695_) -> {
            addParticlesAndSound(pLevel, p_220695_.add((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ()), pRandom);
         });
      }
   }

   private static void addParticlesAndSound(Level pLevel, Vec3 pOffset, RandomSource pRandom) {
      float f = pRandom.nextFloat();
      if (f < 0.3F) {
         pLevel.addParticle(ParticleTypes.SMOKE, pOffset.x, pOffset.y, pOffset.z, 0.0D, 0.0D, 0.0D);
         if (f < 0.17F) {
            pLevel.playLocalSound(pOffset.x + 0.5D, pOffset.y + 0.5D, pOffset.z + 0.5D, SoundEvents.CANDLE_AMBIENT, SoundSource.BLOCKS, 1.0F + pRandom.nextFloat(), pRandom.nextFloat() * 0.7F + 0.3F, false);
         }
      }

      pLevel.addParticle(ParticleTypes.SMALL_FLAME, pOffset.x, pOffset.y, pOffset.z, 0.0D, 0.0D, 0.0D);
   }

   public static void extinguish(@Nullable Player pPlayer, BlockState pState, LevelAccessor pLevel, BlockPos pPos) {
      setLit(pLevel, pState, pPos, false);
      if (pState.getBlock() instanceof AbstractCandleBlock) {
         ((AbstractCandleBlock)pState.getBlock()).getParticleOffsets(pState).forEach((p_151926_) -> {
            pLevel.addParticle(ParticleTypes.SMOKE, (double)pPos.getX() + p_151926_.x(), (double)pPos.getY() + p_151926_.y(), (double)pPos.getZ() + p_151926_.z(), 0.0D, (double)0.1F, 0.0D);
         });
      }

      pLevel.playSound((Player)null, pPos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
      pLevel.gameEvent(pPlayer, GameEvent.BLOCK_CHANGE, pPos);
   }

   private static void setLit(LevelAccessor pLevel, BlockState pState, BlockPos pPos, boolean pLit) {
      pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(pLit)), 11);
   }
}