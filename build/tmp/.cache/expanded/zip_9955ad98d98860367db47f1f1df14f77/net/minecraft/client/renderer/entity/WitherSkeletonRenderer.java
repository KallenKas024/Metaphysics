package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherSkeletonRenderer extends SkeletonRenderer {
   private static final ResourceLocation WITHER_SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

   public WitherSkeletonRenderer(EntityRendererProvider.Context p_174447_) {
      super(p_174447_, ModelLayers.WITHER_SKELETON, ModelLayers.WITHER_SKELETON_INNER_ARMOR, ModelLayers.WITHER_SKELETON_OUTER_ARMOR);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(AbstractSkeleton pEntity) {
      return WITHER_SKELETON_LOCATION;
   }

   protected void scale(AbstractSkeleton pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      pPoseStack.scale(1.2F, 1.2F, 1.2F);
   }
}