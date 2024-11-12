package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleRenderer extends MobRenderer<Turtle, TurtleModel<Turtle>> {
   private static final ResourceLocation TURTLE_LOCATION = new ResourceLocation("textures/entity/turtle/big_sea_turtle.png");

   public TurtleRenderer(EntityRendererProvider.Context p_174430_) {
      super(p_174430_, new TurtleModel<>(p_174430_.bakeLayer(ModelLayers.TURTLE)), 0.7F);
   }

   public void render(Turtle pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      if (pEntity.isBaby()) {
         this.shadowRadius *= 0.5F;
      }

      super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Turtle pEntity) {
      return TURTLE_LOCATION;
   }
}