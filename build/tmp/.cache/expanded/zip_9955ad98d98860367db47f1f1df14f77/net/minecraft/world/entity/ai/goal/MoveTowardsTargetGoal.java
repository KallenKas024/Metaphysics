package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveTowardsTargetGoal extends Goal {
   private final PathfinderMob mob;
   @Nullable
   private LivingEntity target;
   private double wantedX;
   private double wantedY;
   private double wantedZ;
   private final double speedModifier;
   private final float within;

   public MoveTowardsTargetGoal(PathfinderMob pMob, double pSpeedModifier, float pWithin) {
      this.mob = pMob;
      this.speedModifier = pSpeedModifier;
      this.within = pWithin;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      this.target = this.mob.getTarget();
      if (this.target == null) {
         return false;
      } else if (this.target.distanceToSqr(this.mob) > (double)(this.within * this.within)) {
         return false;
      } else {
         Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, 16, 7, this.target.position(), (double)((float)Math.PI / 2F));
         if (vec3 == null) {
            return false;
         } else {
            this.wantedX = vec3.x;
            this.wantedY = vec3.y;
            this.wantedZ = vec3.z;
            return true;
         }
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceToSqr(this.mob) < (double)(this.within * this.within);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.target = null;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
   }
}