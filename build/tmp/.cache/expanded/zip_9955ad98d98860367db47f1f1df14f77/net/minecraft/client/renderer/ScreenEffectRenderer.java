package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ScreenEffectRenderer {
   private static final ResourceLocation UNDERWATER_LOCATION = new ResourceLocation("textures/misc/underwater.png");

   public static void renderScreenEffect(Minecraft pMinecraft, PoseStack pPoseStack) {
      Player player = pMinecraft.player;
      if (!player.noPhysics) {
         org.apache.commons.lang3.tuple.Pair<BlockState, BlockPos> overlay = getOverlayBlock(player);
         if (overlay != null) {
            if (!net.minecraftforge.client.ForgeHooksClient.renderBlockOverlay(player, pPoseStack, net.minecraftforge.client.event.RenderBlockScreenEffectEvent.OverlayType.BLOCK, overlay.getLeft(), overlay.getRight()))
               renderTex(pMinecraft.getBlockRenderer().getBlockModelShaper().getTexture(overlay.getLeft(), pMinecraft.level, overlay.getRight()), pPoseStack);
         }
      }

      if (!pMinecraft.player.isSpectator()) {
         if (pMinecraft.player.isEyeInFluid(FluidTags.WATER)) {
            if (!net.minecraftforge.client.ForgeHooksClient.renderWaterOverlay(player, pPoseStack))
            renderWater(pMinecraft, pPoseStack);
         }
         else if (!player.getEyeInFluidType().isAir()) net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(player.getEyeInFluidType()).renderOverlay(pMinecraft, pPoseStack);

         if (pMinecraft.player.isOnFire()) {
            if (!net.minecraftforge.client.ForgeHooksClient.renderFireOverlay(player, pPoseStack))
            renderFire(pMinecraft, pPoseStack);
         }
      }

   }

   @Nullable
   private static BlockState getViewBlockingState(Player pPlayer) {
      return getOverlayBlock(pPlayer).getLeft();
   }

   @Nullable
   private static org.apache.commons.lang3.tuple.Pair<BlockState, BlockPos> getOverlayBlock(Player pPlayer) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < 8; ++i) {
         double d0 = pPlayer.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * pPlayer.getBbWidth() * 0.8F);
         double d1 = pPlayer.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
         double d2 = pPlayer.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * pPlayer.getBbWidth() * 0.8F);
         blockpos$mutableblockpos.set(d0, d1, d2);
         BlockState blockstate = pPlayer.level().getBlockState(blockpos$mutableblockpos);
         if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.isViewBlocking(pPlayer.level(), blockpos$mutableblockpos)) {
            return org.apache.commons.lang3.tuple.Pair.of(blockstate, blockpos$mutableblockpos.immutable());
         }
      }

      return null;
   }

   private static void renderTex(TextureAtlasSprite pTexture, PoseStack pPoseStack) {
      RenderSystem.setShaderTexture(0, pTexture.atlasLocation());
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      float f = 0.1F;
      float f1 = -1.0F;
      float f2 = 1.0F;
      float f3 = -1.0F;
      float f4 = 1.0F;
      float f5 = -0.5F;
      float f6 = pTexture.getU0();
      float f7 = pTexture.getU1();
      float f8 = pTexture.getV0();
      float f9 = pTexture.getV1();
      Matrix4f matrix4f = pPoseStack.last().pose();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
      bufferbuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f7, f9).endVertex();
      bufferbuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f6, f9).endVertex();
      bufferbuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f6, f8).endVertex();
      bufferbuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).color(0.1F, 0.1F, 0.1F, 1.0F).uv(f7, f8).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
   }

   private static void renderWater(Minecraft pMinecraft, PoseStack pPoseStack) {
      renderFluid(pMinecraft, pPoseStack, UNDERWATER_LOCATION);
   }

   public static void renderFluid(Minecraft pMinecraft, PoseStack pPoseStack, ResourceLocation texture) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, texture);
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      BlockPos blockpos = BlockPos.containing(pMinecraft.player.getX(), pMinecraft.player.getEyeY(), pMinecraft.player.getZ());
      float f = LightTexture.getBrightness(pMinecraft.player.level().dimensionType(), pMinecraft.player.level().getMaxLocalRawBrightness(blockpos));
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(f, f, f, 0.1F);
      float f1 = 4.0F;
      float f2 = -1.0F;
      float f3 = 1.0F;
      float f4 = -1.0F;
      float f5 = 1.0F;
      float f6 = -0.5F;
      float f7 = -pMinecraft.player.getYRot() / 64.0F;
      float f8 = pMinecraft.player.getXRot() / 64.0F;
      Matrix4f matrix4f = pPoseStack.last().pose();
      bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
      bufferbuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + f7, 4.0F + f8).endVertex();
      bufferbuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + f7, 4.0F + f8).endVertex();
      bufferbuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + f7, 0.0F + f8).endVertex();
      bufferbuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + f7, 0.0F + f8).endVertex();
      BufferUploader.drawWithShader(bufferbuilder.end());
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.disableBlend();
   }

   private static void renderFire(Minecraft pMinecraft, PoseStack pPoseStack) {
      BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
      RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
      RenderSystem.depthFunc(519);
      RenderSystem.depthMask(false);
      RenderSystem.enableBlend();
      TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();
      RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
      float f = textureatlassprite.getU0();
      float f1 = textureatlassprite.getU1();
      float f2 = (f + f1) / 2.0F;
      float f3 = textureatlassprite.getV0();
      float f4 = textureatlassprite.getV1();
      float f5 = (f3 + f4) / 2.0F;
      float f6 = textureatlassprite.uvShrinkRatio();
      float f7 = Mth.lerp(f6, f, f2);
      float f8 = Mth.lerp(f6, f1, f2);
      float f9 = Mth.lerp(f6, f3, f5);
      float f10 = Mth.lerp(f6, f4, f5);
      float f11 = 1.0F;

      for(int i = 0; i < 2; ++i) {
         pPoseStack.pushPose();
         float f12 = -0.5F;
         float f13 = 0.5F;
         float f14 = -0.5F;
         float f15 = 0.5F;
         float f16 = -0.5F;
         pPoseStack.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
         pPoseStack.mulPose(Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0F));
         Matrix4f matrix4f = pPoseStack.last().pose();
         bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
         bufferbuilder.vertex(matrix4f, -0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f10).endVertex();
         bufferbuilder.vertex(matrix4f, 0.5F, -0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f10).endVertex();
         bufferbuilder.vertex(matrix4f, 0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f9).endVertex();
         bufferbuilder.vertex(matrix4f, -0.5F, 0.5F, -0.5F).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f9).endVertex();
         BufferUploader.drawWithShader(bufferbuilder.end());
         pPoseStack.popPose();
      }

      RenderSystem.disableBlend();
      RenderSystem.depthMask(true);
      RenderSystem.depthFunc(515);
   }
}
