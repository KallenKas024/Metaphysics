package net.minecraft.util;

public class FastColor {
   public static class ABGR32 {
      public static int alpha(int pPackedColor) {
         return pPackedColor >>> 24;
      }

      public static int red(int pPackedColor) {
         return pPackedColor & 255;
      }

      public static int green(int pPackedColor) {
         return pPackedColor >> 8 & 255;
      }

      public static int blue(int pPackedColor) {
         return pPackedColor >> 16 & 255;
      }

      public static int transparent(int pPackedColor) {
         return pPackedColor & 16777215;
      }

      public static int opaque(int pPackedColor) {
         return pPackedColor | -16777216;
      }

      public static int color(int pAlpha, int pBlue, int pGreen, int pRed) {
         return pAlpha << 24 | pBlue << 16 | pGreen << 8 | pRed;
      }

      public static int color(int pAlpha, int pPackedColor) {
         return pAlpha << 24 | pPackedColor & 16777215;
      }
   }

   public static class ARGB32 {
      public static int alpha(int pPackedColor) {
         return pPackedColor >>> 24;
      }

      public static int red(int pPackedColor) {
         return pPackedColor >> 16 & 255;
      }

      public static int green(int pPackedColor) {
         return pPackedColor >> 8 & 255;
      }

      public static int blue(int pPackedColor) {
         return pPackedColor & 255;
      }

      public static int color(int pAlpha, int pRed, int pGreen, int pBlue) {
         return pAlpha << 24 | pRed << 16 | pGreen << 8 | pBlue;
      }

      public static int multiply(int pPackedColourOne, int pPackedColorTwo) {
         return color(alpha(pPackedColourOne) * alpha(pPackedColorTwo) / 255, red(pPackedColourOne) * red(pPackedColorTwo) / 255, green(pPackedColourOne) * green(pPackedColorTwo) / 255, blue(pPackedColourOne) * blue(pPackedColorTwo) / 255);
      }

      public static int lerp(float pDelta, int pMin, int pMax) {
         int i = Mth.lerpInt(pDelta, alpha(pMin), alpha(pMax));
         int j = Mth.lerpInt(pDelta, red(pMin), red(pMax));
         int k = Mth.lerpInt(pDelta, green(pMin), green(pMax));
         int l = Mth.lerpInt(pDelta, blue(pMin), blue(pMax));
         return color(i, j, k, l);
      }
   }
}