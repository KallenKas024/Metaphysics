package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaDecorLayer extends RenderLayer<Llama, LlamaModel<Llama>> {
   private static final ResourceLocation[] TEXTURE_LOCATION = new ResourceLocation[]{new ResourceLocation("textures/entity/llama/decor/white.png"), new ResourceLocation("textures/entity/llama/decor/orange.png"), new ResourceLocation("textures/entity/llama/decor/magenta.png"), new ResourceLocation("textures/entity/llama/decor/light_blue.png"), new ResourceLocation("textures/entity/llama/decor/yellow.png"), new ResourceLocation("textures/entity/llama/decor/lime.png"), new ResourceLocation("textures/entity/llama/decor/pink.png"), new ResourceLocation("textures/entity/llama/decor/gray.png"), new ResourceLocation("textures/entity/llama/decor/light_gray.png"), new ResourceLocation("textures/entity/llama/decor/cyan.png"), new ResourceLocation("textures/entity/llama/decor/purple.png"), new ResourceLocation("textures/entity/llama/decor/blue.png"), new ResourceLocation("textures/entity/llama/decor/brown.png"), new ResourceLocation("textures/entity/llama/decor/green.png"), new ResourceLocation("textures/entity/llama/decor/red.png"), new ResourceLocation("textures/entity/llama/decor/black.png")};
   private static final ResourceLocation TRADER_LLAMA = new ResourceLocation("textures/entity/llama/decor/trader_llama.png");
   private final LlamaModel<Llama> model;

   public LlamaDecorLayer(RenderLayerParent<Llama, LlamaModel<Llama>> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.model = new LlamaModel<>(pModelSet.bakeLayer(ModelLayers.LLAMA_DECOR));
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Llama pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      DyeColor dyecolor = pLivingEntity.getSwag();
      ResourceLocation resourcelocation;
      if (dyecolor != null) {
         resourcelocation = TEXTURE_LOCATION[dyecolor.getId()];
      } else {
         if (!pLivingEntity.isTraderLlama()) {
            return;
         }

         resourcelocation = TRADER_LLAMA;
      }

      this.getParentModel().copyPropertiesTo(this.model);
      this.model.setupAnim(pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entityCutoutNoCull(resourcelocation));
      this.model.renderToBuffer(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
   }
}