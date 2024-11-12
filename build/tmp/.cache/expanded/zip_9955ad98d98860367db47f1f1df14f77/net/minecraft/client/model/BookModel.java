package net.minecraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BookModel extends Model {
   private static final String LEFT_PAGES = "left_pages";
   private static final String RIGHT_PAGES = "right_pages";
   private static final String FLIP_PAGE_1 = "flip_page1";
   private static final String FLIP_PAGE_2 = "flip_page2";
   private final ModelPart root;
   private final ModelPart leftLid;
   private final ModelPart rightLid;
   private final ModelPart leftPages;
   private final ModelPart rightPages;
   private final ModelPart flipPage1;
   private final ModelPart flipPage2;

   public BookModel(ModelPart pRoot) {
      super(RenderType::entitySolid);
      this.root = pRoot;
      this.leftLid = pRoot.getChild("left_lid");
      this.rightLid = pRoot.getChild("right_lid");
      this.leftPages = pRoot.getChild("left_pages");
      this.rightPages = pRoot.getChild("right_pages");
      this.flipPage1 = pRoot.getChild("flip_page1");
      this.flipPage2 = pRoot.getChild("flip_page2");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, -1.0F));
      partdefinition.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), PartPose.offset(0.0F, 0.0F, 1.0F));
      partdefinition.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F), PartPose.rotation(0.0F, ((float)Math.PI / 2F), 0.0F));
      partdefinition.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
      CubeListBuilder cubelistbuilder = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
      partdefinition.addOrReplaceChild("flip_page1", cubelistbuilder, PartPose.ZERO);
      partdefinition.addOrReplaceChild("flip_page2", cubelistbuilder, PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }

   public void render(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
   }

   public void setupAnim(float pTime, float pRightPageFlipAmount, float pLeftPageFlipAmount, float pBookOpenAmount) {
      float f = (Mth.sin(pTime * 0.02F) * 0.1F + 1.25F) * pBookOpenAmount;
      this.leftLid.yRot = (float)Math.PI + f;
      this.rightLid.yRot = -f;
      this.leftPages.yRot = f;
      this.rightPages.yRot = -f;
      this.flipPage1.yRot = f - f * 2.0F * pRightPageFlipAmount;
      this.flipPage2.yRot = f - f * 2.0F * pLeftPageFlipAmount;
      this.leftPages.x = Mth.sin(f);
      this.rightPages.x = Mth.sin(f);
      this.flipPage1.x = Mth.sin(f);
      this.flipPage2.x = Mth.sin(f);
   }
}