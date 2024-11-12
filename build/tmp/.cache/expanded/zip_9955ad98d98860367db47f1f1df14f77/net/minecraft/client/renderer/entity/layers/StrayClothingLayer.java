package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StrayClothingLayer<T extends Mob & RangedAttackMob, M extends EntityModel<T>> extends RenderLayer<T, M> {
   private static final ResourceLocation STRAY_CLOTHES_LOCATION = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
   private final SkeletonModel<T> layerModel;

   public StrayClothingLayer(RenderLayerParent<T, M> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.layerModel = new SkeletonModel<>(pModelSet.bakeLayer(ModelLayers.STRAY_OUTER_LAYER));
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      coloredCutoutModelCopyLayerRender(this.getParentModel(), this.layerModel, STRAY_CLOTHES_LOCATION, pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, 1.0F, 1.0F, 1.0F);
   }
}