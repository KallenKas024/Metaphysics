package net.minecraft.client.gui.navigation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ScreenPosition(int x, int y) {
   public static ScreenPosition of(ScreenAxis pAxis, int pPrimaryPosition, int pSecondaryPosition) {
      ScreenPosition screenposition;
      switch (pAxis) {
         case HORIZONTAL:
            screenposition = new ScreenPosition(pPrimaryPosition, pSecondaryPosition);
            break;
         case VERTICAL:
            screenposition = new ScreenPosition(pSecondaryPosition, pPrimaryPosition);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return screenposition;
   }

   public ScreenPosition step(ScreenDirection pDirection) {
      ScreenPosition screenposition;
      switch (pDirection) {
         case DOWN:
            screenposition = new ScreenPosition(this.x, this.y + 1);
            break;
         case UP:
            screenposition = new ScreenPosition(this.x, this.y - 1);
            break;
         case LEFT:
            screenposition = new ScreenPosition(this.x - 1, this.y);
            break;
         case RIGHT:
            screenposition = new ScreenPosition(this.x + 1, this.y);
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return screenposition;
   }

   public int getCoordinate(ScreenAxis pAxis) {
      int i;
      switch (pAxis) {
         case HORIZONTAL:
            i = this.x;
            break;
         case VERTICAL:
            i = this.y;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return i;
   }
}