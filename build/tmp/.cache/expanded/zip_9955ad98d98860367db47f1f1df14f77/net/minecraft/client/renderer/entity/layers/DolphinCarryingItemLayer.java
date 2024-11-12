package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinCarryingItemLayer extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
   private final ItemInHandRenderer itemInHandRenderer;

   public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> pRenderer, ItemInHandRenderer pItemInHandRenderer) {
      super(pRenderer);
      this.itemInHandRenderer = pItemInHandRenderer;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Dolphin pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = pLivingEntity.getMainArm() == HumanoidArm.RIGHT;
      pPoseStack.pushPose();
      float f = 1.0F;
      float f1 = -1.0F;
      float f2 = Mth.abs(pLivingEntity.getXRot()) / 60.0F;
      if (pLivingEntity.getXRot() < 0.0F) {
         pPoseStack.translate(0.0F, 1.0F - f2 * 0.5F, -1.0F + f2 * 0.5F);
      } else {
         pPoseStack.translate(0.0F, 1.0F + f2 * 0.8F, -1.0F + f2 * 0.2F);
      }

      ItemStack itemstack = flag ? pLivingEntity.getMainHandItem() : pLivingEntity.getOffhandItem();
      this.itemInHandRenderer.renderItem(pLivingEntity, itemstack, ItemDisplayContext.GROUND, false, pPoseStack, pBuffer, pPackedLight);
      pPoseStack.popPose();
   }
}