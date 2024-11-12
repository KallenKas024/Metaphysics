package net.minecraft.client.model;

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
public class EndermiteModel<T extends Entity> extends HierarchicalModel<T> {
   private static final int BODY_COUNT = 4;
   private static final int[][] BODY_SIZES = new int[][]{{4, 3, 2}, {6, 4, 5}, {3, 3, 1}, {1, 2, 1}};
   private static final int[][] BODY_TEXS = new int[][]{{0, 0}, {0, 5}, {0, 14}, {0, 18}};
   private final ModelPart root;
   private final ModelPart[] bodyParts;

   public EndermiteModel(ModelPart pRoot) {
      this.root = pRoot;
      this.bodyParts = new ModelPart[4];

      for(int i = 0; i < 4; ++i) {
         this.bodyParts[i] = pRoot.getChild(createSegmentName(i));
      }

   }

   private static String createSegmentName(int pIndex) {
      return "segment" + pIndex;
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      float f = -3.5F;

      for(int i = 0; i < 4; ++i) {
         partdefinition.addOrReplaceChild(createSegmentName(i), CubeListBuilder.create().texOffs(BODY_TEXS[i][0], BODY_TEXS[i][1]).addBox((float)BODY_SIZES[i][0] * -0.5F, 0.0F, (float)BODY_SIZES[i][2] * -0.5F, (float)BODY_SIZES[i][0], (float)BODY_SIZES[i][1], (float)BODY_SIZES[i][2]), PartPose.offset(0.0F, (float)(24 - BODY_SIZES[i][1]), f));
         if (i < 3) {
            f += (float)(BODY_SIZES[i][2] + BODY_SIZES[i + 1][2]) * 0.5F;
         }
      }

      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public ModelPart root() {
      return this.root;
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      for(int i = 0; i < this.bodyParts.length; ++i) {
         this.bodyParts[i].yRot = Mth.cos(pAgeInTicks * 0.9F + (float)i * 0.15F * (float)Math.PI) * (float)Math.PI * 0.01F * (float)(1 + Math.abs(i - 2));
         this.bodyParts[i].x = Mth.sin(pAgeInTicks * 0.9F + (float)i * 0.15F * (float)Math.PI) * (float)Math.PI * 0.1F * (float)Math.abs(i - 2);
      }

   }
}