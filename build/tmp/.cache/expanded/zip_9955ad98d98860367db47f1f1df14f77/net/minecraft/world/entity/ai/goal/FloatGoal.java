package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;

public class FloatGoal extends Goal {
   private final Mob mob;

   public FloatGoal(Mob pMob) {
      this.mob = pMob;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP));
      pMob.getNavigation().setCanFloat(true);
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getFluidJumpThreshold() || this.mob.isInLava() || this.mob.isInFluidType((fluidType, height) -> this.mob.canSwimInFluidType(fluidType) && height > this.mob.getFluidJumpThreshold());
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (this.mob.getRandom().nextFloat() < 0.8F) {
         this.mob.getJumpControl().jump();
      }

   }
}
