package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullModel extends SkullModelBase {
   private final ModelPart root;
   protected final ModelPart head;

   public SkullModel(ModelPart pRoot) {
      this.root = pRoot;
      this.head = pRoot.getChild("head");
   }

   public static MeshDefinition createHeadModel() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
      return meshdefinition;
   }

   public static LayerDefinition createHumanoidHeadLayer() {
      MeshDefinition meshdefinition = createHeadModel();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.getChild("head").addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   public static LayerDefinition createMobHeadLayer() {
      MeshDefinition meshdefinition = createHeadModel();
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void setupAnim(float pMouthAnimation, float pYRot, float pXRot) {
      this.head.yRot = pYRot * ((float)Math.PI / 180F);
      this.head.xRot = pXRot * ((float)Math.PI / 180F);
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }
}