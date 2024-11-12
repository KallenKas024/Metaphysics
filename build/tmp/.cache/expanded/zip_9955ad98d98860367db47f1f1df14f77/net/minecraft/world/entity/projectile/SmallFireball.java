package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SmallFireball extends Fireball {
   public SmallFireball(EntityType<? extends SmallFireball> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public SmallFireball(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ) {
      super(EntityType.SMALL_FIREBALL, pShooter, pOffsetX, pOffsetY, pOffsetZ, pLevel);
   }

   public SmallFireball(Level pLevel, double pX, double pY, double pZ, double pOffsetX, double pOffsetY, double pOffsetZ) {
      super(EntityType.SMALL_FIREBALL, pX, pY, pZ, pOffsetX, pOffsetY, pOffsetZ, pLevel);
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityHitResult pResult) {
      super.onHitEntity(pResult);
      if (!this.level().isClientSide) {
         Entity entity = pResult.getEntity();
         Entity entity1 = this.getOwner();
         int i = entity.getRemainingFireTicks();
         entity.setSecondsOnFire(5);
         if (!entity.hurt(this.damageSources().fireball(this, entity1), 5.0F)) {
            entity.setRemainingFireTicks(i);
         } else if (entity1 instanceof LivingEntity) {
            this.doEnchantDamageEffects((LivingEntity)entity1, entity);
         }

      }
   }

   protected void onHitBlock(BlockHitResult pResult) {
      super.onHitBlock(pResult);
      if (!this.level().isClientSide) {
         Entity entity = this.getOwner();
         if (!(entity instanceof Mob) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), entity)) {
            BlockPos blockpos = pResult.getBlockPos().relative(pResult.getDirection());
            if (this.level().isEmptyBlock(blockpos)) {
               this.level().setBlockAndUpdate(blockpos, BaseFireBlock.getState(this.level(), blockpos));
            }
         }

      }
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(HitResult pResult) {
      super.onHit(pResult);
      if (!this.level().isClientSide) {
         this.discard();
      }

   }

   /**
    * Returns {@code true} if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return false;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      return false;
   }
}
