package net.minecraft.client.gui.screens;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OptionsSubScreen extends Screen {
   protected final Screen lastScreen;
   protected final Options options;

   public OptionsSubScreen(Screen pLastScreen, Options pOptions, Component pTitle) {
      super(pTitle);
      this.lastScreen = pLastScreen;
      this.options = pOptions;
   }

   public void removed() {
      this.minecraft.options.save();
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   protected void basicListRender(GuiGraphics pGuiGraphics, OptionsList pOptionsList, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderBackground(pGuiGraphics);
      pOptionsList.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      pGuiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
      super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
   }
}