package net.minecraft.world.entity.ai.control;

import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LookControl implements Control {
   protected final Mob mob;
   protected float yMaxRotSpeed;
   protected float xMaxRotAngle;
   protected int lookAtCooldown;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;

   public LookControl(Mob pMob) {
      this.mob = pMob;
   }

   /**
    * Sets the mob's look vector
    */
   public void setLookAt(Vec3 pLookVector) {
      this.setLookAt(pLookVector.x, pLookVector.y, pLookVector.z);
   }

   /**
    * Sets the controlling mob's look vector to the provided entity's location
    */
   public void setLookAt(Entity pEntity) {
      this.setLookAt(pEntity.getX(), getWantedY(pEntity), pEntity.getZ());
   }

   /**
    * Sets position to look at using entity
    */
   public void setLookAt(Entity pEntity, float pDeltaYaw, float pDeltaPitch) {
      this.setLookAt(pEntity.getX(), getWantedY(pEntity), pEntity.getZ(), pDeltaYaw, pDeltaPitch);
   }

   public void setLookAt(double pX, double pY, double pZ) {
      this.setLookAt(pX, pY, pZ, (float)this.mob.getHeadRotSpeed(), (float)this.mob.getMaxHeadXRot());
   }

   /**
    * Sets position to look at
    */
   public void setLookAt(double pX, double pY, double pZ, float pDeltaYaw, float pDeltaPitch) {
      this.wantedX = pX;
      this.wantedY = pY;
      this.wantedZ = pZ;
      this.yMaxRotSpeed = pDeltaYaw;
      this.xMaxRotAngle = pDeltaPitch;
      this.lookAtCooldown = 2;
   }

   /**
    * Updates look
    */
   public void tick() {
      if (this.resetXRotOnTick()) {
         this.mob.setXRot(0.0F);
      }

      if (this.lookAtCooldown > 0) {
         --this.lookAtCooldown;
         this.getYRotD().ifPresent((p_287447_) -> {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, p_287447_, this.yMaxRotSpeed);
         });
         this.getXRotD().ifPresent((p_289400_) -> {
            this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), p_289400_, this.xMaxRotAngle));
         });
      } else {
         this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
      }

      this.clampHeadRotationToBody();
   }

   protected void clampHeadRotationToBody() {
      if (!this.mob.getNavigation().isDone()) {
         this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, (float)this.mob.getMaxHeadYRot());
      }

   }

   protected boolean resetXRotOnTick() {
      return true;
   }

   public boolean isLookingAtTarget() {
      return this.lookAtCooldown > 0;
   }

   public double getWantedX() {
      return this.wantedX;
   }

   public double getWantedY() {
      return this.wantedY;
   }

   public double getWantedZ() {
      return this.wantedZ;
   }

   protected Optional<Float> getXRotD() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedY - this.mob.getEyeY();
      double d2 = this.wantedZ - this.mob.getZ();
      double d3 = Math.sqrt(d0 * d0 + d2 * d2);
      return !(Math.abs(d1) > (double)1.0E-5F) && !(Math.abs(d3) > (double)1.0E-5F) ? Optional.empty() : Optional.of((float)(-(Mth.atan2(d1, d3) * (double)(180F / (float)Math.PI))));
   }

   protected Optional<Float> getYRotD() {
      double d0 = this.wantedX - this.mob.getX();
      double d1 = this.wantedZ - this.mob.getZ();
      return !(Math.abs(d1) > (double)1.0E-5F) && !(Math.abs(d0) > (double)1.0E-5F) ? Optional.empty() : Optional.of((float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F);
   }

   /**
    * Rotate as much as possible from {@code from} to {@code to} within the bounds of {@code maxDelta}
    */
   protected float rotateTowards(float pFrom, float pTo, float pMaxDelta) {
      float f = Mth.degreesDifference(pFrom, pTo);
      float f1 = Mth.clamp(f, -pMaxDelta, pMaxDelta);
      return pFrom + f1;
   }

   private static double getWantedY(Entity pEntity) {
      return pEntity instanceof LivingEntity ? pEntity.getEyeY() : (pEntity.getBoundingBox().minY + pEntity.getBoundingBox().maxY) / 2.0D;
   }
}