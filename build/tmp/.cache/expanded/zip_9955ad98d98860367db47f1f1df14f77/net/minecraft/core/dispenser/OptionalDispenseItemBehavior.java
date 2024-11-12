package net.minecraft.core.dispenser;

import net.minecraft.core.BlockSource;

public abstract class OptionalDispenseItemBehavior extends DefaultDispenseItemBehavior {
   private boolean success = true;

   public boolean isSuccess() {
      return this.success;
   }

   public void setSuccess(boolean pSuccess) {
      this.success = pSuccess;
   }

   /**
    * Play the dispense sound from the specified block.
    */
   protected void playSound(BlockSource pSource) {
      pSource.getLevel().levelEvent(this.isSuccess() ? 1000 : 1001, pSource.getPos(), 0);
   }
}