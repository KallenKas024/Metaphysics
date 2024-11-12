package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HuskRenderer extends ZombieRenderer {
   private static final ResourceLocation HUSK_LOCATION = new ResourceLocation("textures/entity/zombie/husk.png");

   public HuskRenderer(EntityRendererProvider.Context p_174180_) {
      super(p_174180_, ModelLayers.HUSK, ModelLayers.HUSK_INNER_ARMOR, ModelLayers.HUSK_OUTER_ARMOR);
   }

   protected void scale(Zombie pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      float f = 1.0625F;
      pPoseStack.scale(1.0625F, 1.0625F, 1.0625F);
      super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Zombie pEntity) {
      return HUSK_LOCATION;
   }
}