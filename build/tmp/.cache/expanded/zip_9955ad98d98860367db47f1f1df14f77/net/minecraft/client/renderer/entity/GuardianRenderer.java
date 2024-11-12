package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class GuardianRenderer extends MobRenderer<Guardian, GuardianModel> {
   private static final ResourceLocation GUARDIAN_LOCATION = new ResourceLocation("textures/entity/guardian.png");
   private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
   private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

   public GuardianRenderer(EntityRendererProvider.Context p_174159_) {
      this(p_174159_, 0.5F, ModelLayers.GUARDIAN);
   }

   protected GuardianRenderer(EntityRendererProvider.Context pContext, float pShadowRadius, ModelLayerLocation pLayer) {
      super(pContext, new GuardianModel(pContext.bakeLayer(pLayer)), pShadowRadius);
   }

   public boolean shouldRender(Guardian pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
      if (super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ)) {
         return true;
      } else {
         if (pLivingEntity.hasActiveAttackTarget()) {
            LivingEntity livingentity = pLivingEntity.getActiveAttackTarget();
            if (livingentity != null) {
               Vec3 vec3 = this.getPosition(livingentity, (double)livingentity.getBbHeight() * 0.5D, 1.0F);
               Vec3 vec31 = this.getPosition(pLivingEntity, (double)pLivingEntity.getEyeHeight(), 1.0F);
               return pCamera.isVisible(new AABB(vec31.x, vec31.y, vec31.z, vec3.x, vec3.y, vec3.z));
            }
         }

         return false;
      }
   }

   private Vec3 getPosition(LivingEntity pLivingEntity, double pYOffset, float pPartialTick) {
      double d0 = Mth.lerp((double)pPartialTick, pLivingEntity.xOld, pLivingEntity.getX());
      double d1 = Mth.lerp((double)pPartialTick, pLivingEntity.yOld, pLivingEntity.getY()) + pYOffset;
      double d2 = Mth.lerp((double)pPartialTick, pLivingEntity.zOld, pLivingEntity.getZ());
      return new Vec3(d0, d1, d2);
   }

   public void render(Guardian pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
      LivingEntity livingentity = pEntity.getActiveAttackTarget();
      if (livingentity != null) {
         float f = pEntity.getAttackAnimationScale(pPartialTicks);
         float f1 = pEntity.getClientSideAttackTime() + pPartialTicks;
         float f2 = f1 * 0.5F % 1.0F;
         float f3 = pEntity.getEyeHeight();
         pPoseStack.pushPose();
         pPoseStack.translate(0.0F, f3, 0.0F);
         Vec3 vec3 = this.getPosition(livingentity, (double)livingentity.getBbHeight() * 0.5D, pPartialTicks);
         Vec3 vec31 = this.getPosition(pEntity, (double)f3, pPartialTicks);
         Vec3 vec32 = vec3.subtract(vec31);
         float f4 = (float)(vec32.length() + 1.0D);
         vec32 = vec32.normalize();
         float f5 = (float)Math.acos(vec32.y);
         float f6 = (float)Math.atan2(vec32.z, vec32.x);
         pPoseStack.mulPose(Axis.YP.rotationDegrees((((float)Math.PI / 2F) - f6) * (180F / (float)Math.PI)));
         pPoseStack.mulPose(Axis.XP.rotationDegrees(f5 * (180F / (float)Math.PI)));
         int i = 1;
         float f7 = f1 * 0.05F * -1.5F;
         float f8 = f * f;
         int j = 64 + (int)(f8 * 191.0F);
         int k = 32 + (int)(f8 * 191.0F);
         int l = 128 - (int)(f8 * 64.0F);
         float f9 = 0.2F;
         float f10 = 0.282F;
         float f11 = Mth.cos(f7 + 2.3561945F) * 0.282F;
         float f12 = Mth.sin(f7 + 2.3561945F) * 0.282F;
         float f13 = Mth.cos(f7 + ((float)Math.PI / 4F)) * 0.282F;
         float f14 = Mth.sin(f7 + ((float)Math.PI / 4F)) * 0.282F;
         float f15 = Mth.cos(f7 + 3.926991F) * 0.282F;
         float f16 = Mth.sin(f7 + 3.926991F) * 0.282F;
         float f17 = Mth.cos(f7 + 5.4977875F) * 0.282F;
         float f18 = Mth.sin(f7 + 5.4977875F) * 0.282F;
         float f19 = Mth.cos(f7 + (float)Math.PI) * 0.2F;
         float f20 = Mth.sin(f7 + (float)Math.PI) * 0.2F;
         float f21 = Mth.cos(f7 + 0.0F) * 0.2F;
         float f22 = Mth.sin(f7 + 0.0F) * 0.2F;
         float f23 = Mth.cos(f7 + ((float)Math.PI / 2F)) * 0.2F;
         float f24 = Mth.sin(f7 + ((float)Math.PI / 2F)) * 0.2F;
         float f25 = Mth.cos(f7 + ((float)Math.PI * 1.5F)) * 0.2F;
         float f26 = Mth.sin(f7 + ((float)Math.PI * 1.5F)) * 0.2F;
         float f27 = 0.0F;
         float f28 = 0.4999F;
         float f29 = -1.0F + f2;
         float f30 = f4 * 2.5F + f29;
         VertexConsumer vertexconsumer = pBuffer.getBuffer(BEAM_RENDER_TYPE);
         PoseStack.Pose posestack$pose = pPoseStack.last();
         Matrix4f matrix4f = posestack$pose.pose();
         Matrix3f matrix3f = posestack$pose.normal();
         vertex(vertexconsumer, matrix4f, matrix3f, f19, f4, f20, j, k, l, 0.4999F, f30);
         vertex(vertexconsumer, matrix4f, matrix3f, f19, 0.0F, f20, j, k, l, 0.4999F, f29);
         vertex(vertexconsumer, matrix4f, matrix3f, f21, 0.0F, f22, j, k, l, 0.0F, f29);
         vertex(vertexconsumer, matrix4f, matrix3f, f21, f4, f22, j, k, l, 0.0F, f30);
         vertex(vertexconsumer, matrix4f, matrix3f, f23, f4, f24, j, k, l, 0.4999F, f30);
         vertex(vertexconsumer, matrix4f, matrix3f, f23, 0.0F, f24, j, k, l, 0.4999F, f29);
         vertex(vertexconsumer, matrix4f, matrix3f, f25, 0.0F, f26, j, k, l, 0.0F, f29);
         vertex(vertexconsumer, matrix4f, matrix3f, f25, f4, f26, j, k, l, 0.0F, f30);
         float f31 = 0.0F;
         if (pEntity.tickCount % 2 == 0) {
            f31 = 0.5F;
         }

         vertex(vertexconsumer, matrix4f, matrix3f, f11, f4, f12, j, k, l, 0.5F, f31 + 0.5F);
         vertex(vertexconsumer, matrix4f, matrix3f, f13, f4, f14, j, k, l, 1.0F, f31 + 0.5F);
         vertex(vertexconsumer, matrix4f, matrix3f, f17, f4, f18, j, k, l, 1.0F, f31);
         vertex(vertexconsumer, matrix4f, matrix3f, f15, f4, f16, j, k, l, 0.5F, f31);
         pPoseStack.popPose();
      }

   }

   private static void vertex(VertexConsumer pConsumer, Matrix4f pPose, Matrix3f pNormal, float pX, float pY, float pZ, int pRed, int pGreen, int pBlue, float pU, float pV) {
      pConsumer.vertex(pPose, pX, pY, pZ).color(pRed, pGreen, pBlue, 255).uv(pU, pV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pNormal, 0.0F, 1.0F, 0.0F).endVertex();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Guardian pEntity) {
      return GUARDIAN_LOCATION;
   }
}