package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpectralArrow extends AbstractArrow {
   private int duration = 200;

   public SpectralArrow(EntityType<? extends SpectralArrow> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public SpectralArrow(Level pLevel, LivingEntity pShooter) {
      super(EntityType.SPECTRAL_ARROW, pShooter, pLevel);
   }

   public SpectralArrow(Level pLevel, double pX, double pY, double pZ) {
      super(EntityType.SPECTRAL_ARROW, pX, pY, pZ, pLevel);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.level().isClientSide && !this.inGround) {
         this.level().addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
      }

   }

   protected ItemStack getPickupItem() {
      return new ItemStack(Items.SPECTRAL_ARROW);
   }

   protected void doPostHurtEffects(LivingEntity pLiving) {
      super.doPostHurtEffects(pLiving);
      MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
      pLiving.addEffect(mobeffectinstance, this.getEffectSource());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Duration")) {
         this.duration = pCompound.getInt("Duration");
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Duration", this.duration);
   }
}