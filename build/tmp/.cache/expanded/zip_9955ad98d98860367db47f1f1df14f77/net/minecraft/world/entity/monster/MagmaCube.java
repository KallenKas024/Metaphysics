package net.minecraft.world.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class MagmaCube extends Slime {
   public MagmaCube(EntityType<? extends MagmaCube> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.2F);
   }

   public static boolean checkMagmaCubeSpawnRules(EntityType<MagmaCube> pMagmaCube, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getDifficulty() != Difficulty.PEACEFUL;
   }

   public boolean checkSpawnObstruction(LevelReader pLevel) {
      return pLevel.isUnobstructed(this) && !pLevel.containsAnyLiquid(this.getBoundingBox());
   }

   public void setSize(int pSize, boolean pResetHealth) {
      super.setSize(pSize, pResetHealth);
      this.getAttribute(Attributes.ARMOR).setBaseValue((double)(pSize * 3));
   }

   public float getLightLevelDependentMagicValue() {
      return 1.0F;
   }

   protected ParticleOptions getParticleType() {
      return ParticleTypes.FLAME;
   }

   /**
    * Returns {@code true} if the entity is on fire. Used by render to add the fire effect on rendering.
    */
   public boolean isOnFire() {
      return false;
   }

   /**
    * Gets the amount of time the slime needs to wait between jumps.
    */
   protected int getJumpDelay() {
      return super.getJumpDelay() * 4;
   }

   protected void decreaseSquish() {
      this.targetSquish *= 0.9F;
   }

   /**
    * Causes this entity to do an upwards motion (jumping).
    */
   protected void jumpFromGround() {
      Vec3 vec3 = this.getDeltaMovement();
      float f = (float)this.getSize() * 0.1F;
      this.setDeltaMovement(vec3.x, (double)(this.getJumpPower() + f), vec3.z);
      this.hasImpulse = true;
      net.minecraftforge.common.ForgeHooks.onLivingJump(this);
   }

   @Deprecated // FORGE: use jumpInFluid instead
   protected void jumpInLiquid(TagKey<Fluid> pFluidTag) {
      this.jumpInLiquidInternal(() -> pFluidTag == FluidTags.LAVA, () -> super.jumpInLiquid(pFluidTag));
   }

   private void jumpInLiquidInternal(java.util.function.BooleanSupplier isLava, Runnable onSuper) {
      if (isLava.getAsBoolean()) {
         Vec3 vec3 = this.getDeltaMovement();
         this.setDeltaMovement(vec3.x, (double)(0.22F + (float)this.getSize() * 0.05F), vec3.z);
         this.hasImpulse = true;
      } else {
         onSuper.run();
      }

   }

   @Override
   public void jumpInFluid(net.minecraftforge.fluids.FluidType type) {
      this.jumpInLiquidInternal(() -> type == net.minecraftforge.common.ForgeMod.LAVA_TYPE.get(), () -> super.jumpInFluid(type));
   }

   /**
    * Indicates weather the slime is able to damage the player (based upon the slime's size)
    */
   protected boolean isDealsDamage() {
      return this.isEffectiveAi();
   }

   protected float getAttackDamage() {
      return super.getAttackDamage() + 2.0F;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isTiny() ? SoundEvents.MAGMA_CUBE_HURT_SMALL : SoundEvents.MAGMA_CUBE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return this.isTiny() ? SoundEvents.MAGMA_CUBE_DEATH_SMALL : SoundEvents.MAGMA_CUBE_DEATH;
   }

   protected SoundEvent getSquishSound() {
      return this.isTiny() ? SoundEvents.MAGMA_CUBE_SQUISH_SMALL : SoundEvents.MAGMA_CUBE_SQUISH;
   }

   protected SoundEvent getJumpSound() {
      return SoundEvents.MAGMA_CUBE_JUMP;
   }
}
