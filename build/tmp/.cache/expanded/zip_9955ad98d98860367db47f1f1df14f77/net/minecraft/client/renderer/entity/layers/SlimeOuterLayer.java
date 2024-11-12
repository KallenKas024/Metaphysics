package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlimeOuterLayer<T extends LivingEntity> extends RenderLayer<T, SlimeModel<T>> {
   private final EntityModel<T> model;

   public SlimeOuterLayer(RenderLayerParent<T, SlimeModel<T>> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.model = new SlimeModel<>(pModelSet.bakeLayer(ModelLayers.SLIME_OUTER));
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      Minecraft minecraft = Minecraft.getInstance();
      boolean flag = minecraft.shouldEntityAppearGlowing(pLivingEntity) && pLivingEntity.isInvisible();
      if (!pLivingEntity.isInvisible() || flag) {
         VertexConsumer vertexconsumer;
         if (flag) {
            vertexconsumer = pBuffer.getBuffer(RenderType.outline(this.getTextureLocation(pLivingEntity)));
         } else {
            vertexconsumer = pBuffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(pLivingEntity)));
         }

         this.getParentModel().copyPropertiesTo(this.model);
         this.model.prepareMobModel(pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks);
         this.model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
         this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F), 1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}