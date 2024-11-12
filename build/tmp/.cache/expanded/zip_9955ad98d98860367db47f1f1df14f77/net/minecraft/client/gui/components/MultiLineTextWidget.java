package net.minecraft.client.gui.components;

import java.util.OptionalInt;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SingleKeyCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiLineTextWidget extends AbstractStringWidget {
   private OptionalInt maxWidth = OptionalInt.empty();
   private OptionalInt maxRows = OptionalInt.empty();
   private final SingleKeyCache<MultiLineTextWidget.CacheKey, MultiLineLabel> cache;
   private boolean centered = false;

   public MultiLineTextWidget(Component pMessage, Font pFont) {
      this(0, 0, pMessage, pFont);
   }

   public MultiLineTextWidget(int pX, int pY, Component pMessage, Font pFont) {
      super(pX, pY, 0, 0, pMessage, pFont);
      this.cache = Util.singleKeyCache((p_270516_) -> {
         return p_270516_.maxRows.isPresent() ? MultiLineLabel.create(pFont, p_270516_.message, p_270516_.maxWidth, p_270516_.maxRows.getAsInt()) : MultiLineLabel.create(pFont, p_270516_.message, p_270516_.maxWidth);
      });
      this.active = false;
   }

   public MultiLineTextWidget setColor(int pColor) {
      super.setColor(pColor);
      return this;
   }

   public MultiLineTextWidget setMaxWidth(int pMaxWidth) {
      this.maxWidth = OptionalInt.of(pMaxWidth);
      return this;
   }

   public MultiLineTextWidget setMaxRows(int pMaxRows) {
      this.maxRows = OptionalInt.of(pMaxRows);
      return this;
   }

   public MultiLineTextWidget setCentered(boolean pCentered) {
      this.centered = pCentered;
      return this;
   }

   public int getWidth() {
      return this.cache.getValue(this.getFreshCacheKey()).getWidth();
   }

   public int getHeight() {
      return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * 9;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      MultiLineLabel multilinelabel = this.cache.getValue(this.getFreshCacheKey());
      int i = this.getX();
      int j = this.getY();
      int k = 9;
      int l = this.getColor();
      if (this.centered) {
         multilinelabel.renderCentered(pGuiGraphics, i + this.getWidth() / 2, j, k, l);
      } else {
         multilinelabel.renderLeftAligned(pGuiGraphics, i, j, k, l);
      }

   }

   private MultiLineTextWidget.CacheKey getFreshCacheKey() {
      return new MultiLineTextWidget.CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
   }

   @OnlyIn(Dist.CLIENT)
   static record CacheKey(Component message, int maxWidth, OptionalInt maxRows) {
   }
}