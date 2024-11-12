package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
   private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
   protected final EntityModel<T> model;
   private final BlockRenderDispatcher blockRenderer;

   public MinecartRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pLayer) {
      super(pContext);
      this.shadowRadius = 0.7F;
      this.model = new MinecartModel<>(pContext.bakeLayer(pLayer));
      this.blockRenderer = pContext.getBlockRenderDispatcher();
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
      pPoseStack.pushPose();
      long i = (long)pEntity.getId() * 493286711L;
      i = i * i * 4392167121L + i * 98761L;
      float f = (((float)(i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float f1 = (((float)(i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float f2 = (((float)(i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      pPoseStack.translate(f, f1, f2);
      double d0 = Mth.lerp((double)pPartialTicks, pEntity.xOld, pEntity.getX());
      double d1 = Mth.lerp((double)pPartialTicks, pEntity.yOld, pEntity.getY());
      double d2 = Mth.lerp((double)pPartialTicks, pEntity.zOld, pEntity.getZ());
      double d3 = (double)0.3F;
      Vec3 vec3 = pEntity.getPos(d0, d1, d2);
      float f3 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
      if (vec3 != null) {
         Vec3 vec31 = pEntity.getPosOffs(d0, d1, d2, (double)0.3F);
         Vec3 vec32 = pEntity.getPosOffs(d0, d1, d2, (double)-0.3F);
         if (vec31 == null) {
            vec31 = vec3;
         }

         if (vec32 == null) {
            vec32 = vec3;
         }

         pPoseStack.translate(vec3.x - d0, (vec31.y + vec32.y) / 2.0D - d1, vec3.z - d2);
         Vec3 vec33 = vec32.add(-vec31.x, -vec31.y, -vec31.z);
         if (vec33.length() != 0.0D) {
            vec33 = vec33.normalize();
            pEntityYaw = (float)(Math.atan2(vec33.z, vec33.x) * 180.0D / Math.PI);
            f3 = (float)(Math.atan(vec33.y) * 73.0D);
         }
      }

      pPoseStack.translate(0.0F, 0.375F, 0.0F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pEntityYaw));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(-f3));
      float f5 = (float)pEntity.getHurtTime() - pPartialTicks;
      float f6 = pEntity.getDamage() - pPartialTicks;
      if (f6 < 0.0F) {
         f6 = 0.0F;
      }

      if (f5 > 0.0F) {
         pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(f5) * f5 * f6 / 10.0F * (float)pEntity.getHurtDir()));
      }

      int j = pEntity.getDisplayOffset();
      BlockState blockstate = pEntity.getDisplayBlockState();
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         pPoseStack.pushPose();
         float f4 = 0.75F;
         pPoseStack.scale(0.75F, 0.75F, 0.75F);
         pPoseStack.translate(-0.5F, (float)(j - 8) / 16.0F, 0.5F);
         pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
         this.renderMinecartContents(pEntity, pPartialTicks, blockstate, pPoseStack, pBuffer, pPackedLight);
         pPoseStack.popPose();
      }

      pPoseStack.scale(-1.0F, -1.0F, 1.0F);
      this.model.setupAnim(pEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.getTextureLocation(pEntity)));
      this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(T pEntity) {
      return MINECART_LOCATION;
   }

   protected void renderMinecartContents(T pEntity, float pPartialTicks, BlockState pState, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      this.blockRenderer.renderSingleBlock(pState, pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
   }
}