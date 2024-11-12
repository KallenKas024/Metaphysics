package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignRenderer implements BlockEntityRenderer<SignBlockEntity> {
   private static final String STICK = "stick";
   private static final int BLACK_TEXT_OUTLINE_COLOR = -988212;
   private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
   private static final float RENDER_SCALE = 0.6666667F;
   private static final Vec3 TEXT_OFFSET = new Vec3(0.0D, (double)0.33333334F, (double)0.046666667F);
   private final Map<WoodType, SignRenderer.SignModel> signModels;
   private final Font font;

   public SignRenderer(BlockEntityRendererProvider.Context pContext) {
      this.signModels = WoodType.values().collect(ImmutableMap.toImmutableMap((p_173645_) -> {
         return p_173645_;
      }, (p_173651_) -> {
         return new SignRenderer.SignModel(pContext.bakeLayer(ModelLayers.createSignModelName(p_173651_)));
      }));
      this.font = pContext.getFont();
   }

   public void render(SignBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
      BlockState blockstate = pBlockEntity.getBlockState();
      SignBlock signblock = (SignBlock)blockstate.getBlock();
      WoodType woodtype = SignBlock.getWoodType(signblock);
      SignRenderer.SignModel signrenderer$signmodel = this.signModels.get(woodtype);
      signrenderer$signmodel.stick.visible = blockstate.getBlock() instanceof StandingSignBlock;
      this.renderSignWithText(pBlockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, blockstate, signblock, woodtype, signrenderer$signmodel);
   }

   public float getSignModelRenderScale() {
      return 0.6666667F;
   }

   public float getSignTextRenderScale() {
      return 0.6666667F;
   }

   void renderSignWithText(SignBlockEntity pSignEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, BlockState pState, SignBlock pSignBlock, WoodType pWoodType, Model pModel) {
      pPoseStack.pushPose();
      this.translateSign(pPoseStack, -pSignBlock.getYRotationDegrees(pState), pState);
      this.renderSign(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pWoodType, pModel);
      this.renderSignText(pSignEntity.getBlockPos(), pSignEntity.getFrontText(), pPoseStack, pBuffer, pPackedLight, pSignEntity.getTextLineHeight(), pSignEntity.getMaxTextLineWidth(), true);
      this.renderSignText(pSignEntity.getBlockPos(), pSignEntity.getBackText(), pPoseStack, pBuffer, pPackedLight, pSignEntity.getTextLineHeight(), pSignEntity.getMaxTextLineWidth(), false);
      pPoseStack.popPose();
   }

   void translateSign(PoseStack pPoseStack, float pYRot, BlockState pState) {
      pPoseStack.translate(0.5F, 0.75F * this.getSignModelRenderScale(), 0.5F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(pYRot));
      if (!(pState.getBlock() instanceof StandingSignBlock)) {
         pPoseStack.translate(0.0F, -0.3125F, -0.4375F);
      }

   }

   void renderSign(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, WoodType pWoodType, Model pModel) {
      pPoseStack.pushPose();
      float f = this.getSignModelRenderScale();
      pPoseStack.scale(f, -f, -f);
      Material material = this.getSignMaterial(pWoodType);
      VertexConsumer vertexconsumer = material.buffer(pBuffer, pModel::renderType);
      this.renderSignModel(pPoseStack, pPackedLight, pPackedOverlay, pModel, vertexconsumer);
      pPoseStack.popPose();
   }

   void renderSignModel(PoseStack pPoseStack, int pPackedLight, int pPackedOverlay, Model pModel, VertexConsumer pVertexConsumer) {
      SignRenderer.SignModel signrenderer$signmodel = (SignRenderer.SignModel)pModel;
      signrenderer$signmodel.root.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay);
   }

   Material getSignMaterial(WoodType pWoodType) {
      return Sheets.getSignMaterial(pWoodType);
   }

   void renderSignText(BlockPos pPos, SignText pText, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pLineHeight, int pMaxWidth, boolean pIsFrontText) {
      pPoseStack.pushPose();
      this.translateSignText(pPoseStack, pIsFrontText, this.getTextOffset());
      int i = getDarkColor(pText);
      int j = 4 * pLineHeight / 2;
      FormattedCharSequence[] aformattedcharsequence = pText.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), (p_277227_) -> {
         List<FormattedCharSequence> list = this.font.split(p_277227_, pMaxWidth);
         return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
      });
      int k;
      boolean flag;
      int l;
      if (pText.hasGlowingText()) {
         k = pText.getColor().getTextColor();
         flag = isOutlineVisible(pPos, k);
         l = 15728880;
      } else {
         k = i;
         flag = false;
         l = pPackedLight;
      }

      for(int i1 = 0; i1 < 4; ++i1) {
         FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
         float f = (float)(-this.font.width(formattedcharsequence) / 2);
         if (flag) {
            this.font.drawInBatch8xOutline(formattedcharsequence, f, (float)(i1 * pLineHeight - j), k, i, pPoseStack.last().pose(), pBuffer, l);
         } else {
            this.font.drawInBatch(formattedcharsequence, f, (float)(i1 * pLineHeight - j), k, false, pPoseStack.last().pose(), pBuffer, Font.DisplayMode.POLYGON_OFFSET, 0, l);
         }
      }

      pPoseStack.popPose();
   }

   private void translateSignText(PoseStack pPoseStack, boolean pIsFrontText, Vec3 pOffset) {
      if (!pIsFrontText) {
         pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      }

      float f = 0.015625F * this.getSignTextRenderScale();
      pPoseStack.translate(pOffset.x, pOffset.y, pOffset.z);
      pPoseStack.scale(f, -f, f);
   }

   Vec3 getTextOffset() {
      return TEXT_OFFSET;
   }

   static boolean isOutlineVisible(BlockPos pPos, int pTextColor) {
      if (pTextColor == DyeColor.BLACK.getTextColor()) {
         return true;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         LocalPlayer localplayer = minecraft.player;
         if (localplayer != null && minecraft.options.getCameraType().isFirstPerson() && localplayer.isScoping()) {
            return true;
         } else {
            Entity entity = minecraft.getCameraEntity();
            return entity != null && entity.distanceToSqr(Vec3.atCenterOf(pPos)) < (double)OUTLINE_RENDER_DISTANCE;
         }
      }
   }

   static int getDarkColor(SignText pSignText) {
      int i = pSignText.getColor().getTextColor();
      if (i == DyeColor.BLACK.getTextColor() && pSignText.hasGlowingText()) {
         return -988212;
      } else {
         double d0 = 0.4D;
         int j = (int)((double)FastColor.ARGB32.red(i) * 0.4D);
         int k = (int)((double)FastColor.ARGB32.green(i) * 0.4D);
         int l = (int)((double)FastColor.ARGB32.blue(i) * 0.4D);
         return FastColor.ARGB32.color(0, j, k, l);
      }
   }

   public static SignRenderer.SignModel createSignModel(EntityModelSet pEntityModelSet, WoodType pWoodType) {
      return new SignRenderer.SignModel(pEntityModelSet.bakeLayer(ModelLayers.createSignModelName(pWoodType)));
   }

   public static LayerDefinition createSignLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      partdefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F), PartPose.ZERO);
      partdefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F), PartPose.ZERO);
      return LayerDefinition.create(meshdefinition, 64, 32);
   }

   @OnlyIn(Dist.CLIENT)
   public static final class SignModel extends Model {
      public final ModelPart root;
      public final ModelPart stick;

      public SignModel(ModelPart pRoot) {
         super(RenderType::entityCutoutNoCull);
         this.root = pRoot;
         this.stick = pRoot.getChild("stick");
      }

      public void renderToBuffer(PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
         this.root.render(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
      }
   }
}