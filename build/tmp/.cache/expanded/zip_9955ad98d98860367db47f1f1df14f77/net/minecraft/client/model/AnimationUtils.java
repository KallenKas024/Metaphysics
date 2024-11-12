package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationUtils {
   public static void animateCrossbowHold(ModelPart pRightArm, ModelPart pLeftArm, ModelPart pHead, boolean pRightHanded) {
      ModelPart modelpart = pRightHanded ? pRightArm : pLeftArm;
      ModelPart modelpart1 = pRightHanded ? pLeftArm : pRightArm;
      modelpart.yRot = (pRightHanded ? -0.3F : 0.3F) + pHead.yRot;
      modelpart1.yRot = (pRightHanded ? 0.6F : -0.6F) + pHead.yRot;
      modelpart.xRot = (-(float)Math.PI / 2F) + pHead.xRot + 0.1F;
      modelpart1.xRot = -1.5F + pHead.xRot;
   }

   public static void animateCrossbowCharge(ModelPart pRightArm, ModelPart pLeftArm, LivingEntity pLivingEntity, boolean pRightHanded) {
      ModelPart modelpart = pRightHanded ? pRightArm : pLeftArm;
      ModelPart modelpart1 = pRightHanded ? pLeftArm : pRightArm;
      modelpart.yRot = pRightHanded ? -0.8F : 0.8F;
      modelpart.xRot = -0.97079635F;
      modelpart1.xRot = modelpart.xRot;
      float f = (float)CrossbowItem.getChargeDuration(pLivingEntity.getUseItem());
      float f1 = Mth.clamp((float)pLivingEntity.getTicksUsingItem(), 0.0F, f);
      float f2 = f1 / f;
      modelpart1.yRot = Mth.lerp(f2, 0.4F, 0.85F) * (float)(pRightHanded ? 1 : -1);
      modelpart1.xRot = Mth.lerp(f2, modelpart1.xRot, (-(float)Math.PI / 2F));
   }

   public static <T extends Mob> void swingWeaponDown(ModelPart pRightArm, ModelPart pLeftArm, T pMob, float pAttackTime, float pAgeInTicks) {
      float f = Mth.sin(pAttackTime * (float)Math.PI);
      float f1 = Mth.sin((1.0F - (1.0F - pAttackTime) * (1.0F - pAttackTime)) * (float)Math.PI);
      pRightArm.zRot = 0.0F;
      pLeftArm.zRot = 0.0F;
      pRightArm.yRot = 0.15707964F;
      pLeftArm.yRot = -0.15707964F;
      if (pMob.getMainArm() == HumanoidArm.RIGHT) {
         pRightArm.xRot = -1.8849558F + Mth.cos(pAgeInTicks * 0.09F) * 0.15F;
         pLeftArm.xRot = -0.0F + Mth.cos(pAgeInTicks * 0.19F) * 0.5F;
         pRightArm.xRot += f * 2.2F - f1 * 0.4F;
         pLeftArm.xRot += f * 1.2F - f1 * 0.4F;
      } else {
         pRightArm.xRot = -0.0F + Mth.cos(pAgeInTicks * 0.19F) * 0.5F;
         pLeftArm.xRot = -1.8849558F + Mth.cos(pAgeInTicks * 0.09F) * 0.15F;
         pRightArm.xRot += f * 1.2F - f1 * 0.4F;
         pLeftArm.xRot += f * 2.2F - f1 * 0.4F;
      }

      bobArms(pRightArm, pLeftArm, pAgeInTicks);
   }

   public static void bobModelPart(ModelPart pModelPart, float pAgeInTicks, float pMultiplier) {
      pModelPart.zRot += pMultiplier * (Mth.cos(pAgeInTicks * 0.09F) * 0.05F + 0.05F);
      pModelPart.xRot += pMultiplier * Mth.sin(pAgeInTicks * 0.067F) * 0.05F;
   }

   public static void bobArms(ModelPart pRightArm, ModelPart pLeftArm, float pAgeInTicks) {
      bobModelPart(pRightArm, pAgeInTicks, 1.0F);
      bobModelPart(pLeftArm, pAgeInTicks, -1.0F);
   }

   public static void animateZombieArms(ModelPart pLeftArm, ModelPart pRightArm, boolean pIsAggressive, float pAttackTime, float pAgeInTicks) {
      float f = Mth.sin(pAttackTime * (float)Math.PI);
      float f1 = Mth.sin((1.0F - (1.0F - pAttackTime) * (1.0F - pAttackTime)) * (float)Math.PI);
      pRightArm.zRot = 0.0F;
      pLeftArm.zRot = 0.0F;
      pRightArm.yRot = -(0.1F - f * 0.6F);
      pLeftArm.yRot = 0.1F - f * 0.6F;
      float f2 = -(float)Math.PI / (pIsAggressive ? 1.5F : 2.25F);
      pRightArm.xRot = f2;
      pLeftArm.xRot = f2;
      pRightArm.xRot += f * 1.2F - f1 * 0.4F;
      pLeftArm.xRot += f * 1.2F - f1 * 0.4F;
      bobArms(pRightArm, pLeftArm, pAgeInTicks);
   }
}