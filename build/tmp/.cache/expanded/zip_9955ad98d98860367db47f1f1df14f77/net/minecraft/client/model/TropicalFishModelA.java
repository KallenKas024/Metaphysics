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
public class TropicalFishModelA<T extends Entity> extends ColorableHierarchicalModel<T> {
   private final ModelPart root;
   private final ModelPart tail;

   public TropicalFishModelA(ModelPart pRoot) {
      this.root = pRoot;
      this.tail = pRoot.getChild("tail");
   }

   public static LayerDefinition createBodyLayer(CubeDeformation pCubeDeformation) {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      int i = 22;
      partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.5F, -3.0F, 2.0F, 3.0F, 6.0F, pCubeDeformation), PartPose.offset(0.0F, 22.0F, 0.0F));
      partdefinition.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, -6).addBox(0.0F, -1.5F, 0.0F, 0.0F, 3.0F, 6.0F, pCubeDeformation), PartPose.offset(0.0F, 22.0F, 3.0F));
      partdefinition.addOrReplaceChild("right_fin", CubeListBuilder.create().texOffs(2, 16).addBox(-2.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, pCubeDeformation), PartPose.offsetAndRotation(-1.0F, 22.5F, 0.0F, 0.0F, ((float)Math.PI / 4F), 0.0F));
      partdefinition.addOrReplaceChild("left_fin", CubeListBuilder.create().texOffs(2, 12).addBox(0.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, pCubeDeformation), PartPose.offsetAndRotation(1.0F, 22.5F, 0.0F, 0.0F, (-(float)Math.PI / 4F), 0.0F));
      partdefinition.addOrReplaceChild("top_fin", CubeListBuilder.create().texOffs(10, -5).addBox(0.0F, -3.0F, 0.0F, 0.0F, 3.0F, 6.0F, pCubeDeformation), PartPose.offset(0.0F, 20.5F, -3.0F));
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      float f = 1.0F;
      if (!pEntity.isInWater()) {
         f = 1.5F;
      }

      this.tail.yRot = -f * 0.45F * Mth.sin(0.6F * pAgeInTicks);
   }
}