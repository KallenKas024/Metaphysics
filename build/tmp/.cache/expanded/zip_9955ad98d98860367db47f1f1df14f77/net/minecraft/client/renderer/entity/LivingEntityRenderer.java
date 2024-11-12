package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.scores.Team;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final float EYE_BED_OFFSET = 0.1F;
   protected M model;
   protected final List<RenderLayer<T, M>> layers = Lists.newArrayList();

   public LivingEntityRenderer(EntityRendererProvider.Context pContext, M pModel, float pShadowRadius) {
      super(pContext);
      this.model = pModel;
      this.shadowRadius = pShadowRadius;
   }

   public final boolean addLayer(RenderLayer<T, M> pLayer) {
      return this.layers.add(pLayer);
   }

   public M getModel() {
      return this.model;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Pre<T, M>(pEntity, this, pPartialTicks, pPoseStack, pBuffer, pPackedLight))) return;
      pPoseStack.pushPose();
      this.model.attackTime = this.getAttackAnim(pEntity, pPartialTicks);

      boolean shouldSit = pEntity.isPassenger() && (pEntity.getVehicle() != null && pEntity.getVehicle().shouldRiderSit());
      this.model.riding = shouldSit;
      this.model.young = pEntity.isBaby();
      float f = Mth.rotLerp(pPartialTicks, pEntity.yBodyRotO, pEntity.yBodyRot);
      float f1 = Mth.rotLerp(pPartialTicks, pEntity.yHeadRotO, pEntity.yHeadRot);
      float f2 = f1 - f;
      if (shouldSit && pEntity.getVehicle() instanceof LivingEntity) {
         LivingEntity livingentity = (LivingEntity)pEntity.getVehicle();
         f = Mth.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
         f2 = f1 - f;
         float f3 = Mth.wrapDegrees(f2);
         if (f3 < -85.0F) {
            f3 = -85.0F;
         }

         if (f3 >= 85.0F) {
            f3 = 85.0F;
         }

         f = f1 - f3;
         if (f3 * f3 > 2500.0F) {
            f += f3 * 0.2F;
         }

         f2 = f1 - f;
      }

      float f6 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
      if (isEntityUpsideDown(pEntity)) {
         f6 *= -1.0F;
         f2 *= -1.0F;
      }

      if (pEntity.hasPose(Pose.SLEEPING)) {
         Direction direction = pEntity.getBedOrientation();
         if (direction != null) {
            float f4 = pEntity.getEyeHeight(Pose.STANDING) - 0.1F;
            pPoseStack.translate((float)(-direction.getStepX()) * f4, 0.0F, (float)(-direction.getStepZ()) * f4);
         }
      }

      float f7 = this.getBob(pEntity, pPartialTicks);
      this.setupRotations(pEntity, pPoseStack, f7, f, pPartialTicks);
      pPoseStack.scale(-1.0F, -1.0F, 1.0F);
      this.scale(pEntity, pPoseStack, pPartialTicks);
      pPoseStack.translate(0.0F, -1.501F, 0.0F);
      float f8 = 0.0F;
      float f5 = 0.0F;
      if (!shouldSit && pEntity.isAlive()) {
         f8 = pEntity.walkAnimation.speed(pPartialTicks);
         f5 = pEntity.walkAnimation.position(pPartialTicks);
         if (pEntity.isBaby()) {
            f5 *= 3.0F;
         }

         if (f8 > 1.0F) {
            f8 = 1.0F;
         }
      }

      this.model.prepareMobModel(pEntity, f5, f8, pPartialTicks);
      this.model.setupAnim(pEntity, f5, f8, f7, f2, f6);
      Minecraft minecraft = Minecraft.getInstance();
      boolean flag = this.isBodyVisible(pEntity);
      boolean flag1 = !flag && !pEntity.isInvisibleTo(minecraft.player);
      boolean flag2 = minecraft.shouldEntityAppearGlowing(pEntity);
      RenderType rendertype = this.getRenderType(pEntity, flag, flag1, flag2);
      if (rendertype != null) {
         VertexConsumer vertexconsumer = pBuffer.getBuffer(rendertype);
         int i = getOverlayCoords(pEntity, this.getWhiteOverlayProgress(pEntity, pPartialTicks));
         this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
      }

      if (!pEntity.isSpectator()) {
         for(RenderLayer<T, M> renderlayer : this.layers) {
            renderlayer.render(pPoseStack, pBuffer, pPackedLight, pEntity, f5, f8, pPartialTicks, f7, f2, f6);
         }
      }

      pPoseStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.RenderLivingEvent.Post<T, M>(pEntity, this, pPartialTicks, pPoseStack, pBuffer, pPackedLight));
   }

   @Nullable
   protected RenderType getRenderType(T pLivingEntity, boolean pBodyVisible, boolean pTranslucent, boolean pGlowing) {
      ResourceLocation resourcelocation = this.getTextureLocation(pLivingEntity);
      if (pTranslucent) {
         return RenderType.itemEntityTranslucentCull(resourcelocation);
      } else if (pBodyVisible) {
         return this.model.renderType(resourcelocation);
      } else {
         return pGlowing ? RenderType.outline(resourcelocation) : null;
      }
   }

   public static int getOverlayCoords(LivingEntity pLivingEntity, float pU) {
      return OverlayTexture.pack(OverlayTexture.u(pU), OverlayTexture.v(pLivingEntity.hurtTime > 0 || pLivingEntity.deathTime > 0));
   }

   protected boolean isBodyVisible(T pLivingEntity) {
      return !pLivingEntity.isInvisible();
   }

   private static float sleepDirectionToRotation(Direction pFacing) {
      switch (pFacing) {
         case SOUTH:
            return 90.0F;
         case WEST:
            return 0.0F;
         case NORTH:
            return 270.0F;
         case EAST:
            return 180.0F;
         default:
            return 0.0F;
      }
   }

   protected boolean isShaking(T pEntity) {
      return pEntity.isFullyFrozen();
   }

   protected void setupRotations(T pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      if (this.isShaking(pEntityLiving)) {
         pRotationYaw += (float)(Math.cos((double)pEntityLiving.tickCount * 3.25D) * Math.PI * (double)0.4F);
      }

      if (!pEntityLiving.hasPose(Pose.SLEEPING)) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F - pRotationYaw));
      }

      if (pEntityLiving.deathTime > 0) {
         float f = ((float)pEntityLiving.deathTime + pPartialTicks - 1.0F) / 20.0F * 1.6F;
         f = Mth.sqrt(f);
         if (f > 1.0F) {
            f = 1.0F;
         }

         pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(pEntityLiving)));
      } else if (pEntityLiving.isAutoSpinAttack()) {
         pPoseStack.mulPose(Axis.XP.rotationDegrees(-90.0F - pEntityLiving.getXRot()));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(((float)pEntityLiving.tickCount + pPartialTicks) * -75.0F));
      } else if (pEntityLiving.hasPose(Pose.SLEEPING)) {
         Direction direction = pEntityLiving.getBedOrientation();
         float f1 = direction != null ? sleepDirectionToRotation(direction) : pRotationYaw;
         pPoseStack.mulPose(Axis.YP.rotationDegrees(f1));
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(this.getFlipDegrees(pEntityLiving)));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
      } else if (isEntityUpsideDown(pEntityLiving)) {
         pPoseStack.translate(0.0F, pEntityLiving.getBbHeight() + 0.1F, 0.0F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      }

   }

   /**
    * Returns where in the swing animation the living entity is (from 0 to 1).  Args : entity, partialTickTime
    */
   protected float getAttackAnim(T pLivingBase, float pPartialTickTime) {
      return pLivingBase.getAttackAnim(pPartialTickTime);
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   protected float getBob(T pLivingBase, float pPartialTick) {
      return (float)pLivingBase.tickCount + pPartialTick;
   }

   protected float getFlipDegrees(T pLivingEntity) {
      return 90.0F;
   }

   protected float getWhiteOverlayProgress(T pLivingEntity, float pPartialTicks) {
      return 0.0F;
   }

   protected void scale(T pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
   }

   protected boolean shouldShowName(T pEntity) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
      float f = pEntity.isDiscrete() ? 32.0F : 64.0F;
      if (d0 >= (double)(f * f)) {
         return false;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer localplayer = minecraft.player;
         boolean flag = !pEntity.isInvisibleTo(localplayer);
         if (pEntity != localplayer) {
            Team team = pEntity.getTeam();
            Team team1 = localplayer.getTeam();
            if (team != null) {
               Team.Visibility team$visibility = team.getNameTagVisibility();
               switch (team$visibility) {
                  case ALWAYS:
                     return flag;
                  case NEVER:
                     return false;
                  case HIDE_FOR_OTHER_TEAMS:
                     return team1 == null ? flag : team.isAlliedTo(team1) && (team.canSeeFriendlyInvisibles() || flag);
                  case HIDE_FOR_OWN_TEAM:
                     return team1 == null ? flag : !team.isAlliedTo(team1) && flag;
                  default:
                     return true;
               }
            }
         }

         return Minecraft.renderNames() && pEntity != minecraft.getCameraEntity() && flag && !pEntity.isVehicle();
      }
   }

   public static boolean isEntityUpsideDown(LivingEntity pEntity) {
      if (pEntity instanceof Player || pEntity.hasCustomName()) {
         String s = ChatFormatting.stripFormatting(pEntity.getName().getString());
         if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
            return !(pEntity instanceof Player) || ((Player)pEntity).isModelPartShown(PlayerModelPart.CAPE);
         }
      }

      return false;
   }
}
