package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class DebugRenderer {
   public final PathfindingRenderer pathfindingRenderer = new PathfindingRenderer();
   public final DebugRenderer.SimpleDebugRenderer waterDebugRenderer;
   public final DebugRenderer.SimpleDebugRenderer chunkBorderRenderer;
   public final DebugRenderer.SimpleDebugRenderer heightMapRenderer;
   public final DebugRenderer.SimpleDebugRenderer collisionBoxRenderer;
   public final DebugRenderer.SimpleDebugRenderer supportBlockRenderer;
   public final DebugRenderer.SimpleDebugRenderer neighborsUpdateRenderer;
   public final StructureRenderer structureRenderer;
   public final DebugRenderer.SimpleDebugRenderer lightDebugRenderer;
   public final DebugRenderer.SimpleDebugRenderer worldGenAttemptRenderer;
   public final DebugRenderer.SimpleDebugRenderer solidFaceRenderer;
   public final DebugRenderer.SimpleDebugRenderer chunkRenderer;
   public final BrainDebugRenderer brainDebugRenderer;
   public final VillageSectionsDebugRenderer villageSectionsDebugRenderer;
   public final BeeDebugRenderer beeDebugRenderer;
   public final RaidDebugRenderer raidDebugRenderer;
   public final GoalSelectorDebugRenderer goalSelectorRenderer;
   public final GameTestDebugRenderer gameTestDebugRenderer;
   public final GameEventListenerRenderer gameEventListenerRenderer;
   public final LightSectionDebugRenderer skyLightSectionDebugRenderer;
   private boolean renderChunkborder;

   public DebugRenderer(Minecraft pMinecraft) {
      this.waterDebugRenderer = new WaterDebugRenderer(pMinecraft);
      this.chunkBorderRenderer = new ChunkBorderRenderer(pMinecraft);
      this.heightMapRenderer = new HeightMapRenderer(pMinecraft);
      this.collisionBoxRenderer = new CollisionBoxRenderer(pMinecraft);
      this.supportBlockRenderer = new SupportBlockRenderer(pMinecraft);
      this.neighborsUpdateRenderer = new NeighborsUpdateRenderer(pMinecraft);
      this.structureRenderer = new StructureRenderer(pMinecraft);
      this.lightDebugRenderer = new LightDebugRenderer(pMinecraft);
      this.worldGenAttemptRenderer = new WorldGenAttemptRenderer();
      this.solidFaceRenderer = new SolidFaceRenderer(pMinecraft);
      this.chunkRenderer = new ChunkDebugRenderer(pMinecraft);
      this.brainDebugRenderer = new BrainDebugRenderer(pMinecraft);
      this.villageSectionsDebugRenderer = new VillageSectionsDebugRenderer();
      this.beeDebugRenderer = new BeeDebugRenderer(pMinecraft);
      this.raidDebugRenderer = new RaidDebugRenderer(pMinecraft);
      this.goalSelectorRenderer = new GoalSelectorDebugRenderer(pMinecraft);
      this.gameTestDebugRenderer = new GameTestDebugRenderer();
      this.gameEventListenerRenderer = new GameEventListenerRenderer(pMinecraft);
      this.skyLightSectionDebugRenderer = new LightSectionDebugRenderer(pMinecraft, LightLayer.SKY);
   }

   public void clear() {
      this.pathfindingRenderer.clear();
      this.waterDebugRenderer.clear();
      this.chunkBorderRenderer.clear();
      this.heightMapRenderer.clear();
      this.collisionBoxRenderer.clear();
      this.supportBlockRenderer.clear();
      this.neighborsUpdateRenderer.clear();
      this.structureRenderer.clear();
      this.lightDebugRenderer.clear();
      this.worldGenAttemptRenderer.clear();
      this.solidFaceRenderer.clear();
      this.chunkRenderer.clear();
      this.brainDebugRenderer.clear();
      this.villageSectionsDebugRenderer.clear();
      this.beeDebugRenderer.clear();
      this.raidDebugRenderer.clear();
      this.goalSelectorRenderer.clear();
      this.gameTestDebugRenderer.clear();
      this.gameEventListenerRenderer.clear();
      this.skyLightSectionDebugRenderer.clear();
   }

   /**
    * Toggles the {@link #renderChunkborder} value, effectively toggling the {@link #chunkBorderRenderer} on or off.
    * 
    * @return the new, inverted value
    */
   public boolean switchRenderChunkborder() {
      this.renderChunkborder = !this.renderChunkborder;
      return this.renderChunkborder;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource.BufferSource pBufferSource, double pCamX, double pCamY, double pCamZ) {
      if (this.renderChunkborder && !Minecraft.getInstance().showOnlyReducedInfo()) {
         this.chunkBorderRenderer.render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
      }

      this.gameTestDebugRenderer.render(pPoseStack, pBufferSource, pCamX, pCamY, pCamZ);
   }

   public static Optional<Entity> getTargetedEntity(@Nullable Entity pEntity, int pDistance) {
      if (pEntity == null) {
         return Optional.empty();
      } else {
         Vec3 vec3 = pEntity.getEyePosition();
         Vec3 vec31 = pEntity.getViewVector(1.0F).scale((double)pDistance);
         Vec3 vec32 = vec3.add(vec31);
         AABB aabb = pEntity.getBoundingBox().expandTowards(vec31).inflate(1.0D);
         int i = pDistance * pDistance;
         Predicate<Entity> predicate = (p_113447_) -> {
            return !p_113447_.isSpectator() && p_113447_.isPickable();
         };
         EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(pEntity, vec3, vec32, aabb, predicate, (double)i);
         if (entityhitresult == null) {
            return Optional.empty();
         } else {
            return vec3.distanceToSqr(entityhitresult.getLocation()) > (double)i ? Optional.empty() : Optional.of(entityhitresult.getEntity());
         }
      }
   }

   public static void renderFilledBox(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pStartPos, BlockPos pEndPos, float pRed, float pGreen, float pBlue, float pAlpha) {
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      if (camera.isInitialized()) {
         Vec3 vec3 = camera.getPosition().reverse();
         AABB aabb = (new AABB(pStartPos, pEndPos)).move(vec3);
         renderFilledBox(pPoseStack, pBuffer, aabb, pRed, pGreen, pBlue, pAlpha);
      }
   }

   public static void renderFilledBox(PoseStack pPoseStack, MultiBufferSource pBuffer, BlockPos pPos, float pScale, float pRed, float pGreen, float pBlue, float pAlpha) {
      Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
      if (camera.isInitialized()) {
         Vec3 vec3 = camera.getPosition().reverse();
         AABB aabb = (new AABB(pPos)).move(vec3).inflate((double)pScale);
         renderFilledBox(pPoseStack, pBuffer, aabb, pRed, pGreen, pBlue, pAlpha);
      }
   }

   public static void renderFilledBox(PoseStack pPoseStack, MultiBufferSource pBuffer, AABB pBoundingBox, float pRed, float pGreen, float pBlue, float pAlpha) {
      renderFilledBox(pPoseStack, pBuffer, pBoundingBox.minX, pBoundingBox.minY, pBoundingBox.minZ, pBoundingBox.maxX, pBoundingBox.maxY, pBoundingBox.maxZ, pRed, pGreen, pBlue, pAlpha);
   }

   public static void renderFilledBox(PoseStack pPoseStack, MultiBufferSource pBuffer, double pMinX, double pMinY, double pMinZ, double pMaxX, double pMaxY, double pMaxZ, float pRed, float pGreen, float pBlue, float pAlpha) {
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.debugFilledBox());
      LevelRenderer.addChainedFilledBoxVertices(pPoseStack, vertexconsumer, pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ, pRed, pGreen, pBlue, pAlpha);
   }

   public static void renderFloatingText(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, int pX, int pY, int pZ, int pColor) {
      renderFloatingText(pPoseStack, pBuffer, pText, (double)pX + 0.5D, (double)pY + 0.5D, (double)pZ + 0.5D, pColor);
   }

   public static void renderFloatingText(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, double pX, double pY, double pZ, int pColor) {
      renderFloatingText(pPoseStack, pBuffer, pText, pX, pY, pZ, pColor, 0.02F);
   }

   public static void renderFloatingText(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, double pX, double pY, double pZ, int pColor, float pScale) {
      renderFloatingText(pPoseStack, pBuffer, pText, pX, pY, pZ, pColor, pScale, true, 0.0F, false);
   }

   public static void renderFloatingText(PoseStack pPoseStack, MultiBufferSource pBuffer, String pText, double pX, double pY, double pZ, int pColor, float pScale, boolean p_270731_, float p_270825_, boolean pTransparent) {
      Minecraft minecraft = Minecraft.getInstance();
      Camera camera = minecraft.gameRenderer.getMainCamera();
      if (camera.isInitialized() && minecraft.getEntityRenderDispatcher().options != null) {
         Font font = minecraft.font;
         double d0 = camera.getPosition().x;
         double d1 = camera.getPosition().y;
         double d2 = camera.getPosition().z;
         pPoseStack.pushPose();
         pPoseStack.translate((float)(pX - d0), (float)(pY - d1) + 0.07F, (float)(pZ - d2));
         pPoseStack.mulPoseMatrix((new Matrix4f()).rotation(camera.rotation()));
         pPoseStack.scale(-pScale, -pScale, pScale);
         float f = p_270731_ ? (float)(-font.width(pText)) / 2.0F : 0.0F;
         f -= p_270825_ / pScale;
         font.drawInBatch(pText, f, 0.0F, pColor, false, pPoseStack.last().pose(), pBuffer, pTransparent ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 0, 15728880);
         pPoseStack.popPose();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface SimpleDebugRenderer {
      void render(PoseStack pPoseStack, MultiBufferSource pBuffer, double pCamX, double pCamY, double pCamZ);

      default void clear() {
      }
   }
}