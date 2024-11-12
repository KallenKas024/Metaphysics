package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

public class SwellGoal extends Goal {
   private final Creeper creeper;
   @Nullable
   private LivingEntity target;

   public SwellGoal(Creeper pCreeper) {
      this.creeper = pCreeper;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      LivingEntity livingentity = this.creeper.getTarget();
      return this.creeper.getSwellDir() > 0 || livingentity != null && this.creeper.distanceToSqr(livingentity) < 9.0D;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.creeper.getNavigation().stop();
      this.target = this.creeper.getTarget();
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.target = null;
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (this.target == null) {
         this.creeper.setSwellDir(-1);
      } else if (this.creeper.distanceToSqr(this.target) > 49.0D) {
         this.creeper.setSwellDir(-1);
      } else if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
         this.creeper.setSwellDir(-1);
      } else {
         this.creeper.setSwellDir(1);
      }
   }
}