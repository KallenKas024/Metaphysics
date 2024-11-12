package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchItemLayer<T extends LivingEntity> extends CrossedArmsItemLayer<T, WitchModel<T>> {
   public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> pRenderer, ItemInHandRenderer pItemInHandRenderer) {
      super(pRenderer, pItemInHandRenderer);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      ItemStack itemstack = pLivingEntity.getMainHandItem();
      pPoseStack.pushPose();
      if (itemstack.is(Items.POTION)) {
         this.getParentModel().getHead().translateAndRotate(pPoseStack);
         this.getParentModel().getNose().translateAndRotate(pPoseStack);
         pPoseStack.translate(0.0625F, 0.25F, 0.0F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
         pPoseStack.mulPose(Axis.XP.rotationDegrees(140.0F));
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
         pPoseStack.translate(0.0F, -0.4F, 0.4F);
      }

      super.render(pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      pPoseStack.popPose();
   }
}