package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxRenderer implements BlockEntityRenderer<ShulkerBoxBlockEntity> {
   private final ShulkerModel<?> model;

   public ShulkerBoxRenderer(BlockEntityRendererProvider.Context pContext) {
      this.model = new ShulkerModel(pContext.bakeLayer(ModelLayers.SHULKER));
   }

   public void render(ShulkerBoxBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      Direction direction = Direction.UP;
      if (pBlockEntity.hasLevel()) {
         BlockState blockstate = pBlockEntity.getLevel().getBlockState(pBlockEntity.getBlockPos());
         if (blockstate.getBlock() instanceof ShulkerBoxBlock) {
            direction = blockstate.getValue(ShulkerBoxBlock.FACING);
         }
      }

      DyeColor dyecolor = pBlockEntity.getColor();
      Material material;
      if (dyecolor == null) {
         material = Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION;
      } else {
         material = Sheets.SHULKER_TEXTURE_LOCATION.get(dyecolor.getId());
      }

      pPoseStack.pushPose();
      pPoseStack.translate(0.5F, 0.5F, 0.5F);
      float f = 0.9995F;
      pPoseStack.scale(0.9995F, 0.9995F, 0.9995F);
      pPoseStack.mulPose(direction.getRotation());
      pPoseStack.scale(1.0F, -1.0F, -1.0F);
      pPoseStack.translate(0.0F, -1.0F, 0.0F);
      ModelPart modelpart = this.model.getLid();
      modelpart.setPos(0.0F, 24.0F - pBlockEntity.getProgress(pPartialTick) * 0.5F * 16.0F, 0.0F);
      modelpart.yRot = 270.0F * pBlockEntity.getProgress(pPartialTick) * ((float)Math.PI / 180F);
      VertexConsumer vertexconsumer = material.buffer(pBuffer, RenderType::entityCutoutNoCull);
      this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
      pPoseStack.popPose();
   }
}