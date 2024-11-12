package net.minecraft.client.model.geom;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ModelLayerLocation {
   private final ResourceLocation model;
   private final String layer;

   public ModelLayerLocation(ResourceLocation pModel, String pLayer) {
      this.model = pModel;
      this.layer = pLayer;
   }

   public ResourceLocation getModel() {
      return this.model;
   }

   public String getLayer() {
      return this.layer;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof ModelLayerLocation)) {
         return false;
      } else {
         ModelLayerLocation modellayerlocation = (ModelLayerLocation)pOther;
         return this.model.equals(modellayerlocation.model) && this.layer.equals(modellayerlocation.layer);
      }
   }

   public int hashCode() {
      int i = this.model.hashCode();
      return 31 * i + this.layer.hashCode();
   }

   public String toString() {
      return this.model + "#" + this.layer;
   }
}