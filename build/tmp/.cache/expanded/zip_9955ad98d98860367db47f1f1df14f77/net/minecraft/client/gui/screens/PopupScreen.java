package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PopupScreen extends Screen {
   private static final int BUTTON_PADDING = 20;
   private static final int BUTTON_MARGIN = 5;
   private static final int BUTTON_HEIGHT = 20;
   private final Component narrationMessage;
   private final FormattedText message;
   private final ImmutableList<PopupScreen.ButtonOption> buttonOptions;
   private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
   private int contentTop;
   private int buttonWidth;

   protected PopupScreen(Component pTitle, List<Component> pMessage, ImmutableList<PopupScreen.ButtonOption> pButtonOptions) {
      super(pTitle);
      this.message = FormattedText.composite(pMessage);
      this.narrationMessage = CommonComponents.joinForNarration(pTitle, ComponentUtils.formatList(pMessage, CommonComponents.EMPTY));
      this.buttonOptions = pButtonOptions;
   }

   public Component getNarrationMessage() {
      return this.narrationMessage;
   }

   public void init() {
      for(PopupScreen.ButtonOption popupscreen$buttonoption : this.buttonOptions) {
         this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(popupscreen$buttonoption.message) + 20);
      }

      int l = 5 + this.buttonWidth + 5;
      int i1 = l * this.buttonOptions.size();
      this.messageLines = MultiLineLabel.create(this.font, this.message, i1);
      int i = this.messageLines.getLineCount() * 9;
      this.contentTop = (int)((double)this.height / 2.0D - (double)i / 2.0D);
      int j = this.contentTop + i + 9 * 2;
      int k = (int)((double)this.width / 2.0D - (double)i1 / 2.0D);

      for(PopupScreen.ButtonOption popupscreen$buttonoption1 : this.buttonOptions) {
         this.addRenderableWidget(Button.builder(popupscreen$buttonoption1.message, popupscreen$buttonoption1.onPress).bounds(k, j, this.buttonWidth, 20).build());
         k += l;
      }

   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderDirtBackground(pGuiGraphics);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
      this.messageLines.renderCentered(pGuiGraphics, this.width / 2, this.contentTop);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public boolean shouldCloseOnEsc() {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public static final class ButtonOption {
      final Component message;
      final Button.OnPress onPress;

      public ButtonOption(Component pMessage, Button.OnPress pOnPress) {
         this.message = pMessage;
         this.onPress = pOnPress;
      }
   }
}