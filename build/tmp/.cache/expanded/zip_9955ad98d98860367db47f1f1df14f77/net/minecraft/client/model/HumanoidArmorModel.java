package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
   public HumanoidArmorModel(ModelPart pRoot) {
      super(pRoot);
   }

   public static MeshDefinition createBodyLayer(CubeDeformation pCubDeformation) {
      MeshDefinition meshdefinition = HumanoidModel.createMesh(pCubDeformation, 0.0F);
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubDeformation.extend(-0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
      partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, pCubDeformation.extend(-0.1F)), PartPose.offset(1.9F, 12.0F, 0.0F));
      return meshdefinition;
   }
}