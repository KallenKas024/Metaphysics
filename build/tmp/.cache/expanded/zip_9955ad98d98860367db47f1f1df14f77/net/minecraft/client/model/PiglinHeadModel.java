package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PiglinHeadModel extends SkullModelBase {
   private final ModelPart head;
   private final ModelPart leftEar;
   private final ModelPart rightEar;

   public PiglinHeadModel(ModelPart pRoot) {
      this.head = pRoot.getChild("head");
      this.leftEar = this.head.getChild("left_ear");
      this.rightEar = this.head.getChild("right_ear");
   }

   public static MeshDefinition createHeadModel() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PiglinModel.addHead(CubeDeformation.NONE, meshdefinition);
      return meshdefinition;
   }

   public void setupAnim(float pMouthAnimation, float pYRot, float pXRot) {
      this.head.yRot = pYRot * ((float)Math.PI / 180F);
      this.head.xRot = pXRot * ((float)Math.PI / 180F);
      float f = 1.2F;
      this.leftEar.zRot = (float)(-(Math.cos((double)(pMouthAnimation * (float)Math.PI * 0.2F * 1.2F)) + 2.5D)) * 0.2F;
      this.rightEar.zRot = (float)(Math.cos((double)(pMouthAnimation * (float)Math.PI * 0.2F)) + 2.5D) * 0.2F;
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.head.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }
}