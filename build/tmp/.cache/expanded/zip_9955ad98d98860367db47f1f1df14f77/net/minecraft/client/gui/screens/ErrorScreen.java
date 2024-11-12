package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ErrorScreen extends Screen {
   private final Component message;

   public ErrorScreen(Component pTitle, Component pMessage) {
      super(pTitle);
      this.message = pMessage;
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p_280801_) -> {
         this.minecraft.setScreen((Screen)null);
      }).bounds(this.width / 2 - 100, 140, 200, 20).build());
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      pGuiGraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 90, 16777215);
      pGuiGraphics.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}