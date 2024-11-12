package net.minecraft.client.renderer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ItemInHandRenderer {
   private static final RenderType MAP_BACKGROUND = RenderType.text(new ResourceLocation("textures/map/map_background.png"));
   private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(new ResourceLocation("textures/map/map_background_checkerboard.png"));
   private static final float ITEM_SWING_X_POS_SCALE = -0.4F;
   private static final float ITEM_SWING_Y_POS_SCALE = 0.2F;
   private static final float ITEM_SWING_Z_POS_SCALE = -0.2F;
   private static final float ITEM_HEIGHT_SCALE = -0.6F;
   private static final float ITEM_POS_X = 0.56F;
   private static final float ITEM_POS_Y = -0.52F;
   private static final float ITEM_POS_Z = -0.72F;
   private static final float ITEM_PRESWING_ROT_Y = 45.0F;
   private static final float ITEM_SWING_X_ROT_AMOUNT = -80.0F;
   private static final float ITEM_SWING_Y_ROT_AMOUNT = -20.0F;
   private static final float ITEM_SWING_Z_ROT_AMOUNT = -20.0F;
   private static final float EAT_JIGGLE_X_ROT_AMOUNT = 10.0F;
   private static final float EAT_JIGGLE_Y_ROT_AMOUNT = 90.0F;
   private static final float EAT_JIGGLE_Z_ROT_AMOUNT = 30.0F;
   private static final float EAT_JIGGLE_X_POS_SCALE = 0.6F;
   private static final float EAT_JIGGLE_Y_POS_SCALE = -0.5F;
   private static final float EAT_JIGGLE_Z_POS_SCALE = 0.0F;
   private static final double EAT_JIGGLE_EXPONENT = 27.0D;
   private static final float EAT_EXTRA_JIGGLE_CUTOFF = 0.8F;
   private static final float EAT_EXTRA_JIGGLE_SCALE = 0.1F;
   private static final float ARM_SWING_X_POS_SCALE = -0.3F;
   private static final float ARM_SWING_Y_POS_SCALE = 0.4F;
   private static final float ARM_SWING_Z_POS_SCALE = -0.4F;
   private static final float ARM_SWING_Y_ROT_AMOUNT = 70.0F;
   private static final float ARM_SWING_Z_ROT_AMOUNT = -20.0F;
   private static final float ARM_HEIGHT_SCALE = -0.6F;
   private static final float ARM_POS_SCALE = 0.8F;
   private static final float ARM_POS_X = 0.8F;
   private static final float ARM_POS_Y = -0.75F;
   private static final float ARM_POS_Z = -0.9F;
   private static final float ARM_PRESWING_ROT_Y = 45.0F;
   private static final float ARM_PREROTATION_X_OFFSET = -1.0F;
   private static final float ARM_PREROTATION_Y_OFFSET = 3.6F;
   private static final float ARM_PREROTATION_Z_OFFSET = 3.5F;
   private static final float ARM_POSTROTATION_X_OFFSET = 5.6F;
   private static final int ARM_ROT_X = 200;
   private static final int ARM_ROT_Y = -135;
   private static final int ARM_ROT_Z = 120;
   private static final float MAP_SWING_X_POS_SCALE = -0.4F;
   private static final float MAP_SWING_Z_POS_SCALE = -0.2F;
   private static final float MAP_HANDS_POS_X = 0.0F;
   private static final float MAP_HANDS_POS_Y = 0.04F;
   private static final float MAP_HANDS_POS_Z = -0.72F;
   private static final float MAP_HANDS_HEIGHT_SCALE = -1.2F;
   private static final float MAP_HANDS_TILT_SCALE = -0.5F;
   private static final float MAP_PLAYER_PITCH_SCALE = 45.0F;
   private static final float MAP_HANDS_Z_ROT_AMOUNT = -85.0F;
   private static final float MAPHAND_X_ROT_AMOUNT = 45.0F;
   private static final float MAPHAND_Y_ROT_AMOUNT = 92.0F;
   private static final float MAPHAND_Z_ROT_AMOUNT = -41.0F;
   private static final float MAP_HAND_X_POS = 0.3F;
   private static final float MAP_HAND_Y_POS = -1.1F;
   private static final float MAP_HAND_Z_POS = 0.45F;
   private static final float MAP_SWING_X_ROT_AMOUNT = 20.0F;
   private static final float MAP_PRE_ROT_SCALE = 0.38F;
   private static final float MAP_GLOBAL_X_POS = -0.5F;
   private static final float MAP_GLOBAL_Y_POS = -0.5F;
   private static final float MAP_GLOBAL_Z_POS = 0.0F;
   private static final float MAP_FINAL_SCALE = 0.0078125F;
   private static final int MAP_BORDER = 7;
   private static final int MAP_HEIGHT = 128;
   private static final int MAP_WIDTH = 128;
   private static final float BOW_CHARGE_X_POS_SCALE = 0.0F;
   private static final float BOW_CHARGE_Y_POS_SCALE = 0.0F;
   private static final float BOW_CHARGE_Z_POS_SCALE = 0.04F;
   private static final float BOW_CHARGE_SHAKE_X_SCALE = 0.0F;
   private static final float BOW_CHARGE_SHAKE_Y_SCALE = 0.004F;
   private static final float BOW_CHARGE_SHAKE_Z_SCALE = 0.0F;
   private static final float BOW_CHARGE_Z_SCALE = 0.2F;
   private static final float BOW_MIN_SHAKE_CHARGE = 0.1F;
   private final Minecraft minecraft;
   private ItemStack mainHandItem = ItemStack.EMPTY;
   private ItemStack offHandItem = ItemStack.EMPTY;
   private float mainHandHeight;
   private float oMainHandHeight;
   private float offHandHeight;
   private float oOffHandHeight;
   private final EntityRenderDispatcher entityRenderDispatcher;
   private final ItemRenderer itemRenderer;

   public ItemInHandRenderer(Minecraft pMinecraft, EntityRenderDispatcher pEntityRenderDispatcher, ItemRenderer pItemRenderer) {
      this.minecraft = pMinecraft;
      this.entityRenderDispatcher = pEntityRenderDispatcher;
      this.itemRenderer = pItemRenderer;
   }

   public void renderItem(LivingEntity pEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, boolean pLeftHand, PoseStack pPoseStack, MultiBufferSource pBuffer, int pSeed) {
      if (!pItemStack.isEmpty()) {
         this.itemRenderer.renderStatic(pEntity, pItemStack, pDisplayContext, pLeftHand, pPoseStack, pBuffer, pEntity.level(), pSeed, OverlayTexture.NO_OVERLAY, pEntity.getId() + pDisplayContext.ordinal());
      }
   }

   /**
    * Return the angle to render the Map
    */
   private float calculateMapTilt(float pPitch) {
      float f = 1.0F - pPitch / 45.0F + 0.1F;
      f = Mth.clamp(f, 0.0F, 1.0F);
      return -Mth.cos(f * (float)Math.PI) * 0.5F + 0.5F;
   }

   private void renderMapHand(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, HumanoidArm pSide) {
      RenderSystem.setShaderTexture(0, this.minecraft.player.getSkinTextureLocation());
      PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(this.minecraft.player);
      pPoseStack.pushPose();
      float f = pSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      pPoseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
      pPoseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
      pPoseStack.translate(f * 0.3F, -1.1F, 0.45F);
      if (pSide == HumanoidArm.RIGHT) {
         playerrenderer.renderRightHand(pPoseStack, pBuffer, pCombinedLight, this.minecraft.player);
      } else {
         playerrenderer.renderLeftHand(pPoseStack, pBuffer, pCombinedLight, this.minecraft.player);
      }

      pPoseStack.popPose();
   }

   private void renderOneHandedMap(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, HumanoidArm pHand, float pSwingProgress, ItemStack pStack) {
      float f = pHand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      pPoseStack.translate(f * 0.125F, -0.125F, 0.0F);
      if (!this.minecraft.player.isInvisible()) {
         pPoseStack.pushPose();
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
         this.renderPlayerArm(pPoseStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand);
         pPoseStack.popPose();
      }

      pPoseStack.pushPose();
      pPoseStack.translate(f * 0.51F, -0.08F + pEquippedProgress * -1.2F, -0.75F);
      float f1 = Mth.sqrt(pSwingProgress);
      float f2 = Mth.sin(f1 * (float)Math.PI);
      float f3 = -0.5F * f2;
      float f4 = 0.4F * Mth.sin(f1 * ((float)Math.PI * 2F));
      float f5 = -0.3F * Mth.sin(pSwingProgress * (float)Math.PI);
      pPoseStack.translate(f * f3, f4 - 0.3F * f2, f5);
      pPoseStack.mulPose(Axis.XP.rotationDegrees(f2 * -45.0F));
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f * f2 * -30.0F));
      this.renderMap(pPoseStack, pBuffer, pCombinedLight, pStack);
      pPoseStack.popPose();
   }

   private void renderTwoHandedMap(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pPitch, float pEquippedProgress, float pSwingProgress) {
      float f = Mth.sqrt(pSwingProgress);
      float f1 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
      float f2 = -0.4F * Mth.sin(f * (float)Math.PI);
      pPoseStack.translate(0.0F, -f1 / 2.0F, f2);
      float f3 = this.calculateMapTilt(pPitch);
      pPoseStack.translate(0.0F, 0.04F + pEquippedProgress * -1.2F + f3 * -0.5F, -0.72F);
      pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * -85.0F));
      if (!this.minecraft.player.isInvisible()) {
         pPoseStack.pushPose();
         pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
         this.renderMapHand(pPoseStack, pBuffer, pCombinedLight, HumanoidArm.RIGHT);
         this.renderMapHand(pPoseStack, pBuffer, pCombinedLight, HumanoidArm.LEFT);
         pPoseStack.popPose();
      }

      float f4 = Mth.sin(f * (float)Math.PI);
      pPoseStack.mulPose(Axis.XP.rotationDegrees(f4 * 20.0F));
      pPoseStack.scale(2.0F, 2.0F, 2.0F);
      this.renderMap(pPoseStack, pBuffer, pCombinedLight, this.mainHandItem);
   }

   private void renderMap(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack) {
      pPoseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
      pPoseStack.scale(0.38F, 0.38F, 0.38F);
      pPoseStack.translate(-0.5F, -0.5F, 0.0F);
      pPoseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
      Integer integer = MapItem.getMapId(pStack);
      MapItemSavedData mapitemsaveddata = MapItem.getSavedData(integer, this.minecraft.level);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
      Matrix4f matrix4f = pPoseStack.last().pose();
      vertexconsumer.vertex(matrix4f, -7.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(pCombinedLight).endVertex();
      vertexconsumer.vertex(matrix4f, 135.0F, 135.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(pCombinedLight).endVertex();
      vertexconsumer.vertex(matrix4f, 135.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(pCombinedLight).endVertex();
      vertexconsumer.vertex(matrix4f, -7.0F, -7.0F, 0.0F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(pCombinedLight).endVertex();
      if (mapitemsaveddata != null) {
         this.minecraft.gameRenderer.getMapRenderer().render(pPoseStack, pBuffer, integer, mapitemsaveddata, false, pCombinedLight);
      }

   }

   private void renderPlayerArm(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide) {
      boolean flag = pSide != HumanoidArm.LEFT;
      float f = flag ? 1.0F : -1.0F;
      float f1 = Mth.sqrt(pSwingProgress);
      float f2 = -0.3F * Mth.sin(f1 * (float)Math.PI);
      float f3 = 0.4F * Mth.sin(f1 * ((float)Math.PI * 2F));
      float f4 = -0.4F * Mth.sin(pSwingProgress * (float)Math.PI);
      pPoseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + pEquippedProgress * -0.6F, f4 + -0.71999997F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
      float f5 = Mth.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
      float f6 = Mth.sin(f1 * (float)Math.PI);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
      AbstractClientPlayer abstractclientplayer = this.minecraft.player;
      RenderSystem.setShaderTexture(0, abstractclientplayer.getSkinTextureLocation());
      pPoseStack.translate(f * -1.0F, 3.6F, 3.5F);
      pPoseStack.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
      pPoseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
      pPoseStack.translate(f * 5.6F, 0.0F, 0.0F);
      PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.<AbstractClientPlayer>getRenderer(abstractclientplayer);
      if (flag) {
         playerrenderer.renderRightHand(pPoseStack, pBuffer, pCombinedLight, abstractclientplayer);
      } else {
         playerrenderer.renderLeftHand(pPoseStack, pBuffer, pCombinedLight, abstractclientplayer);
      }

   }

   private void applyEatTransform(PoseStack pPoseStack, float pPartialTicks, HumanoidArm pHand, ItemStack pStack) {
      float f = (float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F;
      float f1 = f / (float)pStack.getUseDuration();
      if (f1 < 0.8F) {
         float f2 = Mth.abs(Mth.cos(f / 4.0F * (float)Math.PI) * 0.1F);
         pPoseStack.translate(0.0F, f2, 0.0F);
      }

      float f3 = 1.0F - (float)Math.pow((double)f1, 27.0D);
      int i = pHand == HumanoidArm.RIGHT ? 1 : -1;
      pPoseStack.translate(f3 * 0.6F * (float)i, f3 * -0.5F, f3 * 0.0F);
      pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * f3 * 90.0F));
      pPoseStack.mulPose(Axis.XP.rotationDegrees(f3 * 10.0F));
      pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f3 * 30.0F));
   }

   private void applyBrushTransform(PoseStack pPoseStack, float pPartialTick, HumanoidArm pHand, ItemStack pStack, float pEquippedProg) {
      this.applyItemArmTransform(pPoseStack, pHand, pEquippedProg);
      float f = (float)(this.minecraft.player.getUseItemRemainingTicks() % 10);
      float f1 = f - pPartialTick + 1.0F;
      float f2 = 1.0F - f1 / 10.0F;
      float f3 = -90.0F;
      float f4 = 60.0F;
      float f5 = 150.0F;
      float f6 = -15.0F;
      int i = 2;
      float f7 = -15.0F + 75.0F * Mth.cos(f2 * 2.0F * (float)Math.PI);
      if (pHand != HumanoidArm.RIGHT) {
         pPoseStack.translate(0.1D, 0.83D, 0.35D);
         pPoseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
         pPoseStack.mulPose(Axis.XP.rotationDegrees(f7));
         pPoseStack.translate(-0.3D, 0.22D, 0.35D);
      } else {
         pPoseStack.translate(-0.25D, 0.22D, 0.35D);
         pPoseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
         pPoseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
         pPoseStack.mulPose(Axis.XP.rotationDegrees(f7));
      }

   }

   private void applyItemArmAttackTransform(PoseStack pPoseStack, HumanoidArm pHand, float pSwingProgress) {
      int i = pHand == HumanoidArm.RIGHT ? 1 : -1;
      float f = Mth.sin(pSwingProgress * pSwingProgress * (float)Math.PI);
      pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
      float f1 = Mth.sin(Mth.sqrt(pSwingProgress) * (float)Math.PI);
      pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f1 * -20.0F));
      pPoseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
      pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * -45.0F));
   }

   private void applyItemArmTransform(PoseStack pPoseStack, HumanoidArm pHand, float pEquippedProg) {
      int i = pHand == HumanoidArm.RIGHT ? 1 : -1;
      pPoseStack.translate((float)i * 0.56F, -0.52F + pEquippedProg * -0.6F, -0.72F);
   }

   public void renderHandsWithItems(float pPartialTicks, PoseStack pPoseStack, MultiBufferSource.BufferSource pBuffer, LocalPlayer pPlayerEntity, int pCombinedLight) {
      float f = pPlayerEntity.getAttackAnim(pPartialTicks);
      InteractionHand interactionhand = MoreObjects.firstNonNull(pPlayerEntity.swingingArm, InteractionHand.MAIN_HAND);
      float f1 = Mth.lerp(pPartialTicks, pPlayerEntity.xRotO, pPlayerEntity.getXRot());
      ItemInHandRenderer.HandRenderSelection iteminhandrenderer$handrenderselection = evaluateWhichHandsToRender(pPlayerEntity);
      float f2 = Mth.lerp(pPartialTicks, pPlayerEntity.xBobO, pPlayerEntity.xBob);
      float f3 = Mth.lerp(pPartialTicks, pPlayerEntity.yBobO, pPlayerEntity.yBob);
      pPoseStack.mulPose(Axis.XP.rotationDegrees((pPlayerEntity.getViewXRot(pPartialTicks) - f2) * 0.1F));
      pPoseStack.mulPose(Axis.YP.rotationDegrees((pPlayerEntity.getViewYRot(pPartialTicks) - f3) * 0.1F));
      if (iteminhandrenderer$handrenderselection.renderMainHand) {
         float f4 = interactionhand == InteractionHand.MAIN_HAND ? f : 0.0F;
         float f5 = 1.0F - Mth.lerp(pPartialTicks, this.oMainHandHeight, this.mainHandHeight);
         if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(InteractionHand.MAIN_HAND, pPoseStack, pBuffer, pCombinedLight, pPartialTicks, f1, f4, f5, this.mainHandItem))
         this.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, InteractionHand.MAIN_HAND, f4, this.mainHandItem, f5, pPoseStack, pBuffer, pCombinedLight);
      }

      if (iteminhandrenderer$handrenderselection.renderOffHand) {
         float f6 = interactionhand == InteractionHand.OFF_HAND ? f : 0.0F;
         float f7 = 1.0F - Mth.lerp(pPartialTicks, this.oOffHandHeight, this.offHandHeight);
         if(!net.minecraftforge.client.ForgeHooksClient.renderSpecificFirstPersonHand(InteractionHand.OFF_HAND, pPoseStack, pBuffer, pCombinedLight, pPartialTicks, f1, f6, f7, this.offHandItem))
         this.renderArmWithItem(pPlayerEntity, pPartialTicks, f1, InteractionHand.OFF_HAND, f6, this.offHandItem, f7, pPoseStack, pBuffer, pCombinedLight);
      }

      pBuffer.endBatch();
   }

   @VisibleForTesting
   static ItemInHandRenderer.HandRenderSelection evaluateWhichHandsToRender(LocalPlayer pPlayer) {
      ItemStack itemstack = pPlayer.getMainHandItem();
      ItemStack itemstack1 = pPlayer.getOffhandItem();
      boolean flag = itemstack.is(Items.BOW) || itemstack1.is(Items.BOW);
      boolean flag1 = itemstack.is(Items.CROSSBOW) || itemstack1.is(Items.CROSSBOW);
      if (!flag && !flag1) {
         return ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
      } else if (pPlayer.isUsingItem()) {
         return selectionUsingItemWhileHoldingBowLike(pPlayer);
      } else {
         return isChargedCrossbow(itemstack) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
      }
   }

   private static ItemInHandRenderer.HandRenderSelection selectionUsingItemWhileHoldingBowLike(LocalPlayer pPlayer) {
      ItemStack itemstack = pPlayer.getUseItem();
      InteractionHand interactionhand = pPlayer.getUsedItemHand();
      if (!itemstack.is(Items.BOW) && !itemstack.is(Items.CROSSBOW)) {
         return interactionhand == InteractionHand.MAIN_HAND && isChargedCrossbow(pPlayer.getOffhandItem()) ? ItemInHandRenderer.HandRenderSelection.RENDER_MAIN_HAND_ONLY : ItemInHandRenderer.HandRenderSelection.RENDER_BOTH_HANDS;
      } else {
         return ItemInHandRenderer.HandRenderSelection.onlyForHand(interactionhand);
      }
   }

   private static boolean isChargedCrossbow(ItemStack pStack) {
      return pStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(pStack);
   }

   private void renderArmWithItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight) {
      if (!pPlayer.isScoping()) {
         boolean flag = pHand == InteractionHand.MAIN_HAND;
         HumanoidArm humanoidarm = flag ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
         pPoseStack.pushPose();
         if (pStack.isEmpty()) {
            if (flag && !pPlayer.isInvisible()) {
               this.renderPlayerArm(pPoseStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, humanoidarm);
            }
         } else if (pStack.is(Items.FILLED_MAP)) {
            if (flag && this.offHandItem.isEmpty()) {
               this.renderTwoHandedMap(pPoseStack, pBuffer, pCombinedLight, pPitch, pEquippedProgress, pSwingProgress);
            } else {
               this.renderOneHandedMap(pPoseStack, pBuffer, pCombinedLight, pEquippedProgress, humanoidarm, pSwingProgress, pStack);
            }
         } else if (pStack.getItem() instanceof CrossbowItem) {
            boolean flag1 = CrossbowItem.isCharged(pStack);
            boolean flag2 = humanoidarm == HumanoidArm.RIGHT;
            int i = flag2 ? 1 : -1;
            if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
               this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
               pPoseStack.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
               pPoseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
               pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * 65.3F));
               pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)i * -9.785F));
               float f9 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
               float f13 = f9 / (float)CrossbowItem.getChargeDuration(pStack);
               if (f13 > 1.0F) {
                  f13 = 1.0F;
               }

               if (f13 > 0.1F) {
                  float f16 = Mth.sin((f9 - 0.1F) * 1.3F);
                  float f3 = f13 - 0.1F;
                  float f4 = f16 * f3;
                  pPoseStack.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
               }

               pPoseStack.translate(f13 * 0.0F, f13 * 0.0F, f13 * 0.04F);
               pPoseStack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
               pPoseStack.mulPose(Axis.YN.rotationDegrees((float)i * 45.0F));
            } else {
               float f = -0.4F * Mth.sin(Mth.sqrt(pSwingProgress) * (float)Math.PI);
               float f1 = 0.2F * Mth.sin(Mth.sqrt(pSwingProgress) * ((float)Math.PI * 2F));
               float f2 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
               pPoseStack.translate((float)i * f, f1, f2);
               this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
               this.applyItemArmAttackTransform(pPoseStack, humanoidarm, pSwingProgress);
               if (flag1 && pSwingProgress < 0.001F && flag) {
                  pPoseStack.translate((float)i * -0.641864F, 0.0F, 0.0F);
                  pPoseStack.mulPose(Axis.YP.rotationDegrees((float)i * 10.0F));
               }
            }

            this.renderItem(pPlayer, pStack, flag2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag2, pPoseStack, pBuffer, pCombinedLight);
         } else {
            boolean flag3 = humanoidarm == HumanoidArm.RIGHT;
            if (!net.minecraftforge.client.extensions.common.IClientItemExtensions.of(pStack).applyForgeHandTransform(pPoseStack, minecraft.player, humanoidarm, pStack, pPartialTicks, pEquippedProgress, pSwingProgress)) // FORGE: Allow items to define custom arm animation
            if (pPlayer.isUsingItem() && pPlayer.getUseItemRemainingTicks() > 0 && pPlayer.getUsedItemHand() == pHand) {
               int k = flag3 ? 1 : -1;
               switch (pStack.getUseAnimation()) {
                  case NONE:
                     this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
                     break;
                  case EAT:
                  case DRINK:
                     this.applyEatTransform(pPoseStack, pPartialTicks, humanoidarm, pStack);
                     this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
                     break;
                  case BLOCK:
                     this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
                     break;
                  case BOW:
                     this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
                     pPoseStack.translate((float)k * -0.2785682F, 0.18344387F, 0.15731531F);
                     pPoseStack.mulPose(Axis.XP.rotationDegrees(-13.935F));
                     pPoseStack.mulPose(Axis.YP.rotationDegrees((float)k * 35.3F));
                     pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
                     float f8 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
                     float f12 = f8 / 20.0F;
                     f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
                     if (f12 > 1.0F) {
                        f12 = 1.0F;
                     }

                     if (f12 > 0.1F) {
                        float f15 = Mth.sin((f8 - 0.1F) * 1.3F);
                        float f18 = f12 - 0.1F;
                        float f20 = f15 * f18;
                        pPoseStack.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
                     }

                     pPoseStack.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
                     pPoseStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
                     pPoseStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
                     break;
                  case SPEAR:
                     this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
                     pPoseStack.translate((float)k * -0.5F, 0.7F, 0.1F);
                     pPoseStack.mulPose(Axis.XP.rotationDegrees(-55.0F));
                     pPoseStack.mulPose(Axis.YP.rotationDegrees((float)k * 35.3F));
                     pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
                     float f7 = (float)pStack.getUseDuration() - ((float)this.minecraft.player.getUseItemRemainingTicks() - pPartialTicks + 1.0F);
                     float f11 = f7 / 10.0F;
                     if (f11 > 1.0F) {
                        f11 = 1.0F;
                     }

                     if (f11 > 0.1F) {
                        float f14 = Mth.sin((f7 - 0.1F) * 1.3F);
                        float f17 = f11 - 0.1F;
                        float f19 = f14 * f17;
                        pPoseStack.translate(f19 * 0.0F, f19 * 0.004F, f19 * 0.0F);
                     }

                     pPoseStack.translate(0.0F, 0.0F, f11 * 0.2F);
                     pPoseStack.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
                     pPoseStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
                     break;
                  case BRUSH:
                     this.applyBrushTransform(pPoseStack, pPartialTicks, humanoidarm, pStack, pEquippedProgress);
               }
            } else if (pPlayer.isAutoSpinAttack()) {
               this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
               int j = flag3 ? 1 : -1;
               pPoseStack.translate((float)j * -0.4F, 0.8F, 0.3F);
               pPoseStack.mulPose(Axis.YP.rotationDegrees((float)j * 65.0F));
               pPoseStack.mulPose(Axis.ZP.rotationDegrees((float)j * -85.0F));
            } else {
               float f5 = -0.4F * Mth.sin(Mth.sqrt(pSwingProgress) * (float)Math.PI);
               float f6 = 0.2F * Mth.sin(Mth.sqrt(pSwingProgress) * ((float)Math.PI * 2F));
               float f10 = -0.2F * Mth.sin(pSwingProgress * (float)Math.PI);
               int l = flag3 ? 1 : -1;
               pPoseStack.translate((float)l * f5, f6, f10);
               this.applyItemArmTransform(pPoseStack, humanoidarm, pEquippedProgress);
               this.applyItemArmAttackTransform(pPoseStack, humanoidarm, pSwingProgress);
            }

            this.renderItem(pPlayer, pStack, flag3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !flag3, pPoseStack, pBuffer, pCombinedLight);
         }

         pPoseStack.popPose();
      }
   }

   public void tick() {
      this.oMainHandHeight = this.mainHandHeight;
      this.oOffHandHeight = this.offHandHeight;
      LocalPlayer localplayer = this.minecraft.player;
      ItemStack itemstack = localplayer.getMainHandItem();
      ItemStack itemstack1 = localplayer.getOffhandItem();
      if (ItemStack.matches(this.mainHandItem, itemstack)) {
         this.mainHandItem = itemstack;
      }

      if (ItemStack.matches(this.offHandItem, itemstack1)) {
         this.offHandItem = itemstack1;
      }

      if (localplayer.isHandsBusy()) {
         this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
         this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
      } else {
         float f = localplayer.getAttackStrengthScale(1.0F);
         boolean requipM = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.mainHandItem, itemstack, localplayer.getInventory().selected);
         boolean requipO = net.minecraftforge.client.ForgeHooksClient.shouldCauseReequipAnimation(this.offHandItem, itemstack1, -1);

         if (!requipM && this.mainHandItem != itemstack)
            this.mainHandItem = itemstack;
         if (!requipO && this.offHandItem != itemstack1)
            this.offHandItem = itemstack1;

         this.mainHandHeight += Mth.clamp((!requipM ? f * f * f : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
         this.offHandHeight += Mth.clamp((float)(!requipO ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
      }

      if (this.mainHandHeight < 0.1F) {
         this.mainHandItem = itemstack;
      }

      if (this.offHandHeight < 0.1F) {
         this.offHandItem = itemstack1;
      }

   }

   public void itemUsed(InteractionHand pHand) {
      if (pHand == InteractionHand.MAIN_HAND) {
         this.mainHandHeight = 0.0F;
      } else {
         this.offHandHeight = 0.0F;
      }

   }

   @OnlyIn(Dist.CLIENT)
   @VisibleForTesting
   static enum HandRenderSelection {
      RENDER_BOTH_HANDS(true, true),
      RENDER_MAIN_HAND_ONLY(true, false),
      RENDER_OFF_HAND_ONLY(false, true);

      final boolean renderMainHand;
      final boolean renderOffHand;

      private HandRenderSelection(boolean pRenderMainHand, boolean pRenderOffHand) {
         this.renderMainHand = pRenderMainHand;
         this.renderOffHand = pRenderOffHand;
      }

      public static ItemInHandRenderer.HandRenderSelection onlyForHand(InteractionHand pHand) {
         return pHand == InteractionHand.MAIN_HAND ? RENDER_MAIN_HAND_ONLY : RENDER_OFF_HAND_ONLY;
      }
   }
}
