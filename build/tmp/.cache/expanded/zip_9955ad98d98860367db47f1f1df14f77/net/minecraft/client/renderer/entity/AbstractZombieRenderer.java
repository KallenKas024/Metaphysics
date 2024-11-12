package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, M extends ZombieModel<T>> extends HumanoidMobRenderer<T, M> {
   private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");

   protected AbstractZombieRenderer(EntityRendererProvider.Context pContext, M pModel, M pInnerModel, M pOuterModel) {
      super(pContext, pModel, 0.5F);
      this.addLayer(new HumanoidArmorLayer<>(this, pInnerModel, pOuterModel, pContext.getModelManager()));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Zombie pEntity) {
      return ZOMBIE_LOCATION;
   }

   protected boolean isShaking(T pEntity) {
      return super.isShaking(pEntity) || pEntity.isUnderWaterConverting();
   }
}