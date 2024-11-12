package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt> {
   private final BlockRenderDispatcher blockRenderer;

   public TntRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.shadowRadius = 0.5F;
      this.blockRenderer = pContext.getBlockRenderDispatcher();
   }

   public void render(PrimedTnt pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      pPoseStack.pushPose();
      pPoseStack.translate(0.0F, 0.5F, 0.0F);
      int i = pEntity.getFuse();
      if ((float)i - pPartialTicks + 1.0F < 10.0F) {
         float f = 1.0F - ((float)i - pPartialTicks + 1.0F) / 10.0F;
         f = Mth.clamp(f, 0.0F, 1.0F);
         f *= f;
         f *= f;
         float f1 = 1.0F + f * 0.3F;
         pPoseStack.scale(f1, f1, f1);
      }

      pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
      pPoseStack.translate(-0.5F, -0.5F, 0.5F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
      TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, Blocks.TNT.defaultBlockState(), pPoseStack, pBuffer, pPackedLight, i / 5 % 2 == 0);
      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(PrimedTnt pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}