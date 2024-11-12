package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
   private final EvokerFangsModel<EvokerFangs> model;

   public EvokerFangsRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.model = new EvokerFangsModel<>(pContext.bakeLayer(ModelLayers.EVOKER_FANGS));
   }

   public void render(EvokerFangs pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      float f = pEntity.getAnimationProgress(pPartialTicks);
      if (f != 0.0F) {
         float f1 = 2.0F;
         if (f > 0.9F) {
            f1 *= (1.0F - f) / 0.1F;
         }

         pPoseStack.pushPose();
         pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F - pEntity.getYRot()));
         pPoseStack.scale(-f1, -f1, f1);
         float f2 = 0.03125F;
         pPoseStack.translate(0.0D, -0.626D, 0.0D);
         pPoseStack.scale(0.5F, 0.5F, 0.5F);
         this.model.setupAnim(pEntity, f, 0.0F, 0.0F, pEntity.getYRot(), pEntity.getXRot());
         VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
         this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         pPoseStack.popPose();
         super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
      }
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(EvokerFangs pEntity) {
      return TEXTURE_LOCATION;
   }
}