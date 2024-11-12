package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public abstract class MobRenderer<T extends Mob, M extends EntityModel<T>> extends LivingEntityRenderer<T, M> {
   public static final int LEASH_RENDER_STEPS = 24;

   public MobRenderer(EntityRendererProvider.Context pContext, M pModel, float pShadowRadius) {
      super(pContext, pModel, pShadowRadius);
   }

   protected boolean shouldShowName(T pEntity) {
      return super.shouldShowName(pEntity) && (pEntity.shouldShowName() || pEntity.hasCustomName() && pEntity == this.entityRenderDispatcher.crosshairPickEntity);
   }

   public boolean shouldRender(T pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
      if (super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ)) {
         return true;
      } else {
         Entity entity = pLivingEntity.getLeashHolder();
         return entity != null ? pCamera.isVisible(entity.getBoundingBoxForCulling()) : false;
      }
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
      Entity entity = pEntity.getLeashHolder();
      if (entity != null) {
         this.renderLeash(pEntity, pPartialTicks, pPoseStack, pBuffer, entity);
      }
   }

   private <E extends Entity> void renderLeash(T pEntityLiving, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, E pLeashHolder) {
      pPoseStack.pushPose();
      Vec3 vec3 = pLeashHolder.getRopeHoldPosition(pPartialTicks);
      double d0 = (double)(Mth.lerp(pPartialTicks, pEntityLiving.yBodyRotO, pEntityLiving.yBodyRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
      Vec3 vec31 = pEntityLiving.getLeashOffset(pPartialTicks);
      double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
      double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
      double d3 = Mth.lerp((double)pPartialTicks, pEntityLiving.xo, pEntityLiving.getX()) + d1;
      double d4 = Mth.lerp((double)pPartialTicks, pEntityLiving.yo, pEntityLiving.getY()) + vec31.y;
      double d5 = Mth.lerp((double)pPartialTicks, pEntityLiving.zo, pEntityLiving.getZ()) + d2;
      pPoseStack.translate(d1, vec31.y, d2);
      float f = (float)(vec3.x - d3);
      float f1 = (float)(vec3.y - d4);
      float f2 = (float)(vec3.z - d5);
      float f3 = 0.025F;
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.leash());
      Matrix4f matrix4f = pPoseStack.last().pose();
      float f4 = Mth.invSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
      float f5 = f2 * f4;
      float f6 = f * f4;
      BlockPos blockpos = BlockPos.containing(pEntityLiving.getEyePosition(pPartialTicks));
      BlockPos blockpos1 = BlockPos.containing(pLeashHolder.getEyePosition(pPartialTicks));
      int i = this.getBlockLightLevel(pEntityLiving, blockpos);
      int j = this.entityRenderDispatcher.getRenderer(pLeashHolder).getBlockLightLevel(pLeashHolder, blockpos1);
      int k = pEntityLiving.level().getBrightness(LightLayer.SKY, blockpos);
      int l = pEntityLiving.level().getBrightness(LightLayer.SKY, blockpos1);

      for(int i1 = 0; i1 <= 24; ++i1) {
         addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
      }

      for(int j1 = 24; j1 >= 0; --j1) {
         addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
      }

      pPoseStack.popPose();
   }

   private static void addVertexPair(VertexConsumer pConsumer, Matrix4f pMatrix, float p_174310_, float p_174311_, float p_174312_, int pEntityBlockLightLevel, int pLeashHolderBlockLightLevel, int pEntitySkyLightLevel, int pLeashHolderSkyLightLevel, float p_174317_, float p_174318_, float p_174319_, float p_174320_, int pIndex, boolean p_174322_) {
      float f = (float)pIndex / 24.0F;
      int i = (int)Mth.lerp(f, (float)pEntityBlockLightLevel, (float)pLeashHolderBlockLightLevel);
      int j = (int)Mth.lerp(f, (float)pEntitySkyLightLevel, (float)pLeashHolderSkyLightLevel);
      int k = LightTexture.pack(i, j);
      float f1 = pIndex % 2 == (p_174322_ ? 1 : 0) ? 0.7F : 1.0F;
      float f2 = 0.5F * f1;
      float f3 = 0.4F * f1;
      float f4 = 0.3F * f1;
      float f5 = p_174310_ * f;
      float f6 = p_174311_ > 0.0F ? p_174311_ * f * f : p_174311_ - p_174311_ * (1.0F - f) * (1.0F - f);
      float f7 = p_174312_ * f;
      pConsumer.vertex(pMatrix, f5 - p_174319_, f6 + p_174318_, f7 + p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
      pConsumer.vertex(pMatrix, f5 + p_174319_, f6 + p_174317_ - p_174318_, f7 - p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
   }
}