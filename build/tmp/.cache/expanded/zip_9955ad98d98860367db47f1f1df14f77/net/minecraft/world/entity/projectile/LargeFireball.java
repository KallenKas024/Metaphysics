package net.minecraft.world.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class LargeFireball extends Fireball {
   private int explosionPower = 1;

   public LargeFireball(EntityType<? extends LargeFireball> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public LargeFireball(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, int pExplosionPower) {
      super(EntityType.FIREBALL, pShooter, pOffsetX, pOffsetY, pOffsetZ, pLevel);
      this.explosionPower = pExplosionPower;
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);
      if (!this.level().isClientSide) {
         // TODO 1.19.3: The creation of Level.ExplosionInteraction means this code path will fire EntityMobGriefingEvent twice. Should we try and fix it? -SS
         boolean flag = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this.getOwner());
         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionPower, flag, Level.ExplosionInteraction.MOB);
         this.discard();
      }

   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      if (!this.level().isClientSide) {
         Entity entity = pResult.getEntity();
         Entity entity1 = this.getOwner();
         entity.hurt(this.damageSources().fireball(this, entity1), 6.0F);
         if (entity1 instanceof LivingEntity) {
            this.doEnchantDamageEffects((LivingEntity)entity1, entity);
         }

      }
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putByte("ExplosionPower", (byte)this.explosionPower);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("ExplosionPower", 99)) {
         this.explosionPower = pCompound.getByte("ExplosionPower");
      }

   }
}
