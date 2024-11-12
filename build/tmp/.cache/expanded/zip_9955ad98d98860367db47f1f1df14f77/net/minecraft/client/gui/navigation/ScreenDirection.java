package net.minecraft.client.gui.navigation;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ScreenDirection {
   UP,
   DOWN,
   LEFT,
   RIGHT;

   private final IntComparator coordinateValueComparator = (p_265081_, p_265641_) -> {
      return p_265081_ == p_265641_ ? 0 : (this.isBefore(p_265081_, p_265641_) ? -1 : 1);
   };

   public ScreenAxis getAxis() {
      ScreenAxis screenaxis;
      switch (this) {
         case UP:
         case DOWN:
            screenaxis = ScreenAxis.VERTICAL;
            break;
         case LEFT:
         case RIGHT:
            screenaxis = ScreenAxis.HORIZONTAL;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return screenaxis;
   }

   public ScreenDirection getOpposite() {
      ScreenDirection screendirection;
      switch (this) {
         case UP:
            screendirection = DOWN;
            break;
         case DOWN:
            screendirection = UP;
            break;
         case LEFT:
            screendirection = RIGHT;
            break;
         case RIGHT:
            screendirection = LEFT;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return screendirection;
   }

   public boolean isPositive() {
      boolean flag;
      switch (this) {
         case UP:
         case LEFT:
            flag = false;
            break;
         case DOWN:
         case RIGHT:
            flag = true;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return flag;
   }

   public boolean isAfter(int pFirst, int pSecond) {
      if (this.isPositive()) {
         return pFirst > pSecond;
      } else {
         return pSecond > pFirst;
      }
   }

   public boolean isBefore(int pFirst, int pSecond) {
      if (this.isPositive()) {
         return pFirst < pSecond;
      } else {
         return pSecond < pFirst;
      }
   }

   public IntComparator coordinateValueComparator() {
      return this.coordinateValueComparator;
   }
}