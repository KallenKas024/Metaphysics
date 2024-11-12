package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.LavaSlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MagmaCubeRenderer extends MobRenderer<MagmaCube, LavaSlimeModel<MagmaCube>> {
   private static final ResourceLocation MAGMACUBE_LOCATION = new ResourceLocation("textures/entity/slime/magmacube.png");

   public MagmaCubeRenderer(EntityRendererProvider.Context p_174298_) {
      super(p_174298_, new LavaSlimeModel<>(p_174298_.bakeLayer(ModelLayers.MAGMA_CUBE)), 0.25F);
   }

   protected int getBlockLightLevel(MagmaCube pEntity, BlockPos pPos) {
      return 15;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(MagmaCube pEntity) {
      return MAGMACUBE_LOCATION;
   }

   public void render(MagmaCube pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      this.shadowRadius = 0.25F * (float)pEntity.getSize();
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   protected void scale(MagmaCube pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      int i = pLivingEntity.getSize();
      float f = Mth.lerp(pPartialTickTime, pLivingEntity.oSquish, pLivingEntity.squish) / ((float)i * 0.5F + 1.0F);
      float f1 = 1.0F / (f + 1.0F);
      pPoseStack.scale(f1 * (float)i, 1.0F / f1 * (float)i, f1 * (float)i);
   }
}