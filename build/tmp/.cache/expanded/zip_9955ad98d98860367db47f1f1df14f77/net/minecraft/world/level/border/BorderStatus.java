package net.minecraft.world.level.border;

public enum BorderStatus {
   GROWING(4259712),
   SHRINKING(16724016),
   STATIONARY(2138367);

   private final int color;

   private BorderStatus(int pColor) {
      this.color = pColor;
   }

   /**
    * Retrieves the color that the border should be, while in this state.
    */
   public int getColor() {
      return this.color;
   }
}