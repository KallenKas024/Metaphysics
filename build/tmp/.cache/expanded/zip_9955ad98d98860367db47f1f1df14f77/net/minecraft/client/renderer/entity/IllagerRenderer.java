package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class IllagerRenderer<T extends AbstractIllager> extends MobRenderer<T, IllagerModel<T>> {
   protected IllagerRenderer(EntityRendererProvider.Context pContext, IllagerModel<T> pModel, float pShadowRadius) {
      super(pContext, pModel, pShadowRadius);
      this.addLayer(new CustomHeadLayer<>(this, pContext.getModelSet(), pContext.getItemInHandRenderer()));
   }

   protected void scale(T pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      float f = 0.9375F;
      pPoseStack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}