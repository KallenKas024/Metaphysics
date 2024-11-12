package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener.Holder<SculkCatalystBlockEntity.CatalystListener> {
   private final SculkCatalystBlockEntity.CatalystListener catalystListener;

   public SculkCatalystBlockEntity(BlockPos pPos, BlockState pBlockState) {
      super(BlockEntityType.SCULK_CATALYST, pPos, pBlockState);
      this.catalystListener = new SculkCatalystBlockEntity.CatalystListener(pBlockState, new BlockPositionSource(pPos));
   }

   public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, SculkCatalystBlockEntity pSculkCatalyst) {
      pSculkCatalyst.catalystListener.getSculkSpreader().updateCursors(pLevel, pPos, pLevel.getRandom(), true);
   }

   public void load(CompoundTag pTag) {
      this.catalystListener.sculkSpreader.load(pTag);
   }

   protected void saveAdditional(CompoundTag pTag) {
      this.catalystListener.sculkSpreader.save(pTag);
      super.saveAdditional(pTag);
   }

   public SculkCatalystBlockEntity.CatalystListener getListener() {
      return this.catalystListener;
   }

   public static class CatalystListener implements GameEventListener {
      public static final int PULSE_TICKS = 8;
      final SculkSpreader sculkSpreader;
      private final BlockState blockState;
      private final PositionSource positionSource;

      public CatalystListener(BlockState pBlockState, PositionSource pPositionSource) {
         this.blockState = pBlockState;
         this.positionSource = pPositionSource;
         this.sculkSpreader = SculkSpreader.createLevelSpreader();
      }

      /**
       * Gets the position of the listener itself.
       */
      public PositionSource getListenerSource() {
         return this.positionSource;
      }

      /**
       * Gets the listening radius of the listener. Events within this radius will notify the listener when broadcasted.
       */
      public int getListenerRadius() {
         return 8;
      }

      public GameEventListener.DeliveryMode getDeliveryMode() {
         return GameEventListener.DeliveryMode.BY_DISTANCE;
      }

      public boolean handleGameEvent(ServerLevel pLevel, GameEvent pGameEvent, GameEvent.Context pContext, Vec3 pPos) {
         if (pGameEvent == GameEvent.ENTITY_DIE) {
            Entity $$5 = pContext.sourceEntity();
            if ($$5 instanceof LivingEntity) {
               LivingEntity livingentity = (LivingEntity)$$5;
               if (!livingentity.wasExperienceConsumed()) {
                  int i = livingentity.getExperienceReward();
                  if (livingentity.shouldDropExperience() && i > 0) {
                     this.sculkSpreader.addCursors(BlockPos.containing(pPos.relative(Direction.UP, 0.5D)), i);
                     this.tryAwardItSpreadsAdvancement(pLevel, livingentity);
                  }

                  livingentity.skipDropExperience();
                  this.positionSource.getPosition(pLevel).ifPresent((p_289513_) -> {
                     this.bloom(pLevel, BlockPos.containing(p_289513_), this.blockState, pLevel.getRandom());
                  });
               }

               return true;
            }
         }

         return false;
      }

      @VisibleForTesting
      public SculkSpreader getSculkSpreader() {
         return this.sculkSpreader;
      }

      private void bloom(ServerLevel pLevel, BlockPos pPos, BlockState pState, RandomSource pRandom) {
         pLevel.setBlock(pPos, pState.setValue(SculkCatalystBlock.PULSE, Boolean.valueOf(true)), 3);
         pLevel.scheduleTick(pPos, pState.getBlock(), 8);
         pLevel.sendParticles(ParticleTypes.SCULK_SOUL, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 1.15D, (double)pPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
         pLevel.playSound((Player)null, pPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + pRandom.nextFloat() * 0.4F);
      }

      private void tryAwardItSpreadsAdvancement(Level pLevel, LivingEntity pEntity) {
         LivingEntity livingentity = pEntity.getLastHurtByMob();
         if (livingentity instanceof ServerPlayer serverplayer) {
            DamageSource damagesource = pEntity.getLastDamageSource() == null ? pLevel.damageSources().playerAttack(serverplayer) : pEntity.getLastDamageSource();
            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger(serverplayer, pEntity, damagesource);
         }

      }
   }
}