package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RenderLayer<T extends Entity, M extends EntityModel<T>> {
   private final RenderLayerParent<T, M> renderer;

   public RenderLayer(RenderLayerParent<T, M> pRenderer) {
      this.renderer = pRenderer;
   }

   protected static <T extends LivingEntity> void coloredCutoutModelCopyLayerRender(EntityModel<T> pModelParent, EntityModel<T> pModel, ResourceLocation pTextureLocation, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, float pPartialTicks, float pRed, float pGreen, float pBlue) {
      if (!pEntity.isInvisible()) {
         pModelParent.copyPropertiesTo(pModel);
         pModel.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
         pModel.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
         renderColoredCutoutModel(pModel, pTextureLocation, pPoseStack, pBuffer, pPackedLight, pEntity, pRed, pGreen, pBlue);
      }

   }

   protected static <T extends LivingEntity> void renderColoredCutoutModel(EntityModel<T> pModel, ResourceLocation pTextureLocation, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pEntity, float pRed, float pGreen, float pBlue) {
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(pTextureLocation));
      pModel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pEntity, 0.0F), pRed, pGreen, pBlue, 1.0F);
   }

   public M getParentModel() {
      return this.renderer.getModel();
   }

   protected ResourceLocation getTextureLocation(T pEntity) {
      return this.renderer.getTextureLocation(pEntity);
   }

   public abstract void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch);
}