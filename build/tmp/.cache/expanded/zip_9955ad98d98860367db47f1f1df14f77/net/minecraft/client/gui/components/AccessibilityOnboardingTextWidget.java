package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AccessibilityOnboardingTextWidget extends MultiLineTextWidget {
   private static final int BORDER_COLOR_FOCUSED = -1;
   private static final int BORDER_COLOR = -6250336;
   private static final int BACKGROUND_COLOR = 1426063360;
   private static final int PADDING = 3;
   private static final int BORDER = 1;

   public AccessibilityOnboardingTextWidget(Font pFont, Component pMessage, int pMaxWidth) {
      super(pMessage, pFont);
      this.setMaxWidth(pMaxWidth);
      this.setCentered(true);
      this.active = true;
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = this.getX() - 3;
      int j = this.getY() - 3;
      int k = this.getX() + this.getWidth() + 3;
      int l = this.getY() + this.getHeight() + 3;
      int i1 = this.isFocused() ? -1 : -6250336;
      pGuiGraphics.fill(i - 1, j - 1, i, l + 1, i1);
      pGuiGraphics.fill(k, j - 1, k + 1, l + 1, i1);
      pGuiGraphics.fill(i, j, k, j - 1, i1);
      pGuiGraphics.fill(i, l, k, l + 1, i1);
      pGuiGraphics.fill(i, j, k, l, 1426063360);
      super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public void playDownSound(SoundManager pHandler) {
   }
}