package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;

public class RestrictSunGoal extends Goal {
   private final PathfinderMob mob;

   public RestrictSunGoal(PathfinderMob pMob) {
      this.mob = pMob;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.level().isDay() && this.mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() && GoalUtils.hasGroundPathNavigation(this.mob);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      ((GroundPathNavigation)this.mob.getNavigation()).setAvoidSun(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      if (GoalUtils.hasGroundPathNavigation(this.mob)) {
         ((GroundPathNavigation)this.mob.getNavigation()).setAvoidSun(false);
      }

   }
}