package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LlamaRenderer extends MobRenderer<Llama, LlamaModel<Llama>> {
   private static final ResourceLocation CREAMY = new ResourceLocation("textures/entity/llama/creamy.png");
   private static final ResourceLocation WHITE = new ResourceLocation("textures/entity/llama/white.png");
   private static final ResourceLocation BROWN = new ResourceLocation("textures/entity/llama/brown.png");
   private static final ResourceLocation GRAY = new ResourceLocation("textures/entity/llama/gray.png");

   public LlamaRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pLayer) {
      super(pContext, new LlamaModel<>(pContext.bakeLayer(pLayer)), 0.7F);
      this.addLayer(new LlamaDecorLayer(this, pContext.getModelSet()));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(Llama pEntity) {
      ResourceLocation resourcelocation;
      switch (pEntity.getVariant()) {
         case CREAMY:
            resourcelocation = CREAMY;
            break;
         case WHITE:
            resourcelocation = WHITE;
            break;
         case BROWN:
            resourcelocation = BROWN;
            break;
         case GRAY:
            resourcelocation = GRAY;
            break;
         default:
            throw new IncompatibleClassChangeError();
      }

      return resourcelocation;
   }
}