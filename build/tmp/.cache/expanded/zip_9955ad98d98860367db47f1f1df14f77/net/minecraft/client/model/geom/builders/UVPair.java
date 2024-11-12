package net.minecraft.client.model.geom.builders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UVPair {
   private final float u;
   private final float v;

   public UVPair(float pU, float pV) {
      this.u = pU;
      this.v = pV;
   }

   public float u() {
      return this.u;
   }

   public float v() {
      return this.v;
   }

   public String toString() {
      return "(" + this.u + "," + this.v + ")";
   }
}