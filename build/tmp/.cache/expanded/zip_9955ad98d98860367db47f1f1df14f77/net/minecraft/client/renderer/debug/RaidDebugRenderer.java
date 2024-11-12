package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaidDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
   private static final int MAX_RENDER_DIST = 160;
   private static final float TEXT_SCALE = 0.04F;
   private final Minecraft minecraft;
   private Collection<BlockPos> raidCenters = Lists.newArrayList();

   public RaidDebugRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void setRaidCenters(Collection<BlockPos> pRaidCenters) {
      this.raidCenters = pRaidCenters;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      BlockPos blockpos = this.getCamera().getBlockPosition();

      for(BlockPos blockpos1 : this.raidCenters) {
         if (blockpos.closerThan(blockpos1, 160.0D)) {
            highlightRaidCenter(pPoseStack, pBuffer, blockpos1);
         }
      }

   }

   private static void highlightRaidCenter(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pPos) {
      DebugRenderer.renderFilledBox(pPoseStack, pBuffer, pPos.offset(-1, -1, -1), pPos.offset(1, 1, 1), 1.0F, 0.0F, 0.0F, 0.15F);
      int i = -65536;
      renderTextOverBlock(pPoseStack, pBuffer, "Raid center", pPos, -65536);
   }

   private static void renderTextOverBlock(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, BlockPos pPos, int pColor) {
      double d0 = (double)pPos.getX() + 0.5D;
      double d1 = (double)pPos.getY() + 1.3D;
      double d2 = (double)pPos.getZ() + 0.5D;
      DebugRenderer.renderFloatingText(pPoseStack, pBuffer, pText, d0, d1, d2, pColor, 0.04F, true, 0.0F, true);
   }

   private Camera getCamera() {
      return this.minecraft.gameRenderer.getMainCamera();
   }
}