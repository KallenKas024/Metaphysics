package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TooltipRenderUtil {
   public static final int MOUSE_OFFSET = 12;
   private static final int PADDING = 3;
   public static final int PADDING_LEFT = 3;
   public static final int PADDING_RIGHT = 3;
   public static final int PADDING_TOP = 3;
   public static final int PADDING_BOTTOM = 3;
   private static final int BACKGROUND_COLOR = -267386864;
   private static final int BORDER_COLOR_TOP = 1347420415;
   private static final int BORDER_COLOR_BOTTOM = 1344798847;

   public static void renderTooltipBackground(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ) {
      renderTooltipBackground(pGuiGraphics, pX, pY, pWidth, pHeight, pZ, BACKGROUND_COLOR, BACKGROUND_COLOR, BORDER_COLOR_TOP, BORDER_COLOR_BOTTOM);
   }

   // Forge: Allow specifying colors for the inner border gradient and a gradient instead of a single color for the background and outer border
   public static void renderTooltipBackground(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ, int backgroundTop, int backgroundBottom, int borderTop, int borderBottom)
   {
      int i = pX - 3;
      int j = pY - 3;
      int k = pWidth + 3 + 3;
      int l = pHeight + 3 + 3;
      renderHorizontalLine(pGuiGraphics, i, j - 1, k, pZ, backgroundTop);
      renderHorizontalLine(pGuiGraphics, i, j + l, k, pZ, backgroundBottom);
      renderRectangle(pGuiGraphics, i, j, k, l, pZ, backgroundTop, backgroundBottom);
      renderVerticalLineGradient(pGuiGraphics, i - 1, j, l, pZ, backgroundTop, backgroundBottom);
      renderVerticalLineGradient(pGuiGraphics, i + k, j, l, pZ, backgroundTop, backgroundBottom);
      renderFrameGradient(pGuiGraphics, i, j + 1, k, l, pZ, borderTop, borderBottom);
   }

   private static void renderFrameGradient(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ, int pTopColor, int pBottomColor) {
      renderVerticalLineGradient(pGuiGraphics, pX, pY, pHeight - 2, pZ, pTopColor, pBottomColor);
      renderVerticalLineGradient(pGuiGraphics, pX + pWidth - 1, pY, pHeight - 2, pZ, pTopColor, pBottomColor);
      renderHorizontalLine(pGuiGraphics, pX, pY - 1, pWidth, pZ, pTopColor);
      renderHorizontalLine(pGuiGraphics, pX, pY - 1 + pHeight - 1, pWidth, pZ, pBottomColor);
   }

   private static void renderVerticalLine(GuiGraphics pGuiGraphics, int pX, int pY, int pLength, int pZ, int pColor) {
      pGuiGraphics.fill(pX, pY, pX + 1, pY + pLength, pZ, pColor);
   }

   private static void renderVerticalLineGradient(GuiGraphics pGuiGraphics, int pX, int pY, int pLength, int pZ, int pTopColor, int pBottomColor) {
      pGuiGraphics.fillGradient(pX, pY, pX + 1, pY + pLength, pZ, pTopColor, pBottomColor);
   }

   private static void renderHorizontalLine(GuiGraphics pGuiGraphics, int pX, int pY, int pLength, int pZ, int pColor) {
      pGuiGraphics.fill(pX, pY, pX + pLength, pY + 1, pZ, pColor);
   }

   /**
   * @deprecated Forge: Use gradient overload instead
   */
   @Deprecated
   private static void renderRectangle(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ, int pColor) {
      renderRectangle(pGuiGraphics, pX, pY, pWidth, pHeight, pZ, pColor, pColor);
   }

   // Forge: Allow specifying a gradient instead of a single color for the background
   private static void renderRectangle(GuiGraphics pGuiGraphics, int pX, int pY, int pWidth, int pHeight, int pZ, int pColor, int colorTo) {
      pGuiGraphics.fillGradient(pX, pY, pX + pWidth, pY + pHeight, pZ, pColor, colorTo);
   }
}
