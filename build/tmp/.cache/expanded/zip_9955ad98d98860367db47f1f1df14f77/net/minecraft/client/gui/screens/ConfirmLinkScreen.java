package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConfirmLinkScreen extends ConfirmScreen {
   private static final Component COPY_BUTTON_TEXT = Component.translatable("chat.copy");
   private static final Component WARNING_TEXT = Component.translatable("chat.link.warning");
   private final String url;
   private final boolean showWarning;

   public ConfirmLinkScreen(BooleanConsumer pCallback, String pUrl, boolean pTrusted) {
      this(pCallback, confirmMessage(pTrusted), Component.literal(pUrl), pUrl, pTrusted ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, pTrusted);
   }

   public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, String pUrl, boolean pTrusted) {
      this(pCallback, pTitle, pUrl, pTrusted ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, pTrusted);
   }

   public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, String pUrl, Component pNoButton, boolean pTrusted) {
      this(pCallback, pTitle, confirmMessage(pTrusted, pUrl), pUrl, pNoButton, pTrusted);
   }

   public ConfirmLinkScreen(BooleanConsumer pCallback, Component pTitle, Component pMessage, String pUrl, Component pNoButton, boolean pTrusted) {
      super(pCallback, pTitle, pMessage);
      this.yesButton = (Component)(pTrusted ? Component.translatable("chat.link.open") : CommonComponents.GUI_YES);
      this.noButton = pNoButton;
      this.showWarning = !pTrusted;
      this.url = pUrl;
   }

   protected static MutableComponent confirmMessage(boolean pTrusted, String pExtraInfo) {
      return confirmMessage(pTrusted).append(CommonComponents.SPACE).append(Component.literal(pExtraInfo));
   }

   protected static MutableComponent confirmMessage(boolean pTrusted) {
      return Component.translatable(pTrusted ? "chat.link.confirmTrusted" : "chat.link.confirm");
   }

   protected void addButtons(int pY) {
      this.addRenderableWidget(Button.builder(this.yesButton, (p_169249_) -> {
         this.callback.accept(true);
      }).bounds(this.width / 2 - 50 - 105, pY, 100, 20).build());
      this.addRenderableWidget(Button.builder(COPY_BUTTON_TEXT, (p_169247_) -> {
         this.copyToClipboard();
         this.callback.accept(false);
      }).bounds(this.width / 2 - 50, pY, 100, 20).build());
      this.addRenderableWidget(Button.builder(this.noButton, (p_169245_) -> {
         this.callback.accept(false);
      }).bounds(this.width / 2 - 50 + 105, pY, 100, 20).build());
   }

   /**
    * Copies the link to the system clipboard.
    */
   public void copyToClipboard() {
      this.minecraft.keyboardHandler.setClipboard(this.url);
   }

   /**
    * Renders the graphical user interface (GUI) element.
    * @param pGuiGraphics the GuiGraphics object used for rendering.
    * @param pMouseX the x-coordinate of the mouse cursor.
    * @param pMouseY the y-coordinate of the mouse cursor.
    * @param pPartialTick the partial tick time.
    */
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      if (this.showWarning) {
         pGuiGraphics.drawCenteredString(this.font, WARNING_TEXT, this.width / 2, 110, 16764108);
      }

   }

   public static void confirmLinkNow(String pUrl, Screen pLastScreen, boolean pTrusted) {
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.setScreen(new ConfirmLinkScreen((p_274671_) -> {
         if (p_274671_) {
            Util.getPlatform().openUri(pUrl);
         }

         minecraft.setScreen(pLastScreen);
      }, pUrl, pTrusted));
   }

   public static Button.OnPress confirmLink(String pUrl, Screen pLastScreen, boolean pTrusted) {
      return (p_274667_) -> {
         confirmLinkNow(pUrl, pLastScreen, pTrusted);
      };
   }
}