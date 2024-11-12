package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.PandaHoldsItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaRenderer extends MobRenderer<Panda, PandaModel<Panda>> {
   private static final Map<Panda.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(Panda.Gene.class), (p_115647_) -> {
      p_115647_.put(Panda.Gene.NORMAL, new ResourceLocation("textures/entity/panda/panda.png"));
      p_115647_.put(Panda.Gene.LAZY, new ResourceLocation("textures/entity/panda/lazy_panda.png"));
      p_115647_.put(Panda.Gene.WORRIED, new ResourceLocation("textures/entity/panda/worried_panda.png"));
      p_115647_.put(Panda.Gene.PLAYFUL, new ResourceLocation("textures/entity/panda/playful_panda.png"));
      p_115647_.put(Panda.Gene.BROWN, new ResourceLocation("textures/entity/panda/brown_panda.png"));
      p_115647_.put(Panda.Gene.WEAK, new ResourceLocation("textures/entity/panda/weak_panda.png"));
      p_115647_.put(Panda.Gene.AGGRESSIVE, new ResourceLocation("textures/entity/panda/aggressive_panda.png"));
   });

   public PandaRenderer(EntityRendererProvider.Context p_174334_) {
      super(p_174334_, new PandaModel<>(p_174334_.bakeLayer(ModelLayers.PANDA)), 0.9F);
      this.addLayer(new PandaHoldsItemLayer(this, p_174334_.getItemInHandRenderer()));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Panda pEntity) {
      return TEXTURES.getOrDefault(pEntity.getVariant(), TEXTURES.get(Panda.Gene.NORMAL));
   }

   protected void setupRotations(Panda pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      if (pEntityLiving.rollCounter > 0) {
         int i = pEntityLiving.rollCounter;
         int j = i + 1;
         float f = 7.0F;
         float f1 = pEntityLiving.isBaby() ? 0.3F : 0.8F;
         if (i < 8) {
            float f3 = (float)(90 * i) / 7.0F;
            float f4 = (float)(90 * j) / 7.0F;
            float f2 = this.getAngle(f3, f4, j, pPartialTicks, 8.0F);
            pPoseStack.translate(0.0F, (f1 + 0.2F) * (f2 / 90.0F), 0.0F);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-f2));
         } else if (i < 16) {
            float f13 = ((float)i - 8.0F) / 7.0F;
            float f16 = 90.0F + 90.0F * f13;
            float f5 = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
            float f10 = this.getAngle(f16, f5, j, pPartialTicks, 16.0F);
            pPoseStack.translate(0.0F, f1 + 0.2F + (f1 - 0.2F) * (f10 - 90.0F) / 90.0F, 0.0F);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-f10));
         } else if ((float)i < 24.0F) {
            float f14 = ((float)i - 16.0F) / 7.0F;
            float f17 = 180.0F + 90.0F * f14;
            float f19 = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
            float f11 = this.getAngle(f17, f19, j, pPartialTicks, 24.0F);
            pPoseStack.translate(0.0F, f1 + f1 * (270.0F - f11) / 90.0F, 0.0F);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-f11));
         } else if (i < 32) {
            float f15 = ((float)i - 24.0F) / 7.0F;
            float f18 = 270.0F + 90.0F * f15;
            float f20 = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
            float f12 = this.getAngle(f18, f20, j, pPartialTicks, 32.0F);
            pPoseStack.translate(0.0F, f1 * ((360.0F - f12) / 90.0F), 0.0F);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-f12));
         }
      }

      float f6 = pEntityLiving.getSitAmount(pPartialTicks);
      if (f6 > 0.0F) {
         pPoseStack.translate(0.0F, 0.8F * f6, 0.0F);
         pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f6, pEntityLiving.getXRot(), pEntityLiving.getXRot() + 90.0F)));
         pPoseStack.translate(0.0F, -1.0F * f6, 0.0F);
         if (pEntityLiving.isScared()) {
            float f7 = (float)(Math.cos((double)pEntityLiving.tickCount * 1.25D) * Math.PI * (double)0.05F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(f7));
            if (pEntityLiving.isBaby()) {
               pPoseStack.translate(0.0F, 0.8F, 0.55F);
            }
         }
      }

      float f8 = pEntityLiving.getLieOnBackAmount(pPartialTicks);
      if (f8 > 0.0F) {
         float f9 = pEntityLiving.isBaby() ? 0.5F : 1.3F;
         pPoseStack.translate(0.0F, f9 * f8, 0.0F);
         pPoseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(f8, pEntityLiving.getXRot(), pEntityLiving.getXRot() + 180.0F)));
      }

   }

   private float getAngle(float pCurrentAngle, float pNextAngle, int pNextRollCounter, float pPartialTick, float pRollEndCount) {
      return (float)pNextRollCounter < pRollEndCount ? Mth.lerp(pPartialTick, pCurrentAngle, pNextAngle) : pCurrentAngle;
   }
}