package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class QuadrupedModel<T extends Entity> extends AgeableListModel<T> {
   protected final ModelPart head;
   protected final ModelPart body;
   protected final ModelPart rightHindLeg;
   protected final ModelPart leftHindLeg;
   protected final ModelPart rightFrontLeg;
   protected final ModelPart leftFrontLeg;

   protected QuadrupedModel(ModelPart pRoot, boolean pScaleHead, float pBabyYHeadOffset, float pBabyZHeadOffset, float pBabyHeadScale, float pBabyBodyScale, int pBodyYOffset) {
      super(pScaleHead, pBabyYHeadOffset, pBabyZHeadOffset, pBabyHeadScale, pBabyBodyScale, (float)pBodyYOffset);
      this.head = pRoot.getChild("head");
      this.body = pRoot.getChild("body");
      this.rightHindLeg = pRoot.getChild("right_hind_leg");
      this.leftHindLeg = pRoot.getChild("left_hind_leg");
      this.rightFrontLeg = pRoot.getChild("right_front_leg");
      this.leftFrontLeg = pRoot.getChild("left_front_leg");
   }

   public static MeshDefinition createBodyMesh(int pYOffset, CubeDeformation pCubeDeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -8.0F, 8.0F, 8.0F, 8.0F, pCubeDeformation), PartPose.offset(0.0F, (float)(18 - pYOffset), -6.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5.0F, -10.0F, -7.0F, 10.0F, 16.0F, 8.0F, pCubeDeformation), PartPose.offsetAndRotation(0.0F, (float)(17 - pYOffset), 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, (float)pYOffset, 4.0F, pCubeDeformation);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-3.0F, (float)(24 - pYOffset), 7.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(3.0F, (float)(24 - pYOffset), 7.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-3.0F, (float)(24 - pYOffset), -5.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(3.0F, (float)(24 - pYOffset), -5.0F));
      return meshdefinition;
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.rightHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
      this.leftHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.rightFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.leftFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
   }
}