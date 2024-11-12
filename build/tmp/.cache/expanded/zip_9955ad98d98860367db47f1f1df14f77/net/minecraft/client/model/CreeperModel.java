package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperModel<T extends Entity> extends HierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart head;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private static final int Y_OFFSET = 6;

   public CreeperModel(ModelPart pRoot) {
      this.root = pRoot;
      this.head = pRoot.getChild("head");
      this.leftHindLeg = pRoot.getChild("right_hind_leg");
      this.rightHindLeg = pRoot.getChild("left_hind_leg");
      this.leftFrontLeg = pRoot.getChild("right_front_leg");
      this.rightFrontLeg = pRoot.getChild("left_front_leg");
   }

   public static LayerDefinition createBodyLayer(CubeDeformation pCubeDeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, pCubeDeformation), PartPose.offset(0.0F, 6.0F, 0.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, pCubeDeformation), PartPose.offset(0.0F, 6.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, pCubeDeformation);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-2.0F, 18.0F, 4.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(2.0F, 18.0F, 4.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-2.0F, 18.0F, -4.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(2.0F, 18.0F, -4.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
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
      this.rightHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
      this.leftHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.rightFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.leftFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
   }
}