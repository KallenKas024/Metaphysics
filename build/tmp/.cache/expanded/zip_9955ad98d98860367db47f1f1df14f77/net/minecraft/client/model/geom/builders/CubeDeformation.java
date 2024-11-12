package net.minecraft.client.model.geom.builders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CubeDeformation {
   public static final CubeDeformation NONE = new CubeDeformation(0.0F);
   final float growX;
   final float growY;
   final float growZ;

   public CubeDeformation(float pGrowX, float pGrowY, float pGrowZ) {
      this.growX = pGrowX;
      this.growY = pGrowY;
      this.growZ = pGrowZ;
   }

   public CubeDeformation(float pGrow) {
      this(pGrow, pGrow, pGrow);
   }

   public CubeDeformation extend(float pGrow) {
      return new CubeDeformation(this.growX + pGrow, this.growY + pGrow, this.growZ + pGrow);
   }

   public CubeDeformation extend(float pGrowX, float pGrowY, float pGrowZ) {
      return new CubeDeformation(this.growX + pGrowX, this.growY + pGrowY, this.growZ + pGrowZ);
   }
}