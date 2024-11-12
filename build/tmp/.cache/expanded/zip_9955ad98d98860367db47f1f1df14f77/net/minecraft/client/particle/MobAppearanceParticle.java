package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.GuardianModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobAppearanceParticle extends Particle {
   private final Model model;
   private final RenderType renderType = RenderType.entityTranslucent(ElderGuardianRenderer.GUARDIAN_ELDER_LOCATION);

   MobAppearanceParticle(ClientLevel pLevel, double pX, double pY, double pZ) {
      super(pLevel, pX, pY, pZ);
      this.model = new GuardianModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.ELDER_GUARDIAN));
      this.gravity = 0.0F;
      this.lifetime = 30;
   }

   public ParticleRenderType getRenderType() {
      return ParticleRenderType.CUSTOM;
   }

   public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
      float f = ((float)this.age + pPartialTicks) / (float)this.lifetime;
      float f1 = 0.05F + 0.5F * Mth.sin(f * (float)Math.PI);
      PoseStack posestack = new PoseStack();
      posestack.mulPose(pRenderInfo.rotation());
      posestack.mulPose(Axis.XP.rotationDegrees(150.0F * f - 60.0F));
      posestack.scale(-1.0F, -1.0F, 1.0F);
      posestack.translate(0.0F, -1.101F, 1.5F);
      MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
      VertexConsumer vertexconsumer = multibuffersource$buffersource.getBuffer(this.renderType);
      this.model.renderToBuffer(posestack, vertexconsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, f1);
      multibuffersource$buffersource.endBatch();
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new MobAppearanceParticle(pLevel, pX, pY, pZ);
      }
   }
}