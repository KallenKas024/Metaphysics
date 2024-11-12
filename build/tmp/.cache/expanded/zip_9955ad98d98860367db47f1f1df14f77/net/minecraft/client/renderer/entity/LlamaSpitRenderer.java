package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaSpitRenderer extends EntityRenderer<LlamaSpit> {
   private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
   private final LlamaSpitModel<LlamaSpit> model;

   public LlamaSpitRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.model = new LlamaSpitModel<>(pContext.bakeLayer(ModelLayers.LLAMA_SPIT));
   }

   public void render(LlamaSpit pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.translate(0.0F, 0.15F, 0.0F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot())));
      this.model.setupAnim(pEntity, pPartialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(LLAMA_SPIT_LOCATION));
      this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(LlamaSpit pEntity) {
      return LLAMA_SPIT_LOCATION;
   }
}