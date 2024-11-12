package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonModel<T extends Mob & RangedAttackMob> extends HumanoidModel<T> {
   public SkeletonModel(ModelPart pRoot) {
      super(pRoot);
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(-2.0F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F), PartPose.offset(2.0F, 12.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
      this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
      ItemStack itemstack = pEntity.getItemInHand(InteractionHand.MAIN_HAND);
      if (itemstack.is(Items.BOW) && pEntity.isAggressive()) {
         if (pEntity.getMainArm() == HumanoidArm.RIGHT) {
            this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
         } else {
            this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
         }
      }

      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      ItemStack itemstack = pEntity.getMainHandItem();
      if (pEntity.isAggressive() && (itemstack.isEmpty() || !itemstack.is(Items.BOW))) {
         float f = Mth.sin(this.attackTime * (float)Math.PI);
         float f1 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float)Math.PI);
         this.rightArm.zRot = 0.0F;
         this.leftArm.zRot = 0.0F;
         this.rightArm.yRot = -(0.1F - f * 0.6F);
         this.leftArm.yRot = 0.1F - f * 0.6F;
         this.rightArm.xRot = (-(float)Math.PI / 2F);
         this.leftArm.xRot = (-(float)Math.PI / 2F);
         this.rightArm.xRot -= f * 1.2F - f1 * 0.4F;
         this.leftArm.xRot -= f * 1.2F - f1 * 0.4F;
         AnimationUtils.bobArms(this.rightArm, this.leftArm, pAgeInTicks);
      }

   }

   public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
      float f = pSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      ModelPart modelpart = this.getArm(pSide);
      modelpart.x += f;
      modelpart.translateAndRotate(pPoseStack);
      modelpart.x -= f;
   }
}