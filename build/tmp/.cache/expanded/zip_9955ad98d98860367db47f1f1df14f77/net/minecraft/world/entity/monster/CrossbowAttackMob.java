package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface CrossbowAttackMob extends RangedAttackMob {
   void setChargingCrossbow(boolean pChargingCrossbow);

   void shootCrossbowProjectile(LivingEntity pTarget, ItemStack pCrossbowStack, Projectile pProjectile, float pProjectileAngle);

   /**
    * Gets the active target the Goal system uses for tracking
    */
   @Nullable
   LivingEntity getTarget();

   void onCrossbowAttackPerformed();

   default void performCrossbowAttack(LivingEntity pUser, float pVelocity) {
      InteractionHand interactionhand = ProjectileUtil.getWeaponHoldingHand(pUser, item -> item instanceof CrossbowItem);
      ItemStack itemstack = pUser.getItemInHand(interactionhand);
      if (pUser.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
         CrossbowItem.performShooting(pUser.level(), pUser, interactionhand, itemstack, pVelocity, (float)(14 - pUser.level().getDifficulty().getId() * 4));
      }

      this.onCrossbowAttackPerformed();
   }

   default void shootCrossbowProjectile(LivingEntity pUser, LivingEntity pTarget, Projectile pProjectile, float pProjectileAngle, float pVelocity) {
      double d0 = pTarget.getX() - pUser.getX();
      double d1 = pTarget.getZ() - pUser.getZ();
      double d2 = Math.sqrt(d0 * d0 + d1 * d1);
      double d3 = pTarget.getY(0.3333333333333333D) - pProjectile.getY() + d2 * (double)0.2F;
      Vector3f vector3f = this.getProjectileShotVector(pUser, new Vec3(d0, d3, d1), pProjectileAngle);
      pProjectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), pVelocity, (float)(14 - pUser.level().getDifficulty().getId() * 4));
      pUser.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (pUser.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   default Vector3f getProjectileShotVector(LivingEntity pUser, Vec3 pVectorTowardsTarget, float pProjectileAngle) {
      Vector3f vector3f = pVectorTowardsTarget.toVector3f().normalize();
      Vector3f vector3f1 = (new Vector3f((Vector3fc)vector3f)).cross(new Vector3f(0.0F, 1.0F, 0.0F));
      if ((double)vector3f1.lengthSquared() <= 1.0E-7D) {
         Vec3 vec3 = pUser.getUpVector(1.0F);
         vector3f1 = (new Vector3f((Vector3fc)vector3f)).cross(vec3.toVector3f());
      }

      Vector3f vector3f2 = (new Vector3f((Vector3fc)vector3f)).rotateAxis(((float)Math.PI / 2F), vector3f1.x, vector3f1.y, vector3f1.z);
      return (new Vector3f((Vector3fc)vector3f)).rotateAxis(pProjectileAngle * ((float)Math.PI / 180F), vector3f2.x, vector3f2.y, vector3f2.z);
   }
}
