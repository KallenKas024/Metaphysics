package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EntityRenderDispatcher implements ResourceManagerReloadListener {
   private static final RenderType SHADOW_RENDER_TYPE = RenderType.entityShadow(new ResourceLocation("textures/misc/shadow.png"));
   private static final float MAX_SHADOW_RADIUS = 32.0F;
   private static final float SHADOW_POWER_FALLOFF_Y = 0.5F;
   public Map<EntityType<?>, EntityRenderer<?>> renderers = ImmutableMap.of();
   /** lists the various player skin types with their associated Renderer class instances. */
   private Map<String, EntityRenderer<? extends Player>> playerRenderers = ImmutableMap.of();
   public final TextureManager textureManager;
   private Level level;
   public Camera camera;
   private Quaternionf cameraOrientation;
   public Entity crosshairPickEntity;
   private final ItemRenderer itemRenderer;
   private final BlockRenderDispatcher blockRenderDispatcher;
   private final ItemInHandRenderer itemInHandRenderer;
   private final Font font;
   public final Options options;
   private final EntityModelSet entityModels;
   private boolean shouldRenderShadow = true;
   private boolean renderHitBoxes;

   public <E extends Entity> int getPackedLightCoords(E pEntity, float pPartialTicks) {
      return this.getRenderer(pEntity).getPackedLightCoords(pEntity, pPartialTicks);
   }

   public EntityRenderDispatcher(Minecraft pMinecraft, TextureManager pTextureManager, ItemRenderer pItemRenderer, BlockRenderDispatcher pBlockRenderDispatcher, Font pFont, Options pOptions, EntityModelSet pEntityModels) {
      this.textureManager = pTextureManager;
      this.itemRenderer = pItemRenderer;
      this.itemInHandRenderer = new ItemInHandRenderer(pMinecraft, this, pItemRenderer);
      this.blockRenderDispatcher = pBlockRenderDispatcher;
      this.font = pFont;
      this.options = pOptions;
      this.entityModels = pEntityModels;
   }

   public <T extends Entity> EntityRenderer<? super T> getRenderer(T pEntity) {
      if (pEntity instanceof AbstractClientPlayer) {
         String s = ((AbstractClientPlayer)pEntity).getModelName();
         EntityRenderer<? extends Player> entityrenderer = this.playerRenderers.get(s);
         return (EntityRenderer) (entityrenderer != null ? entityrenderer : this.playerRenderers.get("default"));
      } else {
         return (EntityRenderer) this.renderers.get(pEntity.getType());
      }
   }

   public void prepare(Level pLevel, Camera pActiveRenderInfo, Entity pEntity) {
      this.level = pLevel;
      this.camera = pActiveRenderInfo;
      this.cameraOrientation = pActiveRenderInfo.rotation();
      this.crosshairPickEntity = pEntity;
   }

   public void overrideCameraOrientation(Quaternionf pCameraOrientation) {
      this.cameraOrientation = pCameraOrientation;
   }

   public void setRenderShadow(boolean pRenderShadow) {
      this.shouldRenderShadow = pRenderShadow;
   }

   public void setRenderHitBoxes(boolean pDebugBoundingBox) {
      this.renderHitBoxes = pDebugBoundingBox;
   }

   public boolean shouldRenderHitBoxes() {
      return this.renderHitBoxes;
   }

   public <E extends Entity> boolean shouldRender(E pEntity, Frustum pFrustum, double pCamX, double pCamY, double pCamZ) {
      EntityRenderer<? super E> entityrenderer = this.getRenderer(pEntity);
      return entityrenderer.shouldRender(pEntity, pFrustum, pCamX, pCamY, pCamZ);
   }

   public <E extends Entity> void render(E pEntity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      EntityRenderer<? super E> entityrenderer = this.getRenderer(pEntity);

      try {
         Vec3 vec3 = entityrenderer.getRenderOffset(pEntity, pPartialTicks);
         double d2 = pX + vec3.x();
         double d3 = pY + vec3.y();
         double d0 = pZ + vec3.z();
         pPoseStack.pushPose();
         pPoseStack.translate(d2, d3, d0);
         entityrenderer.render(pEntity, pRotationYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
         if (pEntity.displayFireAnimation()) {
            this.renderFlame(pPoseStack, pBuffer, pEntity);
         }

         pPoseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
         if (this.options.entityShadows().get() && this.shouldRenderShadow && entityrenderer.shadowRadius > 0.0F && !pEntity.isInvisible()) {
            double d1 = this.distanceToSqr(pEntity.getX(), pEntity.getY(), pEntity.getZ());
            float f = (float)((1.0D - d1 / 256.0D) * (double)entityrenderer.shadowStrength);
            if (f > 0.0F) {
               renderShadow(pPoseStack, pBuffer, pEntity, f, pPartialTicks, this.level, Math.min(entityrenderer.shadowRadius, 32.0F));
            }
         }

         if (this.renderHitBoxes && !pEntity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
            renderHitbox(pPoseStack, pBuffer.getBuffer(RenderType.lines()), pEntity, pPartialTicks);
         }

         pPoseStack.popPose();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
         pEntity.fillCrashReportCategory(crashreportcategory);
         CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
         crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
         crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(this.level, pX, pY, pZ));
         crashreportcategory1.setDetail("Rotation", pRotationYaw);
         crashreportcategory1.setDetail("Delta", pPartialTicks);
         throw new ReportedException(crashreport);
      }
   }

   private static void renderHitbox(PoseStack pPoseStack, VertexConsumer pBuffer, Entity pEntity, float pPartialTicks) {
      AABB aabb = pEntity.getBoundingBox().move(-pEntity.getX(), -pEntity.getY(), -pEntity.getZ());
      LevelRenderer.renderLineBox(pPoseStack, pBuffer, aabb, 1.0F, 1.0F, 1.0F, 1.0F);
      if (pEntity.isMultipartEntity()) {
         double d0 = -Mth.lerp((double)pPartialTicks, pEntity.xOld, pEntity.getX());
         double d1 = -Mth.lerp((double)pPartialTicks, pEntity.yOld, pEntity.getY());
         double d2 = -Mth.lerp((double)pPartialTicks, pEntity.zOld, pEntity.getZ());

         for(net.minecraftforge.entity.PartEntity<?> enderdragonpart : pEntity.getParts()) {
            pPoseStack.pushPose();
            double d3 = d0 + Mth.lerp((double)pPartialTicks, enderdragonpart.xOld, enderdragonpart.getX());
            double d4 = d1 + Mth.lerp((double)pPartialTicks, enderdragonpart.yOld, enderdragonpart.getY());
            double d5 = d2 + Mth.lerp((double)pPartialTicks, enderdragonpart.zOld, enderdragonpart.getZ());
            pPoseStack.translate(d3, d4, d5);
            LevelRenderer.renderLineBox(pPoseStack, pBuffer, enderdragonpart.getBoundingBox().move(-enderdragonpart.getX(), -enderdragonpart.getY(), -enderdragonpart.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
            pPoseStack.popPose();
         }
      }

      if (pEntity instanceof LivingEntity) {
         float f = 0.01F;
         LevelRenderer.renderLineBox(pPoseStack, pBuffer, aabb.minX, (double)(pEntity.getEyeHeight() - 0.01F), aabb.minZ, aabb.maxX, (double)(pEntity.getEyeHeight() + 0.01F), aabb.maxZ, 1.0F, 0.0F, 0.0F, 1.0F);
      }

      Vec3 vec3 = pEntity.getViewVector(pPartialTicks);
      Matrix4f matrix4f = pPoseStack.last().pose();
      Matrix3f matrix3f = pPoseStack.last().normal();
      pBuffer.vertex(matrix4f, 0.0F, pEntity.getEyeHeight(), 0.0F).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
      pBuffer.vertex(matrix4f, (float)(vec3.x * 2.0D), (float)((double)pEntity.getEyeHeight() + vec3.y * 2.0D), (float)(vec3.z * 2.0D)).color(0, 0, 255, 255).normal(matrix3f, (float)vec3.x, (float)vec3.y, (float)vec3.z).endVertex();
   }

   private void renderFlame(PoseStack pPoseStack, MultiBufferSource pBuffer, Entity pEntity) {
      TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_0.sprite();
      TextureAtlasSprite textureatlassprite1 = ModelBakery.FIRE_1.sprite();
      pPoseStack.pushPose();
      float f = pEntity.getBbWidth() * 1.4F;
      pPoseStack.scale(f, f, f);
      float f1 = 0.5F;
      float f2 = 0.0F;
      float f3 = pEntity.getBbHeight() / f;
      float f4 = 0.0F;
      pPoseStack.mulPose(Axis.YP.rotationDegrees(-this.camera.getYRot()));
      pPoseStack.translate(0.0F, 0.0F, -0.3F + (float)((int)f3) * 0.02F);
      float f5 = 0.0F;
      int i = 0;
      VertexConsumer vertexconsumer = pBuffer.getBuffer(Sheets.cutoutBlockSheet());

      for(PoseStack.Pose posestack$pose = pPoseStack.last(); f3 > 0.0F; ++i) {
         TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
         float f6 = textureatlassprite2.getU0();
         float f7 = textureatlassprite2.getV0();
         float f8 = textureatlassprite2.getU1();
         float f9 = textureatlassprite2.getV1();
         if (i / 2 % 2 == 0) {
            float f10 = f8;
            f8 = f6;
            f6 = f10;
         }

         fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 0.0F - f4, f5, f8, f9);
         fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 0.0F - f4, f5, f6, f9);
         fireVertex(posestack$pose, vertexconsumer, -f1 - 0.0F, 1.4F - f4, f5, f6, f7);
         fireVertex(posestack$pose, vertexconsumer, f1 - 0.0F, 1.4F - f4, f5, f8, f7);
         f3 -= 0.45F;
         f4 -= 0.45F;
         f1 *= 0.9F;
         f5 += 0.03F;
      }

      pPoseStack.popPose();
   }

   private static void fireVertex(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, float pX, float pY, float pZ, float pTexU, float pTexV) {
      pBuffer.vertex(pMatrixEntry.pose(), pX, pY, pZ).color(255, 255, 255, 255).uv(pTexU, pTexV).overlayCoords(0, 10).uv2(240).normal(pMatrixEntry.normal(), 0.0F, 1.0F, 0.0F).endVertex();
   }

   private static void renderShadow(PoseStack pPoseStack, MultiBufferSource pBuffer, Entity pEntity, float pWeight, float pPartialTicks, LevelReader pLevel, float pSize) {
      float f = pSize;
      if (pEntity instanceof Mob mob) {
         if (mob.isBaby()) {
            f = pSize * 0.5F;
         }
      }

      double d2 = Mth.lerp((double)pPartialTicks, pEntity.xOld, pEntity.getX());
      double d0 = Mth.lerp((double)pPartialTicks, pEntity.yOld, pEntity.getY());
      double d1 = Mth.lerp((double)pPartialTicks, pEntity.zOld, pEntity.getZ());
      float f1 = Math.min(pWeight / 0.5F, f);
      int i = Mth.floor(d2 - (double)f);
      int j = Mth.floor(d2 + (double)f);
      int k = Mth.floor(d0 - (double)f1);
      int l = Mth.floor(d0);
      int i1 = Mth.floor(d1 - (double)f);
      int j1 = Mth.floor(d1 + (double)f);
      PoseStack.Pose posestack$pose = pPoseStack.last();
      VertexConsumer vertexconsumer = pBuffer.getBuffer(SHADOW_RENDER_TYPE);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int k1 = i1; k1 <= j1; ++k1) {
         for(int l1 = i; l1 <= j; ++l1) {
            blockpos$mutableblockpos.set(l1, 0, k1);
            ChunkAccess chunkaccess = pLevel.getChunk(blockpos$mutableblockpos);

            for(int i2 = k; i2 <= l; ++i2) {
               blockpos$mutableblockpos.setY(i2);
               float f2 = pWeight - (float)(d0 - (double)blockpos$mutableblockpos.getY()) * 0.5F;
               renderBlockShadow(posestack$pose, vertexconsumer, chunkaccess, pLevel, blockpos$mutableblockpos, d2, d0, d1, f, f2);
            }
         }
      }

   }

   private static void renderBlockShadow(PoseStack.Pose pPose, VertexConsumer pVertexConsumer, ChunkAccess pChunk, LevelReader pLevel, BlockPos pPos, double pX, double pY, double pZ, float pSize, float p_277496_) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pChunk.getBlockState(blockpos);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE && pLevel.getMaxLocalRawBrightness(pPos) > 3) {
         if (blockstate.isCollisionShapeFullBlock(pChunk, blockpos)) {
            VoxelShape voxelshape = blockstate.getShape(pChunk, blockpos);
            if (!voxelshape.isEmpty()) {
               float f = LightTexture.getBrightness(pLevel.dimensionType(), pLevel.getMaxLocalRawBrightness(pPos));
               float f1 = p_277496_ * 0.5F * f;
               if (f1 >= 0.0F) {
                  if (f1 > 1.0F) {
                     f1 = 1.0F;
                  }

                  AABB aabb = voxelshape.bounds();
                  double d0 = (double)pPos.getX() + aabb.minX;
                  double d1 = (double)pPos.getX() + aabb.maxX;
                  double d2 = (double)pPos.getY() + aabb.minY;
                  double d3 = (double)pPos.getZ() + aabb.minZ;
                  double d4 = (double)pPos.getZ() + aabb.maxZ;
                  float f2 = (float)(d0 - pX);
                  float f3 = (float)(d1 - pX);
                  float f4 = (float)(d2 - pY);
                  float f5 = (float)(d3 - pZ);
                  float f6 = (float)(d4 - pZ);
                  float f7 = -f2 / 2.0F / pSize + 0.5F;
                  float f8 = -f3 / 2.0F / pSize + 0.5F;
                  float f9 = -f5 / 2.0F / pSize + 0.5F;
                  float f10 = -f6 / 2.0F / pSize + 0.5F;
                  shadowVertex(pPose, pVertexConsumer, f1, f2, f4, f5, f7, f9);
                  shadowVertex(pPose, pVertexConsumer, f1, f2, f4, f6, f7, f10);
                  shadowVertex(pPose, pVertexConsumer, f1, f3, f4, f6, f8, f10);
                  shadowVertex(pPose, pVertexConsumer, f1, f3, f4, f5, f8, f9);
               }

            }
         }
      }
   }

   private static void shadowVertex(PoseStack.Pose pMatrixEntry, VertexConsumer pBuffer, float pAlpha, float pX, float pY, float pZ, float pTexU, float pTexV) {
      Vector3f vector3f = pMatrixEntry.pose().transformPosition(pX, pY, pZ, new Vector3f());
      pBuffer.vertex(vector3f.x(), vector3f.y(), vector3f.z(), 1.0F, 1.0F, 1.0F, pAlpha, pTexU, pTexV, OverlayTexture.NO_OVERLAY, 15728880, 0.0F, 1.0F, 0.0F);
   }

   /**
    * World sets this RenderManager's worldObj to the world provided
    */
   public void setLevel(@Nullable Level pLevel) {
      this.level = pLevel;
      if (pLevel == null) {
         this.camera = null;
      }

   }

   public double distanceToSqr(Entity pEntity) {
      return this.camera.getPosition().distanceToSqr(pEntity.position());
   }

   public double distanceToSqr(double pX, double pY, double pZ) {
      return this.camera.getPosition().distanceToSqr(pX, pY, pZ);
   }

   public Quaternionf cameraOrientation() {
      return this.cameraOrientation;
   }

   public ItemInHandRenderer getItemInHandRenderer() {
      return this.itemInHandRenderer;
   }

   public Map<String, EntityRenderer<? extends Player>> getSkinMap() {
      return java.util.Collections.unmodifiableMap(playerRenderers);
   }

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      EntityRendererProvider.Context entityrendererprovider$context = new EntityRendererProvider.Context(this, this.itemRenderer, this.blockRenderDispatcher, this.itemInHandRenderer, pResourceManager, this.entityModels, this.font);
      this.renderers = EntityRenderers.createEntityRenderers(entityrendererprovider$context);
      this.playerRenderers = EntityRenderers.createPlayerRenderers(entityrendererprovider$context);
      net.minecraftforge.fml.ModLoader.get().postEvent(new net.minecraftforge.client.event.EntityRenderersEvent.AddLayers(renderers, playerRenderers, entityrendererprovider$context));
   }
}
