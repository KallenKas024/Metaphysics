package net.minecraft.client.renderer.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectralArrowRenderer extends ArrowRenderer<SpectralArrow> {
   public static final ResourceLocation SPECTRAL_ARROW_LOCATION = new ResourceLocation("textures/entity/projectiles/spectral_arrow.png");

   public SpectralArrowRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(SpectralArrow pEntity) {
      return SPECTRAL_ARROW_LOCATION;
   }
}