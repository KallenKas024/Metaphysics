package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
   private final ItemInHandRenderer itemInHandRenderer;
   private static final float X_ROT_MIN = (-(float)Math.PI / 6F);
   private static final float X_ROT_MAX = ((float)Math.PI / 2F);

   public PlayerItemInHandLayer(RenderLayerParent<T, M> pRenderer, ItemInHandRenderer pItemInHandRenderer) {
      super(pRenderer, pItemInHandRenderer);
      this.itemInHandRenderer = pItemInHandRenderer;
   }

   protected void renderArmWithItem(LivingEntity pLivingEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, HumanoidArm pArm, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      if (pItemStack.is(Items.SPYGLASS) && pLivingEntity.getUseItem() == pItemStack && pLivingEntity.swingTime == 0) {
         this.renderArmWithSpyglass(pLivingEntity, pItemStack, pArm, pPoseStack, pBuffer, pPackedLight);
      } else {
         super.renderArmWithItem(pLivingEntity, pItemStack, pDisplayContext, pArm, pPoseStack, pBuffer, pPackedLight);
      }

   }

   private void renderArmWithSpyglass(LivingEntity pEntity, ItemStack pStack, HumanoidArm pArm, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight) {
      pPoseStack.pushPose();
      ModelPart modelpart = this.getParentModel().getHead();
      float f = modelpart.xRot;
      modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float)Math.PI / 6F), ((float)Math.PI / 2F));
      modelpart.translateAndRotate(pPoseStack);
      modelpart.xRot = f;
      CustomHeadLayer.translateToHead(pPoseStack, false);
      boolean flag = pArm == HumanoidArm.LEFT;
      pPoseStack.translate((flag ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
      this.itemInHandRenderer.renderItem(pEntity, pStack, ItemDisplayContext.HEAD, false, pPoseStack, pBuffer, pCombinedLight);
      pPoseStack.popPose();
   }
}