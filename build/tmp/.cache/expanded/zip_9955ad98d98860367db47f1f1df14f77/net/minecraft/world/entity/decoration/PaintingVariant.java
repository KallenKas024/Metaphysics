package net.minecraft.world.entity.decoration;

public class PaintingVariant {
   private final int width;
   private final int height;

   public PaintingVariant(int pWidth, int pHeight) {
      this.width = pWidth;
      this.height = pHeight;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }
}