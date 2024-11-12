package net.minecraft.client.gui.screens.advancements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
enum AdvancementTabType {
   ABOVE(0, 0, 28, 32, 8),
   BELOW(84, 0, 28, 32, 8),
   LEFT(0, 64, 32, 28, 5),
   RIGHT(96, 64, 32, 28, 5);

   public static final int MAX_TABS = java.util.Arrays.stream(values()).mapToInt(e -> e.max).sum();
   private final int textureX;
   private final int textureY;
   private final int width;
   private final int height;
   private final int max;

   private AdvancementTabType(int pTextureX, int pTextureY, int pWidth, int pHeight, int pMax) {
      this.textureX = pTextureX;
      this.textureY = pTextureY;
      this.width = pWidth;
      this.height = pHeight;
      this.max = pMax;
   }

   public int getMax() {
      return this.max;
   }

   public void draw(GuiGraphics pGuiGraphics, int pOffsetX, int pOffsetY, boolean pIsSelected, int pIndex) {
      int i = this.textureX;
      if (pIndex > 0) {
         i += this.width;
      }

      if (pIndex == this.max - 1) {
         i += this.width;
      }

      int j = pIsSelected ? this.textureY + this.height : this.textureY;
      pGuiGraphics.blit(AdvancementsScreen.TABS_LOCATION, pOffsetX + this.getX(pIndex), pOffsetY + this.getY(pIndex), i, j, this.width, this.height);
   }

   public void drawIcon(GuiGraphics pGuiGraphics, int pOffsetX, int pOffsetY, int pIndex, ItemStack pStack) {
      int i = pOffsetX + this.getX(pIndex);
      int j = pOffsetY + this.getY(pIndex);
      switch (this) {
         case ABOVE:
            i += 6;
            j += 9;
            break;
         case BELOW:
            i += 6;
            j += 6;
            break;
         case LEFT:
            i += 10;
            j += 5;
            break;
         case RIGHT:
            i += 6;
            j += 5;
      }

      pGuiGraphics.renderFakeItem(pStack, i, j);
   }

   public int getX(int pIndex) {
      switch (this) {
         case ABOVE:
            return (this.width + 4) * pIndex;
         case BELOW:
            return (this.width + 4) * pIndex;
         case LEFT:
            return -this.width + 4;
         case RIGHT:
            return 248;
         default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
      }
   }

   public int getY(int pIndex) {
      switch (this) {
         case ABOVE:
            return -this.height + 4;
         case BELOW:
            return 136;
         case LEFT:
            return this.height * pIndex;
         case RIGHT:
            return this.height * pIndex;
         default:
            throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
      }
   }

   public boolean isMouseOver(int pOffsetX, int pOffsetY, int pIndex, double pMouseX, double pMouseY) {
      int i = pOffsetX + this.getX(pIndex);
      int j = pOffsetY + this.getY(pIndex);
      return pMouseX > (double)i && pMouseX < (double)(i + this.width) && pMouseY > (double)j && pMouseY < (double)(j + this.height);
   }
}
