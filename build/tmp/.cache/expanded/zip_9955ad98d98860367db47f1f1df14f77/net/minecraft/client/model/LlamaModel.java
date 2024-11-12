package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaModel<T extends AbstractChestedHorse> extends EntityModel<T> {
   private final ModelPart head;
   private final ModelPart body;
   private final ModelPart rightHindLeg;
   private final ModelPart leftHindLeg;
   private final ModelPart rightFrontLeg;
   private final ModelPart leftFrontLeg;
   private final ModelPart rightChest;
   private final ModelPart leftChest;

   public LlamaModel(ModelPart pRoot) {
      this.head = pRoot.getChild("head");
      this.body = pRoot.getChild("body");
      this.rightChest = pRoot.getChild("right_chest");
      this.leftChest = pRoot.getChild("left_chest");
      this.rightHindLeg = pRoot.getChild("right_hind_leg");
      this.leftHindLeg = pRoot.getChild("left_hind_leg");
      this.rightFrontLeg = pRoot.getChild("right_front_leg");
      this.leftFrontLeg = pRoot.getChild("left_front_leg");
   }

   public static LayerDefinition createBodyLayer(CubeDeformation pCubeDeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -14.0F, -10.0F, 4.0F, 4.0F, 9.0F, pCubeDeformation).texOffs(0, 14).addBox("neck", -4.0F, -16.0F, -6.0F, 8.0F, 18.0F, 6.0F, pCubeDeformation).texOffs(17, 0).addBox("ear", -4.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, pCubeDeformation).texOffs(17, 0).addBox("ear", 1.0F, -19.0F, -4.0F, 3.0F, 3.0F, 2.0F, pCubeDeformation), PartPose.offset(0.0F, 7.0F, -6.0F));
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 0).addBox(-6.0F, -10.0F, -7.0F, 12.0F, 18.0F, 10.0F, pCubeDeformation), PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, ((float)Math.PI / 2F), 0.0F, 0.0F));
      partdefinition.addOrReplaceChild("right_chest", CubeListBuilder.create().texOffs(45, 28).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, pCubeDeformation), PartPose.offsetAndRotation(-8.5F, 3.0F, 3.0F, 0.0F, ((float)Math.PI / 2F), 0.0F));
      partdefinition.addOrReplaceChild("left_chest", CubeListBuilder.create().texOffs(45, 41).addBox(-3.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F, pCubeDeformation), PartPose.offsetAndRotation(5.5F, 3.0F, 3.0F, 0.0F, ((float)Math.PI / 2F), 0.0F));
      int i = 4;
      int j = 14;
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(29, 29).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F, pCubeDeformation);
      partdefinition.addOrReplaceChild("right_hind_leg", cubelistbuilder, PartPose.offset(-3.5F, 10.0F, 6.0F));
      partdefinition.addOrReplaceChild("left_hind_leg", cubelistbuilder, PartPose.offset(3.5F, 10.0F, 6.0F));
      partdefinition.addOrReplaceChild("right_front_leg", cubelistbuilder, PartPose.offset(-3.5F, 10.0F, -5.0F));
      partdefinition.addOrReplaceChild("left_front_leg", cubelistbuilder, PartPose.offset(3.5F, 10.0F, -5.0F));
      return LayerDefinition.create(meshdefinition, 128, 64);
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
      boolean flag = !pEntity.isBaby() && pEntity.hasChest();
      this.rightChest.visible = flag;
      this.leftChest.visible = flag;
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      if (this.young) {
         float f = 2.0F;
         pPoseStack.pushPose();
         float f1 = 0.7F;
         pPoseStack.scale(0.71428573F, 0.64935064F, 0.7936508F);
         pPoseStack.translate(0.0F, 1.3125F, 0.22F);
         this.head.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         pPoseStack.popPose();
         pPoseStack.pushPose();
         float f2 = 1.1F;
         pPoseStack.scale(0.625F, 0.45454544F, 0.45454544F);
         pPoseStack.translate(0.0F, 2.0625F, 0.0F);
         this.body.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         pPoseStack.popPose();
         pPoseStack.pushPose();
         pPoseStack.scale(0.45454544F, 0.41322312F, 0.45454544F);
         pPoseStack.translate(0.0F, 2.0625F, 0.0F);
         ImmutableList.of(this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((p_103083_) -> {
            p_103083_.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         });
         pPoseStack.popPose();
      } else {
         ImmutableList.of(this.head, this.body, this.rightHindLeg, this.leftHindLeg, this.rightFrontLeg, this.leftFrontLeg, this.rightChest, this.leftChest).forEach((p_103073_) -> {
            p_103073_.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         });
      }

   }
}