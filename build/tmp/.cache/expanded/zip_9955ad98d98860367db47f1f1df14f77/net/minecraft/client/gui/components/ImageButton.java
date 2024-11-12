package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
   protected final ResourceLocation resourceLocation;
   protected final int xTexStart;
   protected final int yTexStart;
   protected final int yDiffTex;
   protected final int textureWidth;
   protected final int textureHeight;

   public ImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pHeight, pResourceLocation, 256, 256, pOnPress);
   }

   public ImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, 256, 256, pOnPress);
   }

   public ImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress) {
      this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pYDiffTex, pResourceLocation, pTextureWidth, pTextureHeight, pOnPress, CommonComponents.EMPTY);
   }

   public ImageButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, int pYDiffTex, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, Button.OnPress pOnPress, Component pMessage) {
      super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
      this.textureWidth = pTextureWidth;
      this.textureHeight = pTextureHeight;
      this.xTexStart = pXTexStart;
      this.yTexStart = pYTexStart;
      this.yDiffTex = pYDiffTex;
      this.resourceLocation = pResourceLocation;
   }

   public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      this.renderTexture(pGuiGraphics, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
   }
}