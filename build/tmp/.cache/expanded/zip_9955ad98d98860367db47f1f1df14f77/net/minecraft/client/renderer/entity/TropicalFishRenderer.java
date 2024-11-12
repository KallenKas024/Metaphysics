package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ColorableHierarchicalModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.TropicalFishModelB;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TropicalFishRenderer extends MobRenderer<TropicalFish, ColorableHierarchicalModel<TropicalFish>> {
   /** Breaking recompile intentionally since modelA/B incorrectly mapped. */
   private final ColorableHierarchicalModel<TropicalFish> modelA = this.getModel();
   /** Breaking recompile intentionally since modelA/B incorrectly mapped. */
   private final ColorableHierarchicalModel<TropicalFish> modelB;
   private static final ResourceLocation MODEL_A_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_a.png");
   private static final ResourceLocation MODEL_B_TEXTURE = new ResourceLocation("textures/entity/fish/tropical_b.png");

   public TropicalFishRenderer(EntityRendererProvider.Context p_174428_) {
      super(p_174428_, new TropicalFishModelA<>(p_174428_.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15F);
      this.modelB = new TropicalFishModelB<>(p_174428_.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
      this.addLayer(new TropicalFishPatternLayer(this, p_174428_.getModelSet()));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(TropicalFish pEntity) {
      ResourceLocation resourcelocation;
      switch (pEntity.getVariant().base()) {
         case SMALL:
            resourcelocation = MODEL_A_TEXTURE;
            break;
         case LARGE:
            resourcelocation = MODEL_B_TEXTURE;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return resourcelocation;
   }

   public void render(TropicalFish pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      ColorableHierarchicalModel colorablehierarchicalmodel1;
      switch (pEntity.getVariant().base()) {
         case SMALL:
            colorablehierarchicalmodel1 = this.modelA;
            break;
         case LARGE:
            colorablehierarchicalmodel1 = this.modelB;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      ColorableHierarchicalModel<TropicalFish> colorablehierarchicalmodel = colorablehierarchicalmodel1;
      this.model = colorablehierarchicalmodel;
      float[] afloat = pEntity.getBaseColor().getTextureDiffuseColors();
      colorablehierarchicalmodel.setColor(afloat[0], afloat[1], afloat[2]);
      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
      colorablehierarchicalmodel.setColor(1.0F, 1.0F, 1.0F);
   }

   protected void setupRotations(TropicalFish pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * Mth.sin(0.6F * pAgeInTicks);
      pPoseStack.mulPose(Axis.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pPoseStack.translate(0.2F, 0.1F, 0.0F);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
      }

   }
}