package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatCollarLayer extends RenderLayer<Cat, CatModel<Cat>> {
   private static final ResourceLocation CAT_COLLAR_LOCATION = new ResourceLocation("textures/entity/cat/cat_collar.png");
   private final CatModel<Cat> catModel;

   public CatCollarLayer(RenderLayerParent<Cat, CatModel<Cat>> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.catModel = new CatModel<>(pModelSet.bakeLayer(ModelLayers.CAT_COLLAR));
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Cat pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (pLivingEntity.isTame()) {
         float[] afloat = pLivingEntity.getCollarColor().getTextureDiffuseColors();
         coloredCutoutModelCopyLayerRender(this.getParentModel(), this.catModel, CAT_COLLAR_LOCATION, pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, afloat[0], afloat[1], afloat[2]);
      }
   }
}