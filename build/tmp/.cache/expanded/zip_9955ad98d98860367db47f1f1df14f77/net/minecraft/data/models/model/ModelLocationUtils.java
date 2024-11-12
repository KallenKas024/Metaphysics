package net.minecraft.data.models.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelLocationUtils {
   /** @deprecated */
   @Deprecated
   public static ResourceLocation decorateBlockModelLocation(String pBlockModelLocation) {
      return new ResourceLocation("minecraft", "block/" + pBlockModelLocation);
   }

   public static ResourceLocation decorateItemModelLocation(String pItemModelLocation) {
      return new ResourceLocation("minecraft", "item/" + pItemModelLocation);
   }

   public static ResourceLocation getModelLocation(Block pBlock, String pModelLocationSuffix) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(pBlock);
      return resourcelocation.withPath((p_251253_) -> {
         return "block/" + p_251253_ + pModelLocationSuffix;
      });
   }

   public static ResourceLocation getModelLocation(Block pBlock) {
      ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(pBlock);
      return resourcelocation.withPrefix("block/");
   }

   public static ResourceLocation getModelLocation(Item pItem) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(pItem);
      return resourcelocation.withPrefix("item/");
   }

   public static ResourceLocation getModelLocation(Item pItem, String pModelLocationSuffix) {
      ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(pItem);
      return resourcelocation.withPath((p_251542_) -> {
         return "item/" + p_251542_ + pModelLocationSuffix;
      });
   }
}