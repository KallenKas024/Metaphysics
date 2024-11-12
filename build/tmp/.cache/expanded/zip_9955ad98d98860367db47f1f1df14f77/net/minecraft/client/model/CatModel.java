package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatModel<T extends Cat> extends OcelotModel<T> {
   private float lieDownAmount;
   private float lieDownAmountTail;
   private float relaxStateOneAmount;

   public CatModel(ModelPart pRoot) {
      super(pRoot);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.lieDownAmount = pEntity.getLieDownAmount(pPartialTick);
      this.lieDownAmountTail = pEntity.getLieDownAmountTail(pPartialTick);
      this.relaxStateOneAmount = pEntity.getRelaxStateOneAmount(pPartialTick);
      if (this.lieDownAmount <= 0.0F) {
         this.head.xRot = 0.0F;
         this.head.zRot = 0.0F;
         this.leftFrontLeg.xRot = 0.0F;
         this.leftFrontLeg.zRot = 0.0F;
         this.rightFrontLeg.xRot = 0.0F;
         this.rightFrontLeg.zRot = 0.0F;
         this.rightFrontLeg.x = -1.2F;
         this.leftHindLeg.xRot = 0.0F;
         this.rightHindLeg.xRot = 0.0F;
         this.rightHindLeg.zRot = 0.0F;
         this.rightHindLeg.x = -1.1F;
         this.rightHindLeg.y = 18.0F;
      }

      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
      if (pEntity.isInSittingPose()) {
         this.body.xRot = ((float)Math.PI / 4F);
         this.body.y += -4.0F;
         this.body.z += 5.0F;
         this.head.y += -3.3F;
         ++this.head.z;
         this.tail1.y += 8.0F;
         this.tail1.z += -2.0F;
         this.tail2.y += 2.0F;
         this.tail2.z += -0.8F;
         this.tail1.xRot = 1.7278761F;
         this.tail2.xRot = 2.670354F;
         this.leftFrontLeg.xRot = -0.15707964F;
         this.leftFrontLeg.y = 16.1F;
         this.leftFrontLeg.z = -7.0F;
         this.rightFrontLeg.xRot = -0.15707964F;
         this.rightFrontLeg.y = 16.1F;
         this.rightFrontLeg.z = -7.0F;
         this.leftHindLeg.xRot = (-(float)Math.PI / 2F);
         this.leftHindLeg.y = 21.0F;
         this.leftHindLeg.z = 1.0F;
         this.rightHindLeg.xRot = (-(float)Math.PI / 2F);
         this.rightHindLeg.y = 21.0F;
         this.rightHindLeg.z = 1.0F;
         this.state = 3;
      }

   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      if (this.lieDownAmount > 0.0F) {
         this.head.zRot = ModelUtils.rotlerpRad(this.head.zRot, -1.2707963F, this.lieDownAmount);
         this.head.yRot = ModelUtils.rotlerpRad(this.head.yRot, 1.2707963F, this.lieDownAmount);
         this.leftFrontLeg.xRot = -1.2707963F;
         this.rightFrontLeg.xRot = -0.47079635F;
         this.rightFrontLeg.zRot = -0.2F;
         this.rightFrontLeg.x = -0.2F;
         this.leftHindLeg.xRot = -0.4F;
         this.rightHindLeg.xRot = 0.5F;
         this.rightHindLeg.zRot = -0.5F;
         this.rightHindLeg.x = -0.3F;
         this.rightHindLeg.y = 20.0F;
         this.tail1.xRot = ModelUtils.rotlerpRad(this.tail1.xRot, 0.8F, this.lieDownAmountTail);
         this.tail2.xRot = ModelUtils.rotlerpRad(this.tail2.xRot, -0.4F, this.lieDownAmountTail);
      }

      if (this.relaxStateOneAmount > 0.0F) {
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, -0.58177644F, this.relaxStateOneAmount);
      }

   }
}