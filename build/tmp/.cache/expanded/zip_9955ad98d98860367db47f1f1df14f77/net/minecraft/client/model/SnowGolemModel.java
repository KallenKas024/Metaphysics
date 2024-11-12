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
public class SnowGolemModel<T extends Entity> extends HierarchicalModel<T> {
   private static final String UPPER_BODY = "upper_body";
   private final ModelPart root;
   private final ModelPart upperBody;
   private final ModelPart head;
   private final ModelPart leftArm;
   private final ModelPart rightArm;

   public SnowGolemModel(ModelPart pRoot) {
      this.root = pRoot;
      this.head = pRoot.getChild("head");
      this.leftArm = pRoot.getChild("left_arm");
      this.rightArm = pRoot.getChild("right_arm");
      this.upperBody = pRoot.getChild("upper_body");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      float f = 4.0F;
      CubeDeformation cubedeformation = new CubeDeformation(-0.5F);
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, cubedeformation), PartPose.offset(0.0F, 4.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(32, 0).addBox(-1.0F, 0.0F, -1.0F, 12.0F, 2.0F, 2.0F, cubedeformation);
      partdefinition.addOrReplaceChild("left_arm", cubelistbuilder, PartPose.offsetAndRotation(5.0F, 6.0F, 1.0F, 0.0F, 0.0F, 1.0F));
      partdefinition.addOrReplaceChild("right_arm", cubelistbuilder, PartPose.offsetAndRotation(-5.0F, 6.0F, -1.0F, 0.0F, (float)Math.PI, -1.0F));
      partdefinition.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, cubedeformation), PartPose.offset(0.0F, 13.0F, 0.0F));
      partdefinition.addOrReplaceChild("lower_body", CubeListBuilder.create().texOffs(0, 36).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 12.0F, 12.0F, cubedeformation), PartPose.offset(0.0F, 24.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      this.upperBody.yRot = pNetHeadYaw * ((float)Math.PI / 180F) * 0.25F;
      float f = Mth.sin(this.upperBody.yRot);
      float f1 = Mth.cos(this.upperBody.yRot);
      this.leftArm.yRot = this.upperBody.yRot;
      this.rightArm.yRot = this.upperBody.yRot + (float)Math.PI;
      this.leftArm.x = f1 * 5.0F;
      this.leftArm.z = -f * 5.0F;
      this.rightArm.x = -f1 * 5.0F;
      this.rightArm.z = f * 5.0F;
   }

   public ModelPart root() {
      return this.root;
   }

   public ModelPart getHead() {
      return this.head;
   }
}