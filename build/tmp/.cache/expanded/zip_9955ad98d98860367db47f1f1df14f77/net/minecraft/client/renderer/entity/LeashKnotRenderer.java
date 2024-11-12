package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
   private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
   private final LeashKnotModel<LeashFenceKnotEntity> model;

   public LeashKnotRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.model = new LeashKnotModel<>(pContext.bakeLayer(ModelLayers.LEASH_KNOT));
   }

   public void render(LeashFenceKnotEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.scale(-1.0F, -1.0F, 1.0F);
      this.model.setupAnim(pEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(KNOT_LOCATION));
      this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(LeashFenceKnotEntity pEntity) {
      return KNOT_LOCATION;
   }
}