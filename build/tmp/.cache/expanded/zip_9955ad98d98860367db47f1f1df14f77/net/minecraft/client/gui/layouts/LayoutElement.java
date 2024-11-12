package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface LayoutElement {
   void setX(int pX);

   void setY(int pY);

   int getX();

   int getY();

   int getWidth();

   int getHeight();

   /**
    * {@return the {@link ScreenRectangle} occupied by the GUI element}
    */
   default ScreenRectangle getRectangle() {
      return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
   }

   default void setPosition(int pX, int pY) {
      this.setX(pX);
      this.setY(pY);
   }

   void visitWidgets(Consumer<AbstractWidget> pConsumer);
}