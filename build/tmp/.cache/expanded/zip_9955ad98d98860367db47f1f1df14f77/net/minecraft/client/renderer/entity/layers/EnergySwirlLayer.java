package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EnergySwirlLayer<T extends Entity & PowerableMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
   public EnergySwirlLayer(RenderLayerParent<T, M> pRenderer) {
      super(pRenderer);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (pLivingEntity.isPowered()) {
         float f = (float)pLivingEntity.tickCount + pPartialTicks;
         EntityModel<T> entitymodel = this.model();
         entitymodel.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
         this.getParentModel().copyPropertiesTo(entitymodel);
         VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.energySwirl(this.getTextureLocation(), this.xOffset(f) % 1.0F, f * 0.01F % 1.0F));
         entitymodel.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
         entitymodel.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1.0F);
      }
   }

   protected abstract float xOffset(float pTickCount);

   protected abstract ResourceLocation getTextureLocation();

   protected abstract EntityModel<T> model();
}