package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrushableBlockRenderer implements BlockEntityRenderer<BrushableBlockEntity> {
   private final ItemRenderer itemRenderer;

   public BrushableBlockRenderer(BlockEntityRendererProvider.Context pContext) {
      this.itemRenderer = pContext.getItemRenderer();
   }

   public void render(BrushableBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      if (pBlockEntity.getLevel() != null) {
         int i = pBlockEntity.getBlockState().getValue(BlockStateProperties.DUSTED);
         if (i > 0) {
            Direction direction = pBlockEntity.getHitDirection();
            if (direction != null) {
               ItemStack itemstack = pBlockEntity.getItem();
               if (!itemstack.isEmpty()) {
                  pPoseStack.pushPose();
                  pPoseStack.translate(0.0F, 0.5F, 0.0F);
                  float[] afloat = this.translations(direction, i);
                  pPoseStack.translate(afloat[0], afloat[1], afloat[2]);
                  pPoseStack.mulPose(Axis.YP.rotationDegrees(75.0F));
                  boolean flag = direction == Direction.EAST || direction == Direction.WEST;
                  pPoseStack.mulPose(Axis.YP.rotationDegrees((float)((flag ? 90 : 0) + 11)));
                  pPoseStack.scale(0.5F, 0.5F, 0.5F);
                  int j = LevelRenderer.getLightColor(pBlockEntity.getLevel(), pBlockEntity.getBlockState(), pBlockEntity.getBlockPos().relative(direction));
                  this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, j, OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pBlockEntity.getLevel(), 0);
                  pPoseStack.popPose();
               }
            }
         }
      }
   }

   private float[] translations(Direction pDirection, int pDustedLevel) {
      float[] afloat = new float[]{0.5F, 0.0F, 0.5F};
      float f = (float)pDustedLevel / 10.0F * 0.75F;
      switch (pDirection) {
         case EAST:
            afloat[0] = 0.73F + f;
            break;
         case WEST:
            afloat[0] = 0.25F - f;
            break;
         case UP:
            afloat[1] = 0.25F + f;
            break;
         case DOWN:
            afloat[1] = -0.23F - f;
            break;
         case NORTH:
            afloat[2] = 0.25F - f;
            break;
         case SOUTH:
            afloat[2] = 0.73F + f;
      }

      return afloat;
   }
}