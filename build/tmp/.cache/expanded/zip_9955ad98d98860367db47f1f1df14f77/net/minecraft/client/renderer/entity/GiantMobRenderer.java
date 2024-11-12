package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantMobRenderer extends MobRenderer<Giant, HumanoidModel<Giant>> {
   private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");
   private final float scale;

   public GiantMobRenderer(EntityRendererProvider.Context pContext, float pScale) {
      super(pContext, new GiantZombieModel(pContext.bakeLayer(ModelLayers.GIANT)), 0.5F * pScale);
      this.scale = pScale;
      this.addLayer(new ItemInHandLayer<>(this, pContext.getItemInHandRenderer()));
      this.addLayer(new HumanoidArmorLayer<>(this, new GiantZombieModel(pContext.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)), new GiantZombieModel(pContext.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR)), pContext.getModelManager()));
   }

   protected void scale(Giant pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
      pPoseStack.scale(this.scale, this.scale, this.scale);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Giant pEntity) {
      return ZOMBIE_LOCATION;
   }
}