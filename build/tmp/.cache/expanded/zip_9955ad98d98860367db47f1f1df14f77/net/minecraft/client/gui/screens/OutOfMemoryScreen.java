package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutOfMemoryScreen extends Screen {
   private MultiLineLabel message = MultiLineLabel.EMPTY;

   public OutOfMemoryScreen() {
      super(Component.translatable("outOfMemory.title"));
   }

   protected void init() {
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_TO_TITLE, (p_280810_) -> {
         this.minecraft.setScreen(new TitleScreen());
      }).bounds(this.width / 2 - 155, this.height / 4 + 120 + 12, 150, 20).build());
      this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (p_280811_) -> {
         this.minecraft.stop();
      }).bounds(this.width / 2 - 155 + 160, this.height / 4 + 120 + 12, 150, 20).build());
      this.message = MultiLineLabel.create(this.font, Component.translatable("outOfMemory.message"), 295);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 60 + 20, 16777215);
      this.message.renderLeftAligned(pGuiGraphics, this.width / 2 - 145, this.height / 4, 9, 10526880);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}