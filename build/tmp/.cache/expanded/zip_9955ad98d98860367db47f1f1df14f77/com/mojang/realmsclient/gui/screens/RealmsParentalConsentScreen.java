package com.mojang.realmsclient.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
   private static final Component MESSAGE = Component.translatable("mco.account.privacyinfo");
   private final Screen nextScreen;
   private MultiLineLabel messageLines = MultiLineLabel.EMPTY;

   public RealmsParentalConsentScreen(Screen pNextScreen) {
      super(GameNarrator.NO_TITLE);
      this.nextScreen = pNextScreen;
   }

   public void init() {
      Component component = Component.translatable("mco.account.update");
      Component component1 = CommonComponents.GUI_BACK;
      int i = Math.max(this.font.width(component), this.font.width(component1)) + 30;
      Component component2 = Component.translatable("mco.account.privacy.info");
      int j = (int)((double)this.font.width(component2) * 1.2D);
      this.addRenderableWidget(Button.builder(component2, (p_88873_) -> {
         Util.getPlatform().openUri("https://aka.ms/MinecraftGDPR");
      }).bounds(this.width / 2 - j / 2, row(11), j, 20).build());
      this.addRenderableWidget(Button.builder(component, (p_88871_) -> {
         Util.getPlatform().openUri("https://aka.ms/UpdateMojangAccount");
      }).bounds(this.width / 2 - (i + 5), row(13), i, 20).build());
      this.addRenderableWidget(Button.builder(component1, (p_280730_) -> {
         this.minecraft.setScreen(this.nextScreen);
      }).bounds(this.width / 2 + 5, row(13), i, 20).build());
      this.messageLines = MultiLineLabel.create(this.font, MESSAGE, (int)Math.round((double)this.width * 0.9D));
   }

   public Component getNarrationMessage() {
      return MESSAGE;
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
      this.messageLines.renderCentered(pGuiGraphics, this.width / 2, 15, 15, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}