package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemModel<T extends IronGolem> extends HierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart rightArm;
   private final ModelPart leftArm;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;

   public IronGolemModel(ModelPart pRoot) {
      this.root = pRoot;
      this.head = pRoot.getChild("head");
      this.rightArm = pRoot.getChild("right_arm");
      this.leftArm = pRoot.getChild("left_arm");
      this.rightLeg = pRoot.getChild("right_leg");
      this.leftLeg = pRoot.getChild("left_leg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 10.0F, 8.0F).texOffs(24, 0).addBox(-1.0F, -5.0F, -7.5F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -7.0F, -2.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18.0F, 12.0F, 11.0F).texOffs(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9.0F, 5.0F, 6.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, -7.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 58).addBox(9.0F, -2.5F, -3.0F, 4.0F, 30.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(-4.0F, 11.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(60, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 6.0F, 16.0F, 5.0F), PartPose.offset(5.0F, 11.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 128, 128);
   }

   public ModelPart root() {
      return this.root;
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      this.rightLeg.xRot = -1.5F * Mth.triangleWave(pLimbSwing, 13.0F) * pLimbSwingAmount;
      this.leftLeg.xRot = 1.5F * Mth.triangleWave(pLimbSwing, 13.0F) * pLimbSwingAmount;
      this.rightLeg.yRot = 0.0F;
      this.leftLeg.yRot = 0.0F;
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      int i = pEntity.getAttackAnimationTick();
      if (i > 0) {
         this.rightArm.xRot = -2.0F + 1.5F * Mth.triangleWave((float)i - pPartialTick, 10.0F);
         this.leftArm.xRot = -2.0F + 1.5F * Mth.triangleWave((float)i - pPartialTick, 10.0F);
      } else {
         int j = pEntity.getOfferFlowerTick();
         if (j > 0) {
            this.rightArm.xRot = -0.8F + 0.025F * Mth.triangleWave((float)j, 70.0F);
            this.leftArm.xRot = 0.0F;
         } else {
            this.rightArm.xRot = (-0.2F + 1.5F * Mth.triangleWave(pLimbSwing, 13.0F)) * pLimbSwingAmount;
            this.leftArm.xRot = (-0.2F - 1.5F * Mth.triangleWave(pLimbSwing, 13.0F)) * pLimbSwingAmount;
         }
      }

   }

   public ModelPart getFlowerHoldingArm() {
      return this.rightArm;
   }
}