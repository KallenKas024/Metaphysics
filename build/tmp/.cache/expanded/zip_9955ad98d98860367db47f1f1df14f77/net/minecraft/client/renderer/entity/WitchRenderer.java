package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.WitchItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Witch;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitchRenderer extends MobRenderer<Witch, WitchModel<Witch>> {
   private static final ResourceLocation WITCH_LOCATION = new ResourceLocation("textures/entity/witch.png");

   public WitchRenderer(EntityRendererProvider.Context p_174443_) {
      super(p_174443_, new WitchModel<>(p_174443_.bakeLayer(ModelLayers.WITCH)), 0.5F);
      this.addLayer(new WitchItemLayer<>(this, p_174443_.getItemInHandRenderer()));
   }

   public void render(Witch pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      this.model.setHoldingItem(!pEntity.getMainHandItem().isEmpty());
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Witch pEntity) {
      return WITCH_LOCATION;
   }

   protected void scale(Witch pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      float f = 0.9375F;
      pPoseStack.scale(0.9375F, 0.9375F, 0.9375F);
   }
}