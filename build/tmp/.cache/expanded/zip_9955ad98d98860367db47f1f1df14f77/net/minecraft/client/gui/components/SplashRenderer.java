package net.minecraft.client.gui.components;

import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SplashRenderer {
   public static final SplashRenderer CHRISTMAS = new SplashRenderer("Merry X-mas!");
   public static final SplashRenderer NEW_YEAR = new SplashRenderer("Happy new year!");
   public static final SplashRenderer HALLOWEEN = new SplashRenderer("OOoooOOOoooo! Spooky!");
   private static final int WIDTH_OFFSET = 123;
   private static final int HEIGH_OFFSET = 69;
   private final String splash;

   public SplashRenderer(String pSplash) {
      this.splash = pSplash;
   }

   public void render(GuiGraphics pGuiGraphics, int pScreenWidth, Font pFont, int pColor) {
      pGuiGraphics.pose().pushPose();
      pGuiGraphics.pose().translate((float)pScreenWidth / 2.0F + 123.0F, 69.0F, 0.0F);
      pGuiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
      float f = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) * 0.1F);
      f = f * 100.0F / (float)(pFont.width(this.splash) + 32);
      pGuiGraphics.pose().scale(f, f, f);
      pGuiGraphics.drawCenteredString(pFont, this.splash, 0, -8, 16776960 | pColor);
      pGuiGraphics.pose().popPose();
   }
}