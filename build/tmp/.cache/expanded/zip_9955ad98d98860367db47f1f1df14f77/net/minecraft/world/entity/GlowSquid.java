package net.minecraft.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

public class GlowSquid extends Squid {
   private static final EntityDataAccessor<Integer> DATA_DARK_TICKS_REMAINING = SynchedEntityData.defineId(GlowSquid.class, EntityDataSerializers.INT);

   public GlowSquid(EntityType<? extends GlowSquid> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected ParticleOptions getInkParticle() {
      return ParticleTypes.GLOW_SQUID_INK;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_DARK_TICKS_REMAINING, 0);
   }

   protected SoundEvent getSquirtSound() {
      return SoundEvents.GLOW_SQUID_SQUIRT;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.GLOW_SQUID_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.GLOW_SQUID_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.GLOW_SQUID_DEATH;
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("DarkTicksRemaining", this.getDarkTicksRemaining());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setDarkTicks(pCompound.getInt("DarkTicksRemaining"));
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      int i = this.getDarkTicksRemaining();
      if (i > 0) {
         this.setDarkTicks(i - 1);
      }

      this.level().addParticle(ParticleTypes.GLOW, this.getRandomX(0.6D), this.getRandomY(), this.getRandomZ(0.6D), 0.0D, 0.0D, 0.0D);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      boolean flag = super.hurt(pSource, pAmount);
      if (flag) {
         this.setDarkTicks(100);
      }

      return flag;
   }

   private void setDarkTicks(int pDarkTicks) {
      this.entityData.set(DATA_DARK_TICKS_REMAINING, pDarkTicks);
   }

   public int getDarkTicksRemaining() {
      return this.entityData.get(DATA_DARK_TICKS_REMAINING);
   }

   public static boolean checkGlowSquideSpawnRules(EntityType<? extends LivingEntity> pType, ServerLevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pPos.getY() <= pLevel.getSeaLevel() - 33 && pLevel.getRawBrightness(pPos, 0) == 0 && pLevel.getBlockState(pPos).is(Blocks.WATER);
   }
}