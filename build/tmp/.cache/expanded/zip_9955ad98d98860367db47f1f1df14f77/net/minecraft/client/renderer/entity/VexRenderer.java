package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.VexModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VexRenderer extends MobRenderer<Vex, VexModel> {
   private static final ResourceLocation VEX_LOCATION = new ResourceLocation("textures/entity/illager/vex.png");
   private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("textures/entity/illager/vex_charging.png");

   public VexRenderer(EntityRendererProvider.Context p_174435_) {
      super(p_174435_, new VexModel(p_174435_.bakeLayer(ModelLayers.VEX)), 0.3F);
      this.addLayer(new ItemInHandLayer<>(this, p_174435_.getItemInHandRenderer()));
   }

   protected int getBlockLightLevel(Vex pEntity, BlockPos pPos) {
      return 15;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Vex pEntity) {
      return pEntity.isCharging() ? VEX_CHARGING_LOCATION : VEX_LOCATION;
   }
}