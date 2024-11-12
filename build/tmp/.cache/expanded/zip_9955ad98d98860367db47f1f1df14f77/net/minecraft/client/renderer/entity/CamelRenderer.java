package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CamelRenderer extends MobRenderer<Camel, CamelModel<Camel>> {
   private static final ResourceLocation CAMEL_LOCATION = new ResourceLocation("textures/entity/camel/camel.png");

   public CamelRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pLayerLocation) {
      super(pContext, new CamelModel<>(pContext.bakeLayer(pLayerLocation)), 0.7F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Camel pEntity) {
      return CAMEL_LOCATION;
   }
}