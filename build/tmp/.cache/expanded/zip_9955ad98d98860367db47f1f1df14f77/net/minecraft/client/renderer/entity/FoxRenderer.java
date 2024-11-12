package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxRenderer extends MobRenderer<Fox, FoxModel<Fox>> {
   private static final ResourceLocation RED_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/fox.png");
   private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/fox_sleep.png");
   private static final ResourceLocation SNOW_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox.png");
   private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

   public FoxRenderer(EntityRendererProvider.Context p_174127_) {
      super(p_174127_, new FoxModel<>(p_174127_.bakeLayer(ModelLayers.FOX)), 0.4F);
      this.addLayer(new FoxHeldItemLayer(this, p_174127_.getItemInHandRenderer()));
   }

   protected void setupRotations(Fox pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      if (pEntityLiving.isPouncing() || pEntityLiving.isFaceplanted()) {
         float f = -Mth.lerp(pPartialTicks, pEntityLiving.xRotO, pEntityLiving.getXRot());
         pPoseStack.mulPose(Axis.XP.rotationDegrees(f));
      }

   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Fox pEntity) {
      if (pEntity.getVariant() == Fox.Type.RED) {
         return pEntity.isSleeping() ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
      } else {
         return pEntity.isSleeping() ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
      }
   }
}