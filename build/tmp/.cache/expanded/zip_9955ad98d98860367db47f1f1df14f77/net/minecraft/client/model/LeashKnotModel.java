package net.minecraft.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotModel<T extends Entity> extends HierarchicalModel<T> {
   private static final String KNOT = "knot";
   private final ModelPart root;
   private final ModelPart knot;

   public LeashKnotModel(ModelPart pRoot) {
      this.root = pRoot;
      this.knot = pRoot.getChild("knot");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("knot", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 8.0F, 6.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 32, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.knot.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.knot.xRot = pHeadPitch * ((float)Math.PI / 180F);
   }
}