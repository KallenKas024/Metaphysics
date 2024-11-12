package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;

public class OpenDoorGoal extends DoorInteractGoal {
   private final boolean closeDoor;
   private int forgetTime;

   public OpenDoorGoal(Mob pMob, boolean pCloseDoor) {
      super(pMob);
      this.mob = pMob;
      this.closeDoor = pCloseDoor;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.closeDoor && this.forgetTime > 0 && super.canContinueToUse();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.forgetTime = 20;
      this.setOpen(true);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.setOpen(false);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      --this.forgetTime;
      super.tick();
   }
}