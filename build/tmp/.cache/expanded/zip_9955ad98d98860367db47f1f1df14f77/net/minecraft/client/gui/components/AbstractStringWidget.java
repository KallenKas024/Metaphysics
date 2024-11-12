package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractStringWidget extends AbstractWidget {
   private final Font font;
   private int color = 16777215;

   public AbstractStringWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage, Font pFont) {
      super(pX, pY, pWidth, pHeight, pMessage);
      this.font = pFont;
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
   }

   public AbstractStringWidget setColor(int pColor) {
      this.color = pColor;
      return this;
   }

   protected final Font getFont() {
      return this.font;
   }

   protected final int getColor() {
      return this.color;
   }
}