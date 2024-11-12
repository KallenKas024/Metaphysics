package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
   private final BlockRenderDispatcher blockRenderer;

   public TntMinecartRenderer(EntityRendererProvider.Context p_174424_) {
      super(p_174424_, ModelLayers.TNT_MINECART);
      this.blockRenderer = p_174424_.getBlockRenderDispatcher();
   }

   protected void renderMinecartContents(MinecartTNT pEntity, float pPartialTicks, BlockState pState, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      int i = pEntity.getFuse();
      if (i > -1 && (float)i - pPartialTicks + 1.0F < 10.0F) {
         float f = 1.0F - ((float)i - pPartialTicks + 1.0F) / 10.0F;
         f = Mth.clamp(f, 0.0F, 1.0F);
         f *= f;
         f *= f;
         float f1 = 1.0F + f * 0.3F;
         pPoseStack.scale(f1, f1, f1);
      }

      renderWhiteSolidBlock(this.blockRenderer, pState, pPoseStack, pBuffer, pPackedLight, i > -1 && i / 5 % 2 == 0);
   }

   public static void renderWhiteSolidBlock(BlockRenderDispatcher pBlockRenderDispatcher, BlockState pState, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, boolean pWhiteOverlay) {
      int i;
      if (pWhiteOverlay) {
         i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
      } else {
         i = OverlayTexture.NO_OVERLAY;
      }

      pBlockRenderDispatcher.renderSingleBlock(pState, pPoseStack, pBuffer, pPackedLight, i);
   }
}