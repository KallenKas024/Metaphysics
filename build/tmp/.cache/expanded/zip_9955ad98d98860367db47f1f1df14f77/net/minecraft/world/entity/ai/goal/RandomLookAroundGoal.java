package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;

public class RandomLookAroundGoal extends Goal {
   private final Mob mob;
   private double relX;
   private double relZ;
   private int lookTime;

   public RandomLookAroundGoal(Mob pMob) {
      this.mob = pMob;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.getRandom().nextFloat() < 0.02F;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.lookTime >= 0;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      double d0 = (Math.PI * 2D) * this.mob.getRandom().nextDouble();
      this.relX = Math.cos(d0);
      this.relZ = Math.sin(d0);
      this.lookTime = 20 + this.mob.getRandom().nextInt(20);
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      --this.lookTime;
      this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
   }
}