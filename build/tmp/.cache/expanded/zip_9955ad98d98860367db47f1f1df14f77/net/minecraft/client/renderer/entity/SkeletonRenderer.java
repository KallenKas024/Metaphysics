package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonRenderer extends HumanoidMobRenderer<AbstractSkeleton, SkeletonModel<AbstractSkeleton>> {
   private static final ResourceLocation SKELETON_LOCATION = new ResourceLocation("textures/entity/skeleton/skeleton.png");

   public SkeletonRenderer(EntityRendererProvider.Context p_174380_) {
      this(p_174380_, ModelLayers.SKELETON, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
   }

   public SkeletonRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pSkeletonLayer, ModelLayerLocation pInnerModelLayer, ModelLayerLocation pOuterModelLayer) {
      super(pContext, new SkeletonModel<>(pContext.bakeLayer(pSkeletonLayer)), 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, new SkeletonModel(pContext.bakeLayer(pInnerModelLayer)), new SkeletonModel(pContext.bakeLayer(pOuterModelLayer)), pContext.getModelManager()));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(AbstractSkeleton pEntity) {
      return SKELETON_LOCATION;
   }

   protected boolean isShaking(AbstractSkeleton pEntity) {
      return pEntity.isShaking();
   }
}