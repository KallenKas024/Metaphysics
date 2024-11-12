package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.monster.Zombie;

public class ZombieAttackGoal extends MeleeAttackGoal {
   private final Zombie zombie;
   private int raiseArmTicks;

   public ZombieAttackGoal(Zombie pZombie, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
      super(pZombie, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
      this.zombie = pZombie;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.raiseArmTicks = 0;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.zombie.setAggressive(false);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      super.tick();
      ++this.raiseArmTicks;
      if (this.raiseArmTicks >= 5 && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2) {
         this.zombie.setAggressive(true);
      } else {
         this.zombie.setAggressive(false);
      }

   }
}