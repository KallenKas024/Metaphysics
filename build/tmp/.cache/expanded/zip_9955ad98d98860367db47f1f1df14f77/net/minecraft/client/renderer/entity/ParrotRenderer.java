package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotRenderer extends MobRenderer<Parrot, ParrotModel> {
   private static final ResourceLocation RED_BLUE = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
   private static final ResourceLocation BLUE = new ResourceLocation("textures/entity/parrot/parrot_blue.png");
   private static final ResourceLocation GREEN = new ResourceLocation("textures/entity/parrot/parrot_green.png");
   private static final ResourceLocation YELLOW_BLUE = new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png");
   private static final ResourceLocation GREY = new ResourceLocation("textures/entity/parrot/parrot_grey.png");

   public ParrotRenderer(EntityRendererProvider.Context p_174336_) {
      super(p_174336_, new ParrotModel(p_174336_.bakeLayer(ModelLayers.PARROT)), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Parrot pEntity) {
      return getVariantTexture(pEntity.getVariant());
   }

   public static ResourceLocation getVariantTexture(Parrot.Variant pVariant) {
      ResourceLocation resourcelocation;
      switch (pVariant) {
         case RED_BLUE:
            resourcelocation = RED_BLUE;
            break;
         case BLUE:
            resourcelocation = BLUE;
            break;
         case GREEN:
            resourcelocation = GREEN;
            break;
         case YELLOW_BLUE:
            resourcelocation = YELLOW_BLUE;
            break;
         case GRAY:
            resourcelocation = GREY;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return resourcelocation;
   }

   /**
    * Defines what float the third param in setRotationAngles of ModelBase is
    */
   public float getBob(Parrot pLivingBase, float pPartialTicks) {
      float f = Mth.lerp(pPartialTicks, pLivingBase.oFlap, pLivingBase.flap);
      float f1 = Mth.lerp(pPartialTicks, pLivingBase.oFlapSpeed, pLivingBase.flapSpeed);
      return (Mth.sin(f) + 1.0F) * f1;
   }
}