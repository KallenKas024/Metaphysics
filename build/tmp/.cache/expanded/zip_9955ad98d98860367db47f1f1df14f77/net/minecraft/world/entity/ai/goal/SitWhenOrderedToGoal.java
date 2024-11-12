package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public class SitWhenOrderedToGoal extends Goal {
   private final TamableAnimal mob;

   public SitWhenOrderedToGoal(TamableAnimal pMob) {
      this.mob = pMob;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.mob.isOrderedToSit();
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (!this.mob.isTame()) {
         return false;
      } else if (this.mob.isInWaterOrBubble()) {
         return false;
      } else if (!this.mob.onGround()) {
         return false;
      } else {
         LivingEntity livingentity = this.mob.getOwner();
         if (livingentity == null) {
            return true;
         } else {
            return this.mob.distanceToSqr(livingentity) < 144.0D && livingentity.getLastHurtByMob() != null ? false : this.mob.isOrderedToSit();
         }
      }
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.getNavigation().stop();
      this.mob.setInSittingPose(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.setInSittingPose(false);
   }
}