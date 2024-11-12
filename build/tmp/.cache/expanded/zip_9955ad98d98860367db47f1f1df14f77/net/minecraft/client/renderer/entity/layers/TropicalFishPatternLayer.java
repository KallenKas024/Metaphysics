package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishPatternLayer extends RenderLayer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
   private static final ResourceLocation KOB_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_1.png");
   private static final ResourceLocation SUNSTREAK_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_2.png");
   private static final ResourceLocation SNOOPER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_3.png");
   private static final ResourceLocation DASHER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_4.png");
   private static final ResourceLocation BRINELY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_5.png");
   private static final ResourceLocation SPOTTY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a_pattern_6.png");
   private static final ResourceLocation FLOPPER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_1.png");
   private static final ResourceLocation STRIPEY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_2.png");
   private static final ResourceLocation GLITTER_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_3.png");
   private static final ResourceLocation BLOCKFISH_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_4.png");
   private static final ResourceLocation BETTY_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_5.png");
   private static final ResourceLocation CLAYFISH_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b_pattern_6.png");
   private final TropicalFishModelA<TropicalFish> modelA;
   private final TropicalFishModelB<TropicalFish> modelB;

   public TropicalFishPatternLayer(RenderLayerParent<TropicalFish, ColorableHierarchicalModel<TropicalFish>> pRenderer, EntityModelSet pModelSet) {
      super(pRenderer);
      this.modelA = new TropicalFishModelA<>(pModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL_PATTERN));
      this.modelB = new TropicalFishModelB<>(pModelSet.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE_PATTERN));
   }

   public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, TropicalFish pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      TropicalFish.Pattern tropicalfish$pattern = pLivingEntity.getVariant();
      Object object;
      switch (tropicalfish$pattern.base()) {
         case SMALL:
            object = this.modelA;
            break;
         case LARGE:
            object = this.modelB;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      EntityModel<TropicalFish> entitymodel = (EntityModel<TropicalFish>)object;
      ResourceLocation resourcelocation1;
      switch (tropicalfish$pattern) {
         case KOB:
            resourcelocation1 = KOB_TEXTURE;
            break;
         case SUNSTREAK:
            resourcelocation1 = SUNSTREAK_TEXTURE;
            break;
         case SNOOPER:
            resourcelocation1 = SNOOPER_TEXTURE;
            break;
         case DASHER:
            resourcelocation1 = DASHER_TEXTURE;
            break;
         case BRINELY:
            resourcelocation1 = BRINELY_TEXTURE;
            break;
         case SPOTTY:
            resourcelocation1 = SPOTTY_TEXTURE;
            break;
         case FLOPPER:
            resourcelocation1 = FLOPPER_TEXTURE;
            break;
         case STRIPEY:
            resourcelocation1 = STRIPEY_TEXTURE;
            break;
         case GLITTER:
            resourcelocation1 = GLITTER_TEXTURE;
            break;
         case BLOCKFISH:
            resourcelocation1 = BLOCKFISH_TEXTURE;
            break;
         case BETTY:
            resourcelocation1 = BETTY_TEXTURE;
            break;
         case CLAYFISH:
            resourcelocation1 = CLAYFISH_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      ResourceLocation resourcelocation = resourcelocation1;
      float[] afloat = pLivingEntity.getPatternColor().getTextureDiffuseColors();
      coloredCutoutModelCopyLayerRender(this.getParentModel(), entitymodel, resourcelocation, pPoseStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch, pPartialTicks, afloat[0], afloat[1], afloat[2]);
   }
}