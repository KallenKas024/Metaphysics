package net.minecraft.client.model;

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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedModel<T extends Zombie> extends ZombieModel<T> {
   public DrownedModel(ModelPart pRoot) {
      super(pRoot);
   }

   public static LayerDefinition createBodyLayer(CubeDeformation pCubeDeformation) {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(pCubeDeformation, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(5.0F, 2.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(1.9F, 12.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
      this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
      ItemStack itemstack = pEntity.getItemInHand(InteractionHand.MAIN_HAND);
      if (itemstack.is(Items.TRIDENT) && pEntity.isAggressive()) {
         if (pEntity.getMainArm() == HumanoidArm.RIGHT) {
            this.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
         } else {
            this.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
         }
      }

      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
         this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
         this.leftArm.yRot = 0.0F;
      }

      if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
         this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
         this.rightArm.yRot = 0.0F;
      }

      if (this.swimAmount > 0.0F) {
         this.rightArm.xRot = this.rotlerpRad(this.swimAmount, this.rightArm.xRot, -2.5132742F) + this.swimAmount * 0.35F * Mth.sin(0.1F * pAgeInTicks);
         this.leftArm.xRot = this.rotlerpRad(this.swimAmount, this.leftArm.xRot, -2.5132742F) - this.swimAmount * 0.35F * Mth.sin(0.1F * pAgeInTicks);
         this.rightArm.zRot = this.rotlerpRad(this.swimAmount, this.rightArm.zRot, -0.15F);
         this.leftArm.zRot = this.rotlerpRad(this.swimAmount, this.leftArm.zRot, 0.15F);
         this.leftLeg.xRot -= this.swimAmount * 0.55F * Mth.sin(0.1F * pAgeInTicks);
         this.rightLeg.xRot += this.swimAmount * 0.55F * Mth.sin(0.1F * pAgeInTicks);
         this.head.xRot = 0.0F;
      }

   }
}