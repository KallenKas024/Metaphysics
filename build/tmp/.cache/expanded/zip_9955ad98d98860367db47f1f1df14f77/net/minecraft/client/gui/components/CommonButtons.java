package net.minecraft.client.gui.components;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommonButtons {
   public static TextAndImageButton languageTextAndImage(Button.OnPress pOnPress) {
      return TextAndImageButton.builder(Component.translatable("options.language"), Button.WIDGETS_LOCATION, pOnPress).texStart(3, 109).offset(65, 3).yDiffTex(20).usedTextureSize(14, 14).textureSize(256, 256).build();
   }

   public static TextAndImageButton accessibilityTextAndImage(Button.OnPress pOnPress) {
      return TextAndImageButton.builder(Component.translatable("options.accessibility.title"), Button.ACCESSIBILITY_TEXTURE, pOnPress).texStart(3, 2).offset(65, 2).yDiffTex(20).usedTextureSize(14, 16).textureSize(32, 64).build();
   }
}