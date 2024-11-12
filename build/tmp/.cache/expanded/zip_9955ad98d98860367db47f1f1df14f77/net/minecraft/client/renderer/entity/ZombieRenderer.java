package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieRenderer extends AbstractZombieRenderer<Zombie, ZombieModel<Zombie>> {
   public ZombieRenderer(EntityRendererProvider.Context p_174456_) {
      this(p_174456_, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);
   }

   public ZombieRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pZombieLayer, ModelLayerLocation pInnerArmor, ModelLayerLocation pOuterArmor) {
      super(pContext, new ZombieModel<>(pContext.bakeLayer(pZombieLayer)), new ZombieModel<>(pContext.bakeLayer(pInnerArmor)), new ZombieModel<>(pContext.bakeLayer(pOuterArmor)));
   }
}