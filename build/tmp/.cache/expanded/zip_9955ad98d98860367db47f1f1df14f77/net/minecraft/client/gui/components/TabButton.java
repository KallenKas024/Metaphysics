package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabButton extends AbstractWidget {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/tab_button.png");
   private static final int TEXTURE_WIDTH = 130;
   private static final int TEXTURE_HEIGHT = 24;
   private static final int TEXTURE_BORDER = 2;
   private static final int TEXTURE_BORDER_BOTTOM = 0;
   private static final int SELECTED_OFFSET = 3;
   private static final int TEXT_MARGIN = 1;
   private static final int UNDERLINE_HEIGHT = 1;
   private static final int UNDERLINE_MARGIN_X = 4;
   private static final int UNDERLINE_MARGIN_BOTTOM = 2;
   private final TabManager tabManager;
   private final Tab tab;

   public TabButton(TabManager pTabManager, Tab pTab, int pWidth, int pHeight) {
      super(0, 0, pWidth, pHeight, pTab.getTabTitle());
      this.tabManager = pTabManager;
      this.tab = pTab;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      pGuiGraphics.blitNineSliced(TEXTURE_LOCATION, this.getX(), this.getY(), this.width, this.height, 2, 2, 2, 0, 130, 24, 0, this.getTextureY());
      Font font = Minecraft.getInstance().font;
      int i = this.active ? -1 : -6250336;
      this.renderString(pGuiGraphics, font, i);
      if (this.isSelected()) {
         this.renderFocusUnderline(pGuiGraphics, font, i);
      }

   }

   public void renderString(GuiGraphics pGuiGraphics, Font pFont, int pColor) {
      int i = this.getX() + 1;
      int j = this.getY() + (this.isSelected() ? 0 : 3);
      int k = this.getX() + this.getWidth() - 1;
      int l = this.getY() + this.getHeight();
      renderScrollingString(pGuiGraphics, pFont, this.getMessage(), i, j, k, l, pColor);
   }

   private void renderFocusUnderline(GuiGraphics pGuiGraphics, Font pFont, int pColor) {
      int i = Math.min(pFont.width(this.getMessage()), this.getWidth() - 4);
      int j = this.getX() + (this.getWidth() - i) / 2;
      int k = this.getY() + this.getHeight() - 2;
      pGuiGraphics.fill(j, k, j + i, k + 1, pColor);
   }

   protected int getTextureY() {
      int i = 2;
      if (this.isSelected() && this.isHoveredOrFocused()) {
         i = 1;
      } else if (this.isSelected()) {
         i = 0;
      } else if (this.isHoveredOrFocused()) {
         i = 3;
      }

      return i * 24;
   }

   protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
      pNarrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("gui.narrate.tab", this.tab.getTabTitle()));
   }

   public void playDownSound(SoundManager pHandler) {
   }

   public Tab tab() {
      return this.tab;
   }

   public boolean isSelected() {
      return this.tabManager.getCurrentTab() == this.tab;
   }
}