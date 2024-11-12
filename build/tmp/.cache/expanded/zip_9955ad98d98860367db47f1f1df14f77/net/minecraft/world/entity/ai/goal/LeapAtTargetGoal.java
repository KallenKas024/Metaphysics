package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LeapAtTargetGoal extends Goal {
   private final Mob mob;
   private LivingEntity target;
   private final float yd;

   public LeapAtTargetGoal(Mob pMob, float pYd) {
      this.mob = pMob;
      this.yd = pYd;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.mob.isVehicle()) {
         return false;
      } else {
         this.target = this.mob.getTarget();
         if (this.target == null) {
            return false;
         } else {
            double d0 = this.mob.distanceToSqr(this.target);
            if (!(d0 < 4.0D) && !(d0 > 16.0D)) {
               if (!this.mob.onGround()) {
                  return false;
               } else {
                  return this.mob.getRandom().nextInt(reducedTickDelay(5)) == 0;
               }
            } else {
               return false;
            }
         }
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return !this.mob.onGround();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      Vec3 vec3 = this.mob.getDeltaMovement();
      Vec3 vec31 = new Vec3(this.target.getX() - this.mob.getX(), 0.0D, this.target.getZ() - this.mob.getZ());
      if (vec31.lengthSqr() > 1.0E-7D) {
         vec31 = vec31.normalize().scale(0.4D).add(vec3.scale(0.2D));
      }

      this.mob.setDeltaMovement(vec31.x, (double)this.yd, vec31.z);
   }
}