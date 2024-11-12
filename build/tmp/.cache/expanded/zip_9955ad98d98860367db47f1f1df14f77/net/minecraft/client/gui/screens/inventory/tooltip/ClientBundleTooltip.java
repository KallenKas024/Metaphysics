package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBundleTooltip implements ClientTooltipComponent {
   public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/container/bundle.png");
   private static final int MARGIN_Y = 4;
   private static final int BORDER_WIDTH = 1;
   private static final int TEX_SIZE = 128;
   private static final int SLOT_SIZE_X = 18;
   private static final int SLOT_SIZE_Y = 20;
   private final NonNullList<ItemStack> items;
   private final int weight;

   public ClientBundleTooltip(BundleTooltip pBundleTooltip) {
      this.items = pBundleTooltip.getItems();
      this.weight = pBundleTooltip.getWeight();
   }

   public int getHeight() {
      return this.gridSizeY() * 20 + 2 + 4;
   }

   public int getWidth(Font pFont) {
      return this.gridSizeX() * 18 + 2;
   }

   public void renderImage(Font pFont, int pX, int pY, GuiGraphics pGuiGraphics) {
      int i = this.gridSizeX();
      int j = this.gridSizeY();
      boolean flag = this.weight >= 64;
      int k = 0;

      for(int l = 0; l < j; ++l) {
         for(int i1 = 0; i1 < i; ++i1) {
            int j1 = pX + i1 * 18 + 1;
            int k1 = pY + l * 20 + 1;
            this.renderSlot(j1, k1, k++, flag, pGuiGraphics, pFont);
         }
      }

      this.drawBorder(pX, pY, i, j, pGuiGraphics);
   }

   private void renderSlot(int pX, int pY, int pItemIndex, boolean pIsBundleFull, GuiGraphics pGuiGraphics, Font pFont) {
      if (pItemIndex >= this.items.size()) {
         this.blit(pGuiGraphics, pX, pY, pIsBundleFull ? ClientBundleTooltip.Texture.BLOCKED_SLOT : ClientBundleTooltip.Texture.SLOT);
      } else {
         ItemStack itemstack = this.items.get(pItemIndex);
         this.blit(pGuiGraphics, pX, pY, ClientBundleTooltip.Texture.SLOT);
         pGuiGraphics.renderItem(itemstack, pX + 1, pY + 1, pItemIndex);
         pGuiGraphics.renderItemDecorations(pFont, itemstack, pX + 1, pY + 1);
         if (pItemIndex == 0) {
            AbstractContainerScreen.renderSlotHighlight(pGuiGraphics, pX + 1, pY + 1, 0);
         }

      }
   }

   private void drawBorder(int pX, int pY, int pSlotWidth, int pSlotHeight, GuiGraphics pGuiGraphics) {
      this.blit(pGuiGraphics, pX, pY, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);
      this.blit(pGuiGraphics, pX + pSlotWidth * 18 + 1, pY, ClientBundleTooltip.Texture.BORDER_CORNER_TOP);

      for(int i = 0; i < pSlotWidth; ++i) {
         this.blit(pGuiGraphics, pX + 1 + i * 18, pY, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_TOP);
         this.blit(pGuiGraphics, pX + 1 + i * 18, pY + pSlotHeight * 20, ClientBundleTooltip.Texture.BORDER_HORIZONTAL_BOTTOM);
      }

      for(int j = 0; j < pSlotHeight; ++j) {
         this.blit(pGuiGraphics, pX, pY + j * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
         this.blit(pGuiGraphics, pX + pSlotWidth * 18 + 1, pY + j * 20 + 1, ClientBundleTooltip.Texture.BORDER_VERTICAL);
      }

      this.blit(pGuiGraphics, pX, pY + pSlotHeight * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
      this.blit(pGuiGraphics, pX + pSlotWidth * 18 + 1, pY + pSlotHeight * 20, ClientBundleTooltip.Texture.BORDER_CORNER_BOTTOM);
   }

   private void blit(GuiGraphics pGuiGraphics, int pX, int pY, ClientBundleTooltip.Texture pTexture) {
      pGuiGraphics.blit(TEXTURE_LOCATION, pX, pY, 0, (float)pTexture.x, (float)pTexture.y, pTexture.w, pTexture.h, 128, 128);
   }

   private int gridSizeX() {
      return Math.max(2, (int)Math.ceil(Math.sqrt((double)this.items.size() + 1.0D)));
   }

   private int gridSizeY() {
      return (int)Math.ceil(((double)this.items.size() + 1.0D) / (double)this.gridSizeX());
   }

   @OnlyIn(Dist.CLIENT)
   static enum Texture {
      SLOT(0, 0, 18, 20),
      BLOCKED_SLOT(0, 40, 18, 20),
      BORDER_VERTICAL(0, 18, 1, 20),
      BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
      BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
      BORDER_CORNER_TOP(0, 20, 1, 1),
      BORDER_CORNER_BOTTOM(0, 60, 1, 1);

      public final int x;
      public final int y;
      public final int w;
      public final int h;

      private Texture(int pX, int pY, int pW, int pH) {
         this.x = pX;
         this.y = pY;
         this.w = pW;
         this.h = pH;
      }
   }
}