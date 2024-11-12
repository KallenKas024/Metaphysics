package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossedArmsItemLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private final ItemInHandRenderer itemInHandRenderer;

   public CrossedArmsItemLayer(RenderLayerParent<T, M> pRenderer, ItemInHandRenderer pItemInHandRenderer) {
      super(pRenderer);
      this.itemInHandRenderer = pItemInHandRenderer;
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      pPoseStack.pushPose();
      pPoseStack.translate(0.0F, 0.4F, -0.4F);
      pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
      ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlot.MAINHAND);
      this.itemInHandRenderer.renderItem(pLivingEntity, itemstack, ItemDisplayContext.GROUND, false, pPoseStack, pBuffer, pPackedLight);
      pPoseStack.popPose();
   }
}