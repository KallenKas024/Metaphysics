package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageWidget extends AbstractWidget {
   private final ResourceLocation imageLocation;

   public ImageWidget(int pWidth, int pHeight, ResourceLocation pImageLocation) {
      this(0, 0, pWidth, pHeight, pImageLocation);
   }

   public ImageWidget(int pX, int pY, int pWidth, int pHeight, ResourceLocation pImageLocation) {
      super(pX, pY, pWidth, pHeight, Component.empty());
      this.imageLocation = pImageLocation;
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      int i = this.getWidth();
      int j = this.getHeight();
      pGuiGraphics.blit(this.imageLocation, this.getX(), this.getY(), 0.0F, 0.0F, i, j, i, j);
   }
}