package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CubeMap {
   private static final int SIDES = 6;
   private final ResourceLocation[] images = new ResourceLocation[6];

   public CubeMap(ResourceLocation pBaseImageLocation) {
      for(int i = 0; i < 6; ++i) {
         this.images[i] = pBaseImageLocation.withPath(pBaseImageLocation.getPath() + "_" + i + ".png");
      }

   }

   public void render(Minecraft pMc, float pPitch, float pYaw, float pAlpha) {
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder bufferbuilder = tesselator.getBuilder();
      Matrix4f matrix4f = (new Matrix4f()).setPerspective(1.4835298F, (float)pMc.getWindow().getWidth() / (float)pMc.getWindow().getHeight(), 0.05F, 10.0F);
      RenderSystem.backupProjectionMatrix();
      RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.DISTANCE_TO_ORIGIN);
      PoseStack posestack = RenderSystem.getModelViewStack();
      posestack.pushPose();
      posestack.setIdentity();
      posestack.mulPose(Axis.XP.rotationDegrees(180.0F));
      RenderSystem.applyModelViewMatrix();
      RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.depthMask(false);
      int i = 2;

      for(int j = 0; j < 4; ++j) {
         posestack.pushPose();
         float f = ((float)(j % 2) / 2.0F - 0.5F) / 256.0F;
         float f1 = ((float)(j / 2) / 2.0F - 0.5F) / 256.0F;
         float f2 = 0.0F;
         posestack.translate(f, f1, 0.0F);
         posestack.mulPose(Axis.XP.rotationDegrees(pPitch));
         posestack.mulPose(Axis.YP.rotationDegrees(pYaw));
         RenderSystem.applyModelViewMatrix();

         for(int k = 0; k < 6; ++k) {
            RenderSystem.setShaderTexture(0, this.images[k]);
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            int l = Math.round(255.0F * pAlpha) / (j + 1);
            if (k == 0) {
               bufferbuilder.vertex(-1.0D, -1.0D, 1.0D).uv(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, 1.0D, 1.0D).uv(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, 1.0D, 1.0D).uv(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, -1.0D, 1.0D).uv(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 1) {
               bufferbuilder.vertex(1.0D, -1.0D, 1.0D).uv(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, 1.0D, 1.0D).uv(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, 1.0D, -1.0D).uv(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, -1.0D, -1.0D).uv(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 2) {
               bufferbuilder.vertex(1.0D, -1.0D, -1.0D).uv(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, 1.0D, -1.0D).uv(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, 1.0D, -1.0D).uv(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, -1.0D, -1.0D).uv(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 3) {
               bufferbuilder.vertex(-1.0D, -1.0D, -1.0D).uv(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, 1.0D, -1.0D).uv(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, 1.0D, 1.0D).uv(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, -1.0D, 1.0D).uv(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 4) {
               bufferbuilder.vertex(-1.0D, -1.0D, -1.0D).uv(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, -1.0D, 1.0D).uv(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, -1.0D, 1.0D).uv(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, -1.0D, -1.0D).uv(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            if (k == 5) {
               bufferbuilder.vertex(-1.0D, 1.0D, 1.0D).uv(0.0F, 0.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(-1.0D, 1.0D, -1.0D).uv(0.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, 1.0D, -1.0D).uv(1.0F, 1.0F).color(255, 255, 255, l).endVertex();
               bufferbuilder.vertex(1.0D, 1.0D, 1.0D).uv(1.0F, 0.0F).color(255, 255, 255, l).endVertex();
            }

            tesselator.end();
         }

         posestack.popPose();
         RenderSystem.applyModelViewMatrix();
         RenderSystem.colorMask(true, true, true, false);
      }

      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.restoreProjectionMatrix();
      posestack.popPose();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
   }

   public CompletableFuture<Void> preload(TextureManager pTexMngr, Executor pBackgroundExecutor) {
      CompletableFuture<?>[] completablefuture = new CompletableFuture[6];

      for(int i = 0; i < completablefuture.length; ++i) {
         completablefuture[i] = pTexMngr.preload(this.images[i], pBackgroundExecutor);
      }

      return CompletableFuture.allOf(completablefuture);
   }
}