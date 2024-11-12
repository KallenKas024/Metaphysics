package net.minecraft.client.model;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelUtils {
   public static float rotlerpRad(float pMin, float pMax, float pDelta) {
      float f;
      for(f = pMax - pMin; f < -(float)Math.PI; f += ((float)Math.PI * 2F)) {
      }

      while(f >= (float)Math.PI) {
         f -= ((float)Math.PI * 2F);
      }

      return pMin + pDelta * f;
   }
}