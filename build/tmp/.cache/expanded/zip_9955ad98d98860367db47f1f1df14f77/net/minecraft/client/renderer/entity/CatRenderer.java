package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.CatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatRenderer extends MobRenderer<Cat, CatModel<Cat>> {
   public CatRenderer(EntityRendererProvider.Context p_173943_) {
      super(p_173943_, new CatModel<>(p_173943_.bakeLayer(ModelLayers.CAT)), 0.4F);
      this.addLayer(new CatCollarLayer(this, p_173943_.getModelSet()));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Cat pEntity) {
      return pEntity.getResourceLocation();
   }

   protected void scale(Cat pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      super.scale(pLivingEntity, pPoseStack, pPartialTickTime);
      pPoseStack.scale(0.8F, 0.8F, 0.8F);
   }

   protected void setupRotations(Cat pEntityLiving, PoseStack pPoseStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pPoseStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = pEntityLiving.getLieDownAmount(pPartialTicks);
      if (f > 0.0F) {
         pPoseStack.translate(0.4F * f, 0.15F * f, 0.1F * f);
         pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(f, 0.0F, 90.0F)));
         BlockPos blockpos = pEntityLiving.blockPosition();

         for(Player player : pEntityLiving.level().getEntitiesOfClass(Player.class, (new AABB(blockpos)).inflate(2.0D, 2.0D, 2.0D))) {
            if (player.isSleeping()) {
               pPoseStack.translate(0.15F * f, 0.0F, 0.0F);
               break;
            }
         }
      }

   }
}