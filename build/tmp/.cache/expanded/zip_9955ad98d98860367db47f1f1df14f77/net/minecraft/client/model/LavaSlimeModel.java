package net.minecraft.client.model;

import java.util.Arrays;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LavaSlimeModel<T extends Slime> extends HierarchicalModel<T> {
   private static final int SEGMENT_COUNT = 8;
   private final ModelPart root;
   private final ModelPart[] bodyCubes = new ModelPart[8];

   public LavaSlimeModel(ModelPart pRoot) {
      this.root = pRoot;
      Arrays.setAll(this.bodyCubes, (p_170709_) -> {
         return pRoot.getChild(getSegmentName(p_170709_));
      });
   }

   private static String getSegmentName(int pIndex) {
      return "cube" + pIndex;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();

      for(int i = 0; i < 8; ++i) {
         int j = 0;
         int k = i;
         if (i == 2) {
            j = 24;
            k = 10;
         } else if (i == 3) {
            j = 24;
            k = 19;
         }

         partdefinition.addOrReplaceChild(getSegmentName(i), CubeListBuilder.create().texOffs(j, k).addBox(-4.0F, (float)(16 + i), -4.0F, 8.0F, 1.0F, 8.0F), PartPose.ZERO);
      }

      partdefinition.addOrReplaceChild("inside_cube", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 18.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      float f = Mth.lerp(pPartialTick, pEntity.oSquish, pEntity.squish);
      if (f < 0.0F) {
         f = 0.0F;
      }

      for(int i = 0; i < this.bodyCubes.length; ++i) {
         this.bodyCubes[i].y = (float)(-(4 - i)) * f * 1.7F;
      }

   }

   public ModelPart root() {
      return this.root;
   }
}