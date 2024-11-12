package net.minecraft.client.renderer.debug;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SupportBlockRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private double lastUpdateTime = Double.MIN_VALUE;
   private List<Entity> surroundEntities = Collections.emptyList();

   public SupportBlockRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      double d0 = (double)Util.getNanos();
      if (d0 - this.lastUpdateTime > 1.0E8D) {
         this.lastUpdateTime = d0;
         Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
         this.surroundEntities = ImmutableList.copyOf(entity.level().getEntities(entity, entity.getBoundingBox().inflate(16.0D)));
      }

      Player player = this.minecraft.player;
      if (player != null && player.mainSupportingBlockPos.isPresent()) {
         this.drawHighlights(pPoseStack, pBuffer, pCamX, pCamY, pCamZ, player, () -> {
            return 0.0D;
         }, 1.0F, 0.0F, 0.0F);
      }

      for(Entity entity1 : this.surroundEntities) {
         if (entity1 != player) {
            this.drawHighlights(pPoseStack, pBuffer, pCamX, pCamY, pCamZ, entity1, () -> {
               return this.getBias(entity1);
            }, 0.0F, 1.0F, 0.0F);
         }
      }

   }

   private void drawHighlights(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ, Entity pEntity, DoubleSupplier pBiasGetter, float pRed, float pGreen, float pBlue) {
      pEntity.mainSupportingBlockPos.ifPresent((p_286428_) -> {
         double d0 = pBiasGetter.getAsDouble();
         BlockPos blockpos = pEntity.getOnPos();
         this.highlightPosition(blockpos, pPoseStack, pCamX, pCamY, pCamZ, pBuffer, 0.02D + d0, pRed, pGreen, pBlue);
         BlockPos blockpos1 = pEntity.getOnPosLegacy();
         if (!blockpos1.equals(blockpos)) {
            this.highlightPosition(blockpos1, pPoseStack, pCamX, pCamY, pCamZ, pBuffer, 0.04D + d0, 0.0F, 1.0F, 1.0F);
         }

      });
   }

   private double getBias(Entity pEntity) {
      return 0.02D * (double)(String.valueOf((double)pEntity.getId() + 0.132453657D).hashCode() % 1000) / 1000.0D;
   }

   private void highlightPosition(BlockPos pPos, PoseStack pPoseStack, double pCamX, double pCamY, double pCamZ, MultiBufferSource pBuffer, double pBias, float pRed, float pGreen, float pBlue) {
      double d0 = (double)pPos.getX() - pCamX - 2.0D * pBias;
      double d1 = (double)pPos.getY() - pCamY - 2.0D * pBias;
      double d2 = (double)pPos.getZ() - pCamZ - 2.0D * pBias;
      double d3 = d0 + 1.0D + 4.0D * pBias;
      double d4 = d1 + 1.0D + 4.0D * pBias;
      double d5 = d2 + 1.0D + 4.0D * pBias;
      LevelRenderer.renderLineBox(pPoseStack, pBuffer.getBuffer(RenderType.lines()), d0, d1, d2, d3, d4, d5, pRed, pGreen, pBlue, 0.4F);
      LevelRenderer.renderVoxelShape(pPoseStack, pBuffer.getBuffer(RenderType.lines()), this.minecraft.level.getBlockState(pPos).getCollisionShape(this.minecraft.level, pPos, CollisionContext.empty()).move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ()), -pCamX, -pCamY, -pCamZ, pRed, pGreen, pBlue, 1.0F, false);
   }
}