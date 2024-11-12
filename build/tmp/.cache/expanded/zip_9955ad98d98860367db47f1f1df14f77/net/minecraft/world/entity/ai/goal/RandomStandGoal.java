package net.minecraft.world.entity.ai.goal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class RandomStandGoal extends Goal {
   private final AbstractHorse horse;
   private int nextStand;

   public RandomStandGoal(AbstractHorse pHorse) {
      this.horse = pHorse;
      this.resetStandInterval(pHorse);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.horse.standIfPossible();
      this.playStandSound();
   }

   private void playStandSound() {
      SoundEvent soundevent = this.horse.getAmbientStandSound();
      if (soundevent != null) {
         this.horse.playSound(soundevent);
      }

   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return false;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      ++this.nextStand;
      if (this.nextStand > 0 && this.horse.getRandom().nextInt(1000) < this.nextStand) {
         this.resetStandInterval(this.horse);
         return !this.horse.isImmobile() && this.horse.getRandom().nextInt(10) == 0;
      } else {
         return false;
      }
   }

   private void resetStandInterval(AbstractHorse pHorse) {
      this.nextStand = -pHorse.getAmbientStandInterval();
   }

   public boolean requiresUpdateEveryTick() {
      return true;
   }
}