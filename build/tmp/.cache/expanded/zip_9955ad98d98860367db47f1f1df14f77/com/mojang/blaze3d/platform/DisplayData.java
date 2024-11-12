package com.mojang.blaze3d.platform;

import java.util.OptionalInt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DisplayData {
   public final int width;
   public final int height;
   public final OptionalInt fullscreenWidth;
   public final OptionalInt fullscreenHeight;
   public final boolean isFullscreen;

   public DisplayData(int pWidth, int pHeight, OptionalInt pFullscreenWidth, OptionalInt pFullscreenHeight, boolean pIsFullscreen) {
      this.width = pWidth;
      this.height = pHeight;
      this.fullscreenWidth = pFullscreenWidth;
      this.fullscreenHeight = pFullscreenHeight;
      this.isFullscreen = pIsFullscreen;
   }
}