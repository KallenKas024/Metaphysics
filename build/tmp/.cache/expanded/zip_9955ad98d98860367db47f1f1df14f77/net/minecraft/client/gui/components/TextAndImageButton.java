package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextAndImageButton extends Button {
   protected final ResourceLocation resourceLocation;
   protected final int xTexStart;
   protected final int yTexStart;
   protected final int yDiffTex;
   protected final int textureWidth;
   protected final int textureHeight;
   private final int xOffset;
   private final int yOffset;
   private final int usedTextureWidth;
   private final int usedTextureHeight;

   TextAndImageButton(Component pMessage, int pXTexStart, int pYTexStart, int pXOffset, int pYOffset, int pYDiffTex, int pUsedTextureWidth, int pUsedTextureHeight, int pTextureWidth, int pTextureHeight, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
      super(0, 0, 150, 20, pMessage, pOnPress, DEFAULT_NARRATION);
      this.textureWidth = pTextureWidth;
      this.textureHeight = pTextureHeight;
      this.xTexStart = pXTexStart;
      this.yTexStart = pYTexStart;
      this.yDiffTex = pYDiffTex;
      this.resourceLocation = pResourceLocation;
      this.xOffset = pXOffset;
      this.yOffset = pYOffset;
      this.usedTextureWidth = pUsedTextureWidth;
      this.usedTextureHeight = pUsedTextureHeight;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
      this.renderTexture(pGuiGraphics, this.resourceLocation, this.getXOffset(), this.getYOffset(), this.xTexStart, this.yTexStart, this.yDiffTex, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight);
   }

   public void renderString(GuiGraphics pGuiGraphics, Font pFont, int pColor) {
      int i = this.getX() + 2;
      int j = this.getX() + this.getWidth() - this.usedTextureWidth - 6;
      renderScrollingString(pGuiGraphics, pFont, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), pColor);
   }

   private int getXOffset() {
      return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
   }

   private int getYOffset() {
      return this.getY() + this.yOffset;
   }

   public static TextAndImageButton.Builder builder(Component pMessage, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
      return new TextAndImageButton.Builder(pMessage, pResourceLocation, pOnPress);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Builder {
      private final Component message;
      private final ResourceLocation resourceLocation;
      private final Button.OnPress onPress;
      private int xTexStart;
      private int yTexStart;
      private int yDiffTex;
      private int usedTextureWidth;
      private int usedTextureHeight;
      private int textureWidth;
      private int textureHeight;
      private int xOffset;
      private int yOffset;

      public Builder(Component pMessage, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
         this.message = pMessage;
         this.resourceLocation = pResourceLocation;
         this.onPress = pOnPress;
      }

      public TextAndImageButton.Builder texStart(int pX, int pY) {
         this.xTexStart = pX;
         this.yTexStart = pY;
         return this;
      }

      public TextAndImageButton.Builder offset(int pX, int pY) {
         this.xOffset = pX;
         this.yOffset = pY;
         return this;
      }

      public TextAndImageButton.Builder yDiffTex(int pYDiffTex) {
         this.yDiffTex = pYDiffTex;
         return this;
      }

      public TextAndImageButton.Builder usedTextureSize(int pWidth, int pHeight) {
         this.usedTextureWidth = pWidth;
         this.usedTextureHeight = pHeight;
         return this;
      }

      public TextAndImageButton.Builder textureSize(int pWidth, int pHeight) {
         this.textureWidth = pWidth;
         this.textureHeight = pHeight;
         return this;
      }

      public TextAndImageButton build() {
         return new TextAndImageButton(this.message, this.xTexStart, this.yTexStart, this.xOffset, this.yOffset, this.yDiffTex, this.usedTextureWidth, this.usedTextureHeight, this.textureWidth, this.textureHeight, this.resourceLocation, this.onPress);
      }
   }
}