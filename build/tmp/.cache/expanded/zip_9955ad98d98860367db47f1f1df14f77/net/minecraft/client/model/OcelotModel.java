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
public class OcelotModel<T extends Entity> extends AgeableListModel<T> {
   private static final int CROUCH_STATE = 0;
   private static final int WALK_STATE = 1;
   private static final int SPRINT_STATE = 2;
   protected static final int SITTING_STATE = 3;
   private static final float XO = 0.0F;
   private static final float YO = 16.0F;
   private static final float ZO = -9.0F;
   private static final float HEAD_WALK_Y = 15.0F;
   private static final float HEAD_WALK_Z = -9.0F;
   private static final float BODY_WALK_Y = 12.0F;
   private static final float BODY_WALK_Z = -10.0F;
   private static final float TAIL_1_WALK_Y = 15.0F;
   private static final float TAIL_1_WALK_Z = 8.0F;
   private static final float TAIL_2_WALK_Y = 20.0F;
   private static final float TAIL_2_WALK_Z = 14.0F;
   protected static final float BACK_LEG_Y = 18.0F;
   protected static final float BACK_LEG_Z = 5.0F;
   protected static final float FRONT_LEG_Y = 14.1F;
   private static final float FRONT_LEG_Z = -5.0F;
   private static final String TAIL_1 = "tail1";
   private static final String TAIL_2 = "tail2";
   protected final ModelPart leftHindLeg;
   protected final ModelPart rightHindLeg;
   protected final ModelPart leftFrontLeg;
   protected final ModelPart rightFrontLeg;
   protected final ModelPart tail1;
   protected final ModelPart tail2;
   protected final ModelPart head;
   protected final ModelPart body;
   protected int state = 1;

   public OcelotModel(ModelPart pRoot) {
      super(true, 10.0F, 4.0F);
      this.head = pRoot.getChild("head");
      this.body = pRoot.getChild("body");
      this.tail1 = pRoot.getChild("tail1");
      this.tail2 = pRoot.getChild("tail2");
      this.leftHindLeg = pRoot.getChild("left_hind_leg");
      this.rightHindLeg = pRoot.getChild("right_hind_leg");
      this.leftFrontLeg = pRoot.getChild("left_front_leg");
      this.rightFrontLeg = pRoot.getChild("right_front_leg");
   }

   public static MeshDefinition createBodyMesh(CubeDeformation pCubeDeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      CubeDeformation cubedeformation = new CubeDeformation(-0.02F);
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("main", -2.5F, -2.0F, -3.0F, 5.0F, 4.0F, 5.0F, pCubeDeformation).addBox("nose", -1.5F, -0.001F, -4.0F, 3, 2, 2, pCubeDeformation, 0, 24).addBox("ear1", -2.0F, -3.0F, 0.0F, 1, 1, 2, pCubeDeformation, 0, 10).addBox("ear2", 1.0F, -3.0F, 0.0F, 1, 1, 2, pCubeDeformation, 6, 10), PartPose.offset(0.0F, 15.0F, -9.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(20, 0).addBox(-2.0F, 3.0F, -8.0F, 4.0F, 16.0F, 6.0F, pCubeDeformation), PartPose.offsetAndRotation(0.0F, 12.0F, -10.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("tail1", CubeListBuilder.create().texOffs(0, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, pCubeDeformation), PartPose.offsetAndRotation(0.0F, 15.0F, 8.0F, 0.9F, 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(4, 15).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, cubedeformation), PartPose.offset(0.0F, 20.0F, 14.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(8, 13).addBox(-1.0F, 0.0F, 1.0F, 2.0F, 6.0F, 2.0F, pCubeDeformation);
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(1.1F, 18.0F, 5.0F));
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-1.1F, 18.0F, 5.0F));
      CubeListBuilder cubelistbuilder1 = CubeListBuilder.create().texOffs(40, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 10.0F, 2.0F, pCubeDeformation);
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder1, PartPose.offset(1.2F, 14.1F, -5.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder1, PartPose.offset(-1.2F, 14.1F, -5.0F));
      return meshdefinition;
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.leftHindLeg, this.rightHindLeg, this.leftFrontLeg, this.rightFrontLeg, this.tail1, this.tail2);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      if (this.state != 3) {
         this.body.xRot = ((float)Math.PI / 2F);
         if (this.state == 2) {
            this.leftHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * pLimbSwingAmount;
            this.rightHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + 0.3F) * pLimbSwingAmount;
            this.leftFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI + 0.3F) * pLimbSwingAmount;
            this.rightFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * pLimbSwingAmount;
            this.tail2.xRot = 1.7278761F + ((float)Math.PI / 10F) * Mth.cos(pLimbSwing) * pLimbSwingAmount;
         } else {
            this.leftHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * pLimbSwingAmount;
            this.rightHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * pLimbSwingAmount;
            this.leftFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * pLimbSwingAmount;
            this.rightFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * pLimbSwingAmount;
            if (this.state == 1) {
               this.tail2.xRot = 1.7278761F + ((float)Math.PI / 4F) * Mth.cos(pLimbSwing) * pLimbSwingAmount;
            } else {
               this.tail2.xRot = 1.7278761F + 0.47123894F * Mth.cos(pLimbSwing) * pLimbSwingAmount;
            }
         }
      }

   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.body.y = 12.0F;
      this.body.z = -10.0F;
      this.head.y = 15.0F;
      this.head.z = -9.0F;
      this.tail1.y = 15.0F;
      this.tail1.z = 8.0F;
      this.tail2.y = 20.0F;
      this.tail2.z = 14.0F;
      this.leftFrontLeg.y = 14.1F;
      this.leftFrontLeg.z = -5.0F;
      this.rightFrontLeg.y = 14.1F;
      this.rightFrontLeg.z = -5.0F;
      this.leftHindLeg.y = 18.0F;
      this.leftHindLeg.z = 5.0F;
      this.rightHindLeg.y = 18.0F;
      this.rightHindLeg.z = 5.0F;
      this.tail1.xRot = 0.9F;
      if (pEntity.isCrouching()) {
         ++this.body.y;
         this.head.y += 2.0F;
         ++this.tail1.y;
         this.tail2.y += -4.0F;
         this.tail2.z += 2.0F;
         this.tail1.xRot = ((float)Math.PI / 2F);
         this.tail2.xRot = ((float)Math.PI / 2F);
         this.state = 0;
      } else if (pEntity.isSprinting()) {
         this.tail2.y = this.tail1.y;
         this.tail2.z += 2.0F;
         this.tail1.xRot = ((float)Math.PI / 2F);
         this.tail2.xRot = ((float)Math.PI / 2F);
         this.state = 2;
      } else {
         this.state = 1;
      }

   }
}