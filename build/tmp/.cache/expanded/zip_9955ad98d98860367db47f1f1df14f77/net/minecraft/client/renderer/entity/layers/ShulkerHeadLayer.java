package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerHeadLayer extends RenderLayer<Shulker, ShulkerModel<Shulker>> {
   public ShulkerHeadLayer(RenderLayerParent<Shulker, ShulkerModel<Shulker>> pRenderer) {
      super(pRenderer);
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Shulker pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      ResourceLocation resourcelocation = ShulkerRenderer.getTextureLocation(pLivingEntity.getColor());
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(resourcelocation));
      this.getParentModel().getHead().render(pPoseStack, vertexconsumer, pPackedLight, LivingEntityRenderer.getOverlayCoords(pLivingEntity, 0.0F));
   }
}