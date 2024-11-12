package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpacerElement implements LayoutElement {
   private int x;
   private int y;
   private final int width;
   private final int height;

   public SpacerElement(int pWidth, int pHeight) {
      this(0, 0, pWidth, pHeight);
   }

   public SpacerElement(int pX, int pY, int pWidth, int pHeight) {
      this.x = pX;
      this.y = pY;
      this.width = pWidth;
      this.height = pHeight;
   }

   public static SpacerElement width(int pWidth) {
      return new SpacerElement(pWidth, 0);
   }

   public static SpacerElement height(int pHeight) {
      return new SpacerElement(0, pHeight);
   }

   public void setX(int pX) {
      this.x = pX;
   }

   public void setY(int pY) {
      this.y = pY;
   }

   public int getX() {
      return this.x;
   }

   public int getY() {
      return this.y;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public void visitWidgets(Consumer<AbstractWidget> pConsumer) {
   }
}