package net.minecraft.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PanoramaRenderer {
   private final Minecraft minecraft;
   private final CubeMap cubeMap;
   private float spin;
   private float bob;

   public PanoramaRenderer(CubeMap pCubeMap) {
      this.cubeMap = pCubeMap;
      this.minecraft = Minecraft.getInstance();
   }

   public void render(float pDeltaT, float pAlpha) {
      float f = (float)((double)pDeltaT * this.minecraft.options.panoramaSpeed().get());
      this.spin = wrap(this.spin + f * 0.1F, 360.0F);
      this.bob = wrap(this.bob + f * 0.001F, ((float)Math.PI * 2F));
      this.cubeMap.render(this.minecraft, 10.0F, -this.spin, pAlpha);
   }

   private static float wrap(float pValue, float pMax) {
      return pValue > pMax ? pValue - pMax : pValue;
   }
}