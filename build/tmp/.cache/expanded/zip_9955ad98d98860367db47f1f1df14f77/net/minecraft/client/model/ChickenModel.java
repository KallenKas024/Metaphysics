package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenModel<T extends Entity> extends AgeableListModel<T> {
   public static final String RED_THING = "red_thing";
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightLeg;
   private final ModelPart leftLeg;
   private final ModelPart rightWing;
   private final ModelPart leftWing;
   private final ModelPart beak;
   private final ModelPart redThing;

   public ChickenModel(ModelPart pRoot) {
      this.head = pRoot.getChild("head");
      this.beak = pRoot.getChild("beak");
      this.redThing = pRoot.getChild("red_thing");
      this.body = pRoot.getChild("body");
      this.rightLeg = pRoot.getChild("right_leg");
      this.leftLeg = pRoot.getChild("left_leg");
      this.rightWing = pRoot.getChild("right_wing");
      this.leftWing = pRoot.getChild("left_wing");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      int i = 16;
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 3.0F), PartPose.offset(0.0F, 15.0F, -4.0F));
      partdefinition.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(14, 0).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 15.0F, -4.0F));
      partdefinition.addOrReplaceChild("red_thing", CubeListBuilder.create().texOffs(14, 4).addBox(-1.0F, -2.0F, -3.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 15.0F, -4.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F);
      partdefinition.addOrReplaceChild("right_leg", cubelistbuilder, PartPose.offset(-2.0F, 19.0F, 1.0F));
      partdefinition.addOrReplaceChild("left_leg", cubelistbuilder, PartPose.offset(1.0F, 19.0F, 1.0F));
      partdefinition.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(24, 13).addBox(0.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(-4.0F, 13.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(24, 13).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 4.0F, 6.0F), PartPose.offset(4.0F, 13.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   protected Iterable<ModelPart> headParts() {
      return ImmutableList.of(this.head, this.beak, this.redThing);
   }

   protected Iterable<ModelPart> bodyParts() {
      return ImmutableList.of(this.body, this.rightLeg, this.leftLeg, this.rightWing, this.leftWing);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.beak.xRot = this.head.xRot;
      this.beak.yRot = this.head.yRot;
      this.redThing.xRot = this.head.xRot;
      this.redThing.yRot = this.head.yRot;
      this.rightLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
      this.leftLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.rightWing.zRot = pAgeInTicks;
      this.leftWing.zRot = -pAgeInTicks;
   }
}