package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>> extends MobRenderer<T, M> {
   public HumanoidMobRenderer(EntityRendererProvider.Context pContext, M pModel, float pShadowRadius) {
      this(pContext, pModel, pShadowRadius, 1.0F, 1.0F, 1.0F);
   }

   public HumanoidMobRenderer(EntityRendererProvider.Context pContext, M pModel, float pShadowRadius, float pScaleX, float pScaleY, float pScaleZ) {
      super(pContext, pModel, pShadowRadius);
      this.addLayer(new CustomHeadLayer<>(this, pContext.getModelSet(), pScaleX, pScaleY, pScaleZ, pContext.getItemInHandRenderer()));
      this.addLayer(new ElytraLayer<>(this, pContext.getModelSet()));
      this.addLayer(new ItemInHandLayer<>(this, pContext.getItemInHandRenderer()));
   }
}