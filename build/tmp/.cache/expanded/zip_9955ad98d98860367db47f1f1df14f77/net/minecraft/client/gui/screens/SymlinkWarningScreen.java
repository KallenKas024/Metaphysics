package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SymlinkWarningScreen extends Screen {
   private static final Component TITLE = Component.translatable("symlink_warning.title").withStyle(ChatFormatting.BOLD);
   private static final Component MESSAGE_TEXT = Component.translatable("symlink_warning.message", "https://aka.ms/MinecraftSymLinks");
   @Nullable
   private final Screen callbackScreen;
   private final GridLayout layout = (new GridLayout()).rowSpacing(10);

   public SymlinkWarningScreen(@Nullable Screen pCallbackScreen) {
      super(TITLE);
      this.callbackScreen = pCallbackScreen;
   }

   protected void init() {
      super.init();
      this.layout.defaultCellSetting().alignHorizontallyCenter();
      GridLayout.RowHelper gridlayout$rowhelper = this.layout.createRowHelper(1);
      gridlayout$rowhelper.addChild(new StringWidget(this.title, this.font));
      gridlayout$rowhelper.addChild((new MultiLineTextWidget(MESSAGE_TEXT, this.font)).setMaxWidth(this.width - 50).setCentered(true));
      int i = 120;
      GridLayout gridlayout = (new GridLayout()).columnSpacing(5);
      GridLayout.RowHelper gridlayout$rowhelper1 = gridlayout.createRowHelper(3);
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_OPEN_IN_BROWSER, (p_289977_) -> {
         Util.getPlatform().openUri("https://aka.ms/MinecraftSymLinks");
      }).size(120, 20).build());
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, (p_289939_) -> {
         this.minecraft.keyboardHandler.setClipboard("https://aka.ms/MinecraftSymLinks");
      }).size(120, 20).build());
      gridlayout$rowhelper1.addChild(Button.builder(CommonComponents.GUI_BACK, (p_289963_) -> {
         this.onClose();
      }).size(120, 20).build());
      gridlayout$rowhelper.addChild(gridlayout);
      this.repositionElements();
      this.layout.visitWidgets(this::addRenderableWidget);
   }

   protected void repositionElements() {
      this.layout.arrangeElements();
      FrameLayout.centerInRectangle(this.layout, this.getRectangle());
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
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }

   public Component getNarrationMessage() {
      return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE_TEXT);
   }

   public void onClose() {
      this.minecraft.setScreen(this.callbackScreen);
   }
}