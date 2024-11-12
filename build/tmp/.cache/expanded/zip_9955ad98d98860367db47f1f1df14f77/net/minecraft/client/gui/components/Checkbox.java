package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractButton {
   private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
   private static final int TEXT_COLOR = 14737632;
   private boolean selected;
   private final boolean showLabel;

   public Checkbox(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected) {
      this(pX, pY, pWidth, pHeight, pMessage, pSelected, true);
   }

   public Checkbox(int pX, int pY, int pWidth, int pHeight, Component pMessage, boolean pSelected, boolean pShowLabel) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.selected = pSelected;
      this.showLabel = pShowLabel;
   }

   public void onPress() {
      this.selected = !this.selected;
   }

   public boolean selected() {
      return this.selected;
   }

   public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
      if (this.active) {
         if (this.isFocused()) {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
         } else {
            pNarrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
         }
      }

   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      Minecraft minecraft = Minecraft.getInstance();
      RenderSystem.enableDepthTest();
      Font font = minecraft.font;
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
      RenderSystem.enableBlend();
      pGuiGraphics.blit(TEXTURE, this.getX(), this.getY(), this.isFocused() ? 20.0F : 0.0F, this.selected ? 20.0F : 0.0F, 20, this.height, 64, 64);
      pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
      if (this.showLabel) {
         pGuiGraphics.drawString(font, this.getMessage(), this.getX() + 24, this.getY() + (this.height - 8) / 2, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
      }

   }
}