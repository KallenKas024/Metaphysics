package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
   private final BlockRenderDispatcher dispatcher;

   public FallingBlockRenderer(EntityRendererProvider.Context pContext) {
      super(pContext);
      this.shadowRadius = 0.5F;
      this.dispatcher = pContext.getBlockRenderDispatcher();
   }

   public void render(FallingBlockEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      BlockState blockstate = pEntity.getBlockState();
      if (blockstate.getRenderShape() == RenderShape.MODEL) {
         Level level = pEntity.level();
         if (blockstate != level.getBlockState(pEntity.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            pPoseStack.pushPose();
            BlockPos blockpos = BlockPos.containing(pEntity.getX(), pEntity.getBoundingBox().maxY, pEntity.getZ());
            pPoseStack.translate(-0.5D, 0.0D, -0.5D);
            var model = this.dispatcher.getBlockModel(blockstate);
            for (var renderType : model.getRenderTypes(blockstate, RandomSource.create(blockstate.getSeed(pEntity.getStartPos())), net.minecraftforge.client.model.data.ModelData.EMPTY))
               this.dispatcher.getModelRenderer().tesselateBlock(level, model, blockstate, blockpos, pPoseStack, pBuffer.getBuffer(renderType), false, RandomSource.create(), blockstate.getSeed(pEntity.getStartPos()), OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, renderType);
            pPoseStack.popPose();
            super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
         }
      }
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(FallingBlockEntity pEntity) {
      return TextureAtlas.LOCATION_BLOCKS;
   }
}
