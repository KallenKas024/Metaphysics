package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FittingMultiLineTextWidget extends AbstractScrollWidget {
   private final Font font;
   private final MultiLineTextWidget multilineWidget;

   public FittingMultiLineTextWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font pFont) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.font = pFont;
      this.multilineWidget = (new MultiLineTextWidget(0, 0, pMessage, pFont)).setMaxWidth(this.getWidth() - this.totalInnerPadding());
   }

   public FittingMultiLineTextWidget setColor(int pColor) {
      this.multilineWidget.setColor(pColor);
      return this;
   }

   public void setWidth(int pWidth) {
      super.setWidth(pWidth);
      this.multilineWidget.setMaxWidth(this.getWidth() - this.totalInnerPadding());
   }

   protected int getInnerHeight() {
      return this.multilineWidget.getHeight();
   }

   protected double scrollRate() {
      return 9.0D;
   }

   protected void renderBackground(GuiGraphics pGuiGraphics) {
      if (this.scrollbarVisible()) {
         super.renderBackground(pGuiGraphics);
      } else if (this.isFocused()) {
         this.renderBorder(pGuiGraphics, this.getX() - this.innerPadding(), this.getY() - this.innerPadding(), this.getWidth() + this.totalInnerPadding(), this.getHeight() + this.totalInnerPadding());
      }

   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.visible) {
         if (!this.scrollbarVisible()) {
            this.renderBackground(pGuiGraphics);
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate((float)this.getX(), (float)this.getY(), 0.0F);
            this.multilineWidget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.pose().popPose();
         } else {
            super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
         }

      }
   }

   protected void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((float)(this.getX() + this.innerPadding()), (float)(this.getY() + this.innerPadding()), 0.0F);
      this.multilineWidget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.pose().popPose();
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
   }
}