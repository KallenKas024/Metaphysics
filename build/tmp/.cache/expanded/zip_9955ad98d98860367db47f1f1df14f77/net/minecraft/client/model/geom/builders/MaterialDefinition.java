package net.minecraft.client.model.geom.builders;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MaterialDefinition {
   final int xTexSize;
   final int yTexSize;

   public MaterialDefinition(int pXTexSize, int pYTexSize) {
      this.xTexSize = pXTexSize;
      this.yTexSize = pYTexSize;
   }
}