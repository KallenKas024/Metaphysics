package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Panda;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaModel<T extends Panda> extends QuadrupedModel<T> {
   private float sitAmount;
   private float lieOnBackAmount;
   private float rollAmount;

   public PandaModel(ModelPart pRoot) {
      super(pRoot, true, 23.0F, 4.8F, 2.7F, 3.0F, 49);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 6).addBox(-6.5F, -5.0F, -4.0F, 13.0F, 10.0F, 9.0F).texOffs(45, 16).addBox("nose", -3.5F, 0.0F, -6.0F, 7.0F, 5.0F, 2.0F).texOffs(52, 25).addBox("left_ear", 3.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F).texOffs(52, 25).addBox("right_ear", -8.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F), PartPose.offset(0.0F, 11.5F, -17.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 25).addBox(-9.5F, -13.0F, -6.5F, 19.0F, 26.0F, 13.0F), PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      int i = 9;
      int j = 6;
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(40, 0).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-5.5F, 15.0F, 9.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(5.5F, 15.0F, 9.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-5.5F, 15.0F, -9.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(5.5F, 15.0F, -9.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
      this.sitAmount = pEntity.getSitAmount(pPartialTick);
      this.lieOnBackAmount = pEntity.getLieOnBackAmount(pPartialTick);
      this.rollAmount = pEntity.isBaby() ? 0.0F : pEntity.getRollAmount(pPartialTick);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      boolean flag = pEntity.getUnhappyCounter() > 0;
      boolean flag1 = pEntity.isSneezing();
      int i = pEntity.getSneezeCounter();
      boolean flag2 = pEntity.isEating();
      boolean flag3 = pEntity.isScared();
      if (flag) {
         this.head.yRot = 0.35F * Mth.sin(0.6F * pAgeInTicks);
         this.head.zRot = 0.35F * Mth.sin(0.6F * pAgeInTicks);
         this.rightFrontLeg.xRot = -0.75F * Mth.sin(0.3F * pAgeInTicks);
         this.leftFrontLeg.xRot = 0.75F * Mth.sin(0.3F * pAgeInTicks);
      } else {
         this.head.zRot = 0.0F;
      }

      if (flag1) {
         if (i < 15) {
            this.head.xRot = (-(float)Math.PI / 4F) * (float)i / 14.0F;
         } else if (i < 20) {
            float f = (float)((i - 15) / 5);
            this.head.xRot = (-(float)Math.PI / 4F) + ((float)Math.PI / 4F) * f;
         }
      }

      if (this.sitAmount > 0.0F) {
         this.body.xRot = ModelUtils.rotlerpRad(this.body.xRot, 1.7407963F, this.sitAmount);
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, ((float)Math.PI / 2F), this.sitAmount);
         this.rightFrontLeg.zRot = -0.27079642F;
         this.leftFrontLeg.zRot = 0.27079642F;
         this.rightHindLeg.zRot = 0.5707964F;
         this.leftHindLeg.zRot = -0.5707964F;
         if (flag2) {
            this.head.xRot = ((float)Math.PI / 2F) + 0.2F * Mth.sin(pAgeInTicks * 0.6F);
            this.rightFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(pAgeInTicks * 0.6F);
            this.leftFrontLeg.xRot = -0.4F - 0.2F * Mth.sin(pAgeInTicks * 0.6F);
         }

         if (flag3) {
            this.head.xRot = 2.1707964F;
            this.rightFrontLeg.xRot = -0.9F;
            this.leftFrontLeg.xRot = -0.9F;
         }
      } else {
         this.rightHindLeg.zRot = 0.0F;
         this.leftHindLeg.zRot = 0.0F;
         this.rightFrontLeg.zRot = 0.0F;
         this.leftFrontLeg.zRot = 0.0F;
      }

      if (this.lieOnBackAmount > 0.0F) {
         this.rightHindLeg.xRot = -0.6F * Mth.sin(pAgeInTicks * 0.15F);
         this.leftHindLeg.xRot = 0.6F * Mth.sin(pAgeInTicks * 0.15F);
         this.rightFrontLeg.xRot = 0.3F * Mth.sin(pAgeInTicks * 0.25F);
         this.leftFrontLeg.xRot = -0.3F * Mth.sin(pAgeInTicks * 0.25F);
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, ((float)Math.PI / 2F), this.lieOnBackAmount);
      }

      if (this.rollAmount > 0.0F) {
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 2.0561945F, this.rollAmount);
         this.rightHindLeg.xRot = -0.5F * Mth.sin(pAgeInTicks * 0.5F);
         this.leftHindLeg.xRot = 0.5F * Mth.sin(pAgeInTicks * 0.5F);
         this.rightFrontLeg.xRot = 0.5F * Mth.sin(pAgeInTicks * 0.5F);
         this.leftFrontLeg.xRot = -0.5F * Mth.sin(pAgeInTicks * 0.5F);
      }

   }
}