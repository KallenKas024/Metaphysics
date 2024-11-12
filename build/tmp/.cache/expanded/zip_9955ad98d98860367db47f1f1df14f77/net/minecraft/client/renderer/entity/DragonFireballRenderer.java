package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class DragonFireballRenderer extends EntityRenderer<DragonFireball> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_fireball.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE_LOCATION);

   public DragonFireballRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
   }

   protected int getBlockLightLevel(DragonFireball pEntity, BlockPos pPos) {
      return 15;
   }

   public void render(DragonFireball pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.scale(2.0F, 2.0F, 2.0F);
      pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      PoseStack.Pose posestack$pose = pPoseStack.last();
      Matrix4f matrix4f = posestack$pose.pose();
      Matrix3f matrix3f = posestack$pose.normal();
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RENDER_TYPE);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 0.0F, 0, 0, 1);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 1.0F, 0, 1, 1);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 1.0F, 1, 1, 0);
      vertex(vertexconsumer, matrix4f, matrix3f, pPackedLight, 0.0F, 1, 0, 0);
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   private static void vertex(VertexConsumer pConsumer, Matrix4f pPose, Matrix3f pNormal, int pLightmapUV, float pX, int pY, int pU, int pV) {
      pConsumer.vertex(pPose, pX - 0.5F, (float)pY - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)pU, (float)pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pLightmapUV).normal(pNormal, 0.0F, 1.0F, 0.0F).endVertex();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(DragonFireball pEntity) {
      return TEXTURE_LOCATION;
   }
}