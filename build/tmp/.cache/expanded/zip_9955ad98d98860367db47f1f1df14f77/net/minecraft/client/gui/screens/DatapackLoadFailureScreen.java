package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DatapackLoadFailureScreen extends Screen {
   private MultiLineLabel message = MultiLineLabel.EMPTY;
   private final Runnable callback;

   public DatapackLoadFailureScreen(Runnable pCallback) {
      super(Component.translatable("datapackFailure.title"));
      this.callback = pCallback;
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.getTitle(), this.width - 50);
      this.addRenderableWidget(Button.builder(Component.translatable("datapackFailure.safeMode"), (p_95905_) -> {
         this.callback.run();
      }).bounds(this.width / 2 - 155, this.height / 6 + 96, 150, 20).build());
      this.addRenderableWidget(Button.builder(CommonComponents.GUI_TO_TITLE, (p_280793_) -> {
         this.minecraft.setScreen((Screen)null);
      }).bounds(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20).build());
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
      this.message.renderCentered(pGuiGraphics, this.width / 2, 70);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }
}