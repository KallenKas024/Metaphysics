package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemOverrides {
   public static final ItemOverrides EMPTY = new ItemOverrides();
   public static final float NO_OVERRIDE = Float.NEGATIVE_INFINITY;
   private final ItemOverrides.BakedOverride[] overrides;
   private final ResourceLocation[] properties;

   protected ItemOverrides() {
      this.overrides = new ItemOverrides.BakedOverride[0];
      this.properties = new ResourceLocation[0];
   }

   /**
    * @deprecated Forge: Use {@link #ItemOverrides(ModelBaker, UnbakedModel, List, java.util.function.Function)}
    */
   @Deprecated
   public ItemOverrides(ModelBaker pBaker, BlockModel pModel, List<ItemOverride> pOverrides) {
      this(pBaker, pModel, pOverrides, pBaker.getModelTextureGetter());
   }

   public ItemOverrides(ModelBaker pBaker, UnbakedModel pModel, List<ItemOverride> pOverrides, java.util.function.Function<net.minecraft.client.resources.model.Material, net.minecraft.client.renderer.texture.TextureAtlasSprite> spriteGetter) {
      this.properties = pOverrides.stream().flatMap(ItemOverride::getPredicates).map(ItemOverride.Predicate::getProperty).distinct().toArray((p_173479_) -> {
         return new ResourceLocation[p_173479_];
      });
      Object2IntMap<ResourceLocation> object2intmap = new Object2IntOpenHashMap<>();

      for(int i = 0; i < this.properties.length; ++i) {
         object2intmap.put(this.properties[i], i);
      }

      List<ItemOverrides.BakedOverride> list = Lists.newArrayList();

      for(int j = pOverrides.size() - 1; j >= 0; --j) {
         ItemOverride itemoverride = pOverrides.get(j);
         BakedModel bakedmodel = this.bakeModel(pBaker, pModel, itemoverride, spriteGetter);
         ItemOverrides.PropertyMatcher[] aitemoverrides$propertymatcher = itemoverride.getPredicates().map((p_173477_) -> {
            int k = object2intmap.getInt(p_173477_.getProperty());
            return new ItemOverrides.PropertyMatcher(k, p_173477_.getValue());
         }).toArray((p_173463_) -> {
            return new ItemOverrides.PropertyMatcher[p_173463_];
         });
         list.add(new ItemOverrides.BakedOverride(aitemoverrides$propertymatcher, bakedmodel));
      }

      this.overrides = list.toArray(new ItemOverrides.BakedOverride[0]);
   }

   @Nullable
   private BakedModel bakeModel(ModelBaker pBaker, UnbakedModel pModel, ItemOverride pOverride, java.util.function.Function<net.minecraft.client.resources.model.Material, net.minecraft.client.renderer.texture.TextureAtlasSprite> spriteGetter) {
      UnbakedModel unbakedmodel = pBaker.getModel(pOverride.getModel());
      return Objects.equals(unbakedmodel, pModel) ? null : pBaker.bake(pOverride.getModel(), BlockModelRotation.X0_Y0, spriteGetter);
   }

   @Nullable
   public BakedModel resolve(BakedModel pModel, ItemStack pStack, @Nullable ClientLevel pLevel, @Nullable LivingEntity pEntity, int pSeed) {
      if (this.overrides.length != 0) {
         Item item = pStack.getItem();
         int i = this.properties.length;
         float[] afloat = new float[i];

         for(int j = 0; j < i; ++j) {
            ResourceLocation resourcelocation = this.properties[j];
            ItemPropertyFunction itempropertyfunction = ItemProperties.getProperty(item, resourcelocation);
            if (itempropertyfunction != null) {
               afloat[j] = itempropertyfunction.call(pStack, pLevel, pEntity, pSeed);
            } else {
               afloat[j] = Float.NEGATIVE_INFINITY;
            }
         }

         for(ItemOverrides.BakedOverride itemoverrides$bakedoverride : this.overrides) {
            if (itemoverrides$bakedoverride.test(afloat)) {
               BakedModel bakedmodel = itemoverrides$bakedoverride.model;
               if (bakedmodel == null) {
                  return pModel;
               }

               return bakedmodel;
            }
         }
      }

      return pModel;
   }

   public com.google.common.collect.ImmutableList<BakedOverride> getOverrides() {
      return com.google.common.collect.ImmutableList.copyOf(overrides);
   }

   @OnlyIn(Dist.CLIENT)
   public static class BakedOverride {
      private final ItemOverrides.PropertyMatcher[] matchers;
      @Nullable
      final BakedModel model;

      BakedOverride(ItemOverrides.PropertyMatcher[] pMatchers, @Nullable BakedModel pModel) {
         this.matchers = pMatchers;
         this.model = pModel;
      }

      boolean test(float[] pProperties) {
         for(ItemOverrides.PropertyMatcher itemoverrides$propertymatcher : this.matchers) {
            float f = pProperties[itemoverrides$propertymatcher.index];
            if (f < itemoverrides$propertymatcher.value) {
               return false;
            }
         }

         return true;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class PropertyMatcher {
      public final int index;
      public final float value;

      PropertyMatcher(int pIndex, float pValue) {
         this.index = pIndex;
         this.value = pValue;
      }
   }
}
