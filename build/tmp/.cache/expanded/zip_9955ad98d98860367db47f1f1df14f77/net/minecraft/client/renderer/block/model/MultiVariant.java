package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiVariant implements UnbakedModel {
   private final List<Variant> variants;

   public MultiVariant(List<Variant> pVariants) {
      this.variants = pVariants;
   }

   public List<Variant> getVariants() {
      return this.variants;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther instanceof MultiVariant) {
         MultiVariant multivariant = (MultiVariant)pOther;
         return this.variants.equals(multivariant.variants);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.variants.hashCode();
   }

   public Collection<ResourceLocation> getDependencies() {
      return this.getVariants().stream().map(Variant::getModelLocation).collect(Collectors.toSet());
   }

   public void resolveParents(Function<ResourceLocation, UnbakedModel> pResolver) {
      this.getVariants().stream().map(Variant::getModelLocation).distinct().forEach((p_247934_) -> {
         pResolver.apply(p_247934_).resolveParents(pResolver);
      });
   }

   @Nullable
   public BakedModel bake(ModelBaker pBaker, Function<Material, TextureAtlasSprite> pSpriteGetter, ModelState pState, ResourceLocation pLocation) {
      if (this.getVariants().isEmpty()) {
         return null;
      } else {
         WeightedBakedModel.Builder weightedbakedmodel$builder = new WeightedBakedModel.Builder();

         for(Variant variant : this.getVariants()) {
            BakedModel bakedmodel = pBaker.bake(variant.getModelLocation(), variant, pSpriteGetter);
            weightedbakedmodel$builder.add(bakedmodel, variant.getWeight());
         }

         return weightedbakedmodel$builder.build();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Deserializer implements JsonDeserializer<MultiVariant> {
      public MultiVariant deserialize(JsonElement pJson, Type pType, JsonDeserializationContext pContext) throws JsonParseException {
         List<Variant> list = Lists.newArrayList();
         if (pJson.isJsonArray()) {
            JsonArray jsonarray = pJson.getAsJsonArray();
            if (jsonarray.size() == 0) {
               throw new JsonParseException("Empty variant array");
            }

            for(JsonElement jsonelement : jsonarray) {
               list.add(pContext.deserialize(jsonelement, Variant.class));
            }
         } else {
            list.add(pContext.deserialize(pJson, Variant.class));
         }

         return new MultiVariant(list);
      }
   }
}
