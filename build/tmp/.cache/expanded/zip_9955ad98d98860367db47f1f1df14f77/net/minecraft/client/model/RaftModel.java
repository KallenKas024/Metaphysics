package net.minecraft.client.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RaftModel extends ListModel<Boat> {
   private static final String LEFT_PADDLE = "left_paddle";
   private static final String RIGHT_PADDLE = "right_paddle";
   private static final String BOTTOM = "bottom";
   private final ModelPart leftPaddle;
   private final ModelPart rightPaddle;
   private final ImmutableList<ModelPart> parts;

   public RaftModel(ModelPart pRoot) {
      this.leftPaddle = pRoot.getChild("left_paddle");
      this.rightPaddle = pRoot.getChild("right_paddle");
      this.parts = this.createPartsBuilder(pRoot).build();
   }

   protected ImmutableList.Builder<ModelPart> createPartsBuilder(ModelPart pRoot) {
      ImmutableList.Builder<ModelPart> builder = new ImmutableList.Builder<>();
      builder.add(pRoot.getChild("bottom"), this.leftPaddle, this.rightPaddle);
      return builder;
   }

   public static void createChildren(PartDefinition pRoot) {
      pRoot.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-14.0F, -11.0F, -4.0F, 28.0F, 20.0F, 4.0F).texOffs(0, 0).addBox(-14.0F, -9.0F, -8.0F, 28.0F, 16.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -2.0F, 1.0F, 1.5708F, 0.0F, 0.0F));
      int i = 20;
      int j = 7;
      int k = 6;
      float f = -5.0F;
      pRoot.addOrReplaceChild("left_paddle", CubeListBuilder.create().texOffs(0, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(-1.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(3.0F, -4.0F, 9.0F, 0.0F, 0.0F, 0.19634955F));
      pRoot.addOrReplaceChild("right_paddle", CubeListBuilder.create().texOffs(40, 24).addBox(-1.0F, 0.0F, -5.0F, 2.0F, 2.0F, 18.0F).addBox(0.001F, -3.0F, 8.0F, 1.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(3.0F, -4.0F, -9.0F, 0.0F, (float)Math.PI, 0.19634955F));
   }

   public static LayerDefinition createBodyModel() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      createChildren(partdefinition);
      return LayerDefinition.create(meshdefinition, 128, 64);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(Boat pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      animatePaddle(pEntity, 0, this.leftPaddle, pLimbSwing);
      animatePaddle(pEntity, 1, this.rightPaddle, pLimbSwing);
   }

   public ImmutableList<ModelPart> parts() {
      return this.parts;
   }

   private static void animatePaddle(Boat pBoat, int pSide, ModelPart pPaddle, float pLimbSwing) {
      float f = pBoat.getRowingTime(pSide, pLimbSwing);
      pPaddle.xRot = Mth.clampedLerp((-(float)Math.PI / 3F), -0.2617994F, (Mth.sin(-f) + 1.0F) / 2.0F);
      pPaddle.yRot = Mth.clampedLerp((-(float)Math.PI / 4F), ((float)Math.PI / 4F), (Mth.sin(-f + 1.0F) + 1.0F) / 2.0F);
      if (pSide == 1) {
         pPaddle.yRot = (float)Math.PI - pPaddle.yRot;
      }

   }
}