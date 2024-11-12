package net.minecraft.realms;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLabel implements Renderable {
   private final Component text;
   private final int x;
   private final int y;
   private final int color;

   public RealmsLabel(Component pText, int pX, int pY, int pColor) {
      this.text = pText;
      this.x = pX;
      this.y = pY;
      this.color = pColor;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      pGuiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, this.x, this.y, this.color);
   }

   public Component getText() {
      return this.text;
   }
}