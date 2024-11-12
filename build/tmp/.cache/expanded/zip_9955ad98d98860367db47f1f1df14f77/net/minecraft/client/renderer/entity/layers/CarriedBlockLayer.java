package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarriedBlockLayer extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
   private final BlockRenderDispatcher blockRenderer;

   public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> pRenderer, BlockRenderDispatcher pBlockRenderer) {
      super(pRenderer);
      this.blockRenderer = pBlockRenderer;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, EnderMan pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      BlockState blockstate = pLivingEntity.getCarriedBlock();
      if (blockstate != null) {
         pPoseStack.pushPose();
         pPoseStack.translate(0.0F, 0.6875F, -0.75F);
         pPoseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
         pPoseStack.translate(0.25F, 0.1875F, 0.25F);
         float f = 0.5F;
         pPoseStack.scale(-0.5F, -0.5F, 0.5F);
         pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
         this.blockRenderer.renderSingleBlock(blockstate, pPoseStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
         pPoseStack.popPose();
      }
   }
}