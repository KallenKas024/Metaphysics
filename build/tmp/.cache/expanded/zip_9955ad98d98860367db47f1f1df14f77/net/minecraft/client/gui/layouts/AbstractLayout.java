package net.minecraft.client.gui.layouts;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractLayout implements Layout {
   private int x;
   private int y;
   protected int width;
   protected int height;

   public AbstractLayout(int pX, int pY, int pWidth, int pHeight) {
      this.x = pX;
      this.y = pY;
      this.width = pWidth;
      this.height = pHeight;
   }

   public void setX(int pX) {
      this.visitChildren((p_265043_) -> {
         int i = p_265043_.getX() + (pX - this.getX());
         p_265043_.setX(i);
      });
      this.x = pX;
   }

   public void setY(int pY) {
      this.visitChildren((p_265586_) -> {
         int i = p_265586_.getY() + (pY - this.getY());
         p_265586_.setY(i);
      });
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

   @OnlyIn(Dist.CLIENT)
   protected abstract static class AbstractChildWrapper {
      public final LayoutElement child;
      public final LayoutSettings.LayoutSettingsImpl layoutSettings;

      protected AbstractChildWrapper(LayoutElement pChild, LayoutSettings pLayoutSettings) {
         this.child = pChild;
         this.layoutSettings = pLayoutSettings.getExposed();
      }

      public int getHeight() {
         return this.child.getHeight() + this.layoutSettings.paddingTop + this.layoutSettings.paddingBottom;
      }

      public int getWidth() {
         return this.child.getWidth() + this.layoutSettings.paddingLeft + this.layoutSettings.paddingRight;
      }

      public void setX(int pX, int pWidth) {
         float f = (float)this.layoutSettings.paddingLeft;
         float f1 = (float)(pWidth - this.child.getWidth() - this.layoutSettings.paddingRight);
         int i = (int)Mth.lerp(this.layoutSettings.xAlignment, f, f1);
         this.child.setX(i + pX);
      }

      public void setY(int pY, int pHeight) {
         float f = (float)this.layoutSettings.paddingTop;
         float f1 = (float)(pHeight - this.child.getHeight() - this.layoutSettings.paddingBottom);
         int i = Math.round(Mth.lerp(this.layoutSettings.yAlignment, f, f1));
         this.child.setY(i + pY);
      }
   }
}