package net.minecraft.world.level;

public class GrassColor {
   private static int[] pixels = new int[65536];

   public static void init(int[] pGrassBuffer) {
      pixels = pGrassBuffer;
   }

   public static int get(double pTemperature, double pHumidity) {
      pHumidity *= pTemperature;
      int i = (int)((1.0D - pTemperature) * 255.0D);
      int j = (int)((1.0D - pHumidity) * 255.0D);
      int k = j << 8 | i;
      return k >= pixels.length ? -65281 : pixels[k];
   }

   public static int getDefaultColor() {
      return get(0.5D, 1.0D);
   }
}