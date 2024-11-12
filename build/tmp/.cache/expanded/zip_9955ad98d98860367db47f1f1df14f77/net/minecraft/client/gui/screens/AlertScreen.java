package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AlertScreen extends Screen {
   private static final int LABEL_Y = 90;
   private final Component messageText;
   private MultiLineLabel message = MultiLineLabel.EMPTY;
   private final Runnable callback;
   private final Component okButton;
   private final boolean shouldCloseOnEsc;

   public AlertScreen(Runnable pCallback, Component pTitle, Component pText) {
      this(pCallback, pTitle, pText, CommonComponents.GUI_BACK, true);
   }

   public AlertScreen(Runnable pCallback, Component pTitle, Component pMessageText, Component pOkButton, boolean pShouldCloseOnEsc) {
      super(pTitle);
      this.callback = pCallback;
      this.messageText = pMessageText;
      this.okButton = pOkButton;
      this.shouldCloseOnEsc = pShouldCloseOnEsc;
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
   }

   protected void init() {
      super.init();
      this.message = MultiLineLabel.create(this.font, this.messageText, this.width - 50);
      int i = this.message.getLineCount() * 9;
      int j = Mth.clamp(90 + i + 12, this.height / 6 + 96, this.height - 24);
      int k = 150;
      this.addRenderableWidget(Button.builder(this.okButton, (p_95533_) -> {
         this.callback.run();
      }).bounds((this.width - 150) / 2, j, 150, 20).build());
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
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
      this.message.renderCentered(pGuiGraphics, this.width / 2, 90);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public boolean shouldCloseOnEsc() {
      return this.shouldCloseOnEsc;
   }
}