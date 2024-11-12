package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
   private final Minecraft minecraft;
   private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, BoundingBox>> postPiecesBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.newIdentityHashMap();
   private static final int MAX_RENDER_DIST = 500;

   public StructureRenderer(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ) {
      Camera camera = this.minecraft.gameRenderer.getMainCamera();
      LevelAccessor levelaccessor = this.minecraft.level;
      DimensionType dimensiontype = levelaccessor.dimensionType();
      BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0D, camera.getPosition().z);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.lines());
      if (this.postMainBoxes.containsKey(dimensiontype)) {
         for(BoundingBox boundingbox : this.postMainBoxes.get(dimensiontype).values()) {
            if (blockpos.closerThan(boundingbox.getCenter(), 500.0D)) {
               LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, (double)boundingbox.minX() - pCamX, (double)boundingbox.minY() - pCamY, (double)boundingbox.minZ() - pCamZ, (double)(boundingbox.maxX() + 1) - pCamX, (double)(boundingbox.maxY() + 1) - pCamY, (double)(boundingbox.maxZ() + 1) - pCamZ, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      if (this.postPiecesBoxes.containsKey(dimensiontype)) {
         for(Map.Entry<String, BoundingBox> entry : this.postPiecesBoxes.get(dimensiontype).entrySet()) {
            String s = entry.getKey();
            BoundingBox boundingbox1 = entry.getValue();
            Boolean obool = this.startPiecesMap.get(dimensiontype).get(s);
            if (blockpos.closerThan(boundingbox1.getCenter(), 500.0D)) {
               if (obool) {
                  LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, (double)boundingbox1.minX() - pCamX, (double)boundingbox1.minY() - pCamY, (double)boundingbox1.minZ() - pCamZ, (double)(boundingbox1.maxX() + 1) - pCamX, (double)(boundingbox1.maxY() + 1) - pCamY, (double)(boundingbox1.maxZ() + 1) - pCamZ, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
               } else {
                  LevelRenderer.renderLineBox(pPoseStack, vertexconsumer, (double)boundingbox1.minX() - pCamX, (double)boundingbox1.minY() - pCamY, (double)boundingbox1.minZ() - pCamZ, (double)(boundingbox1.maxX() + 1) - pCamX, (double)(boundingbox1.maxY() + 1) - pCamY, (double)(boundingbox1.maxZ() + 1) - pCamZ, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
               }
            }
         }
      }

   }

   public void addBoundingBox(BoundingBox pPostMainBox, List<BoundingBox> pPieceBoxes, List<Boolean> pStartPieceFlags, DimensionType pDimensionType) {
      if (!this.postMainBoxes.containsKey(pDimensionType)) {
         this.postMainBoxes.put(pDimensionType, Maps.newHashMap());
      }

      if (!this.postPiecesBoxes.containsKey(pDimensionType)) {
         this.postPiecesBoxes.put(pDimensionType, Maps.newHashMap());
         this.startPiecesMap.put(pDimensionType, Maps.newHashMap());
      }

      this.postMainBoxes.get(pDimensionType).put(pPostMainBox.toString(), pPostMainBox);

      for(int i = 0; i < pPieceBoxes.size(); ++i) {
         BoundingBox boundingbox = pPieceBoxes.get(i);
         Boolean obool = pStartPieceFlags.get(i);
         this.postPiecesBoxes.get(pDimensionType).put(boundingbox.toString(), boundingbox);
         this.startPiecesMap.get(pDimensionType).put(boundingbox.toString(), obool);
      }

   }

   public void clear() {
      this.postMainBoxes.clear();
      this.postPiecesBoxes.clear();
      this.startPiecesMap.clear();
   }
}