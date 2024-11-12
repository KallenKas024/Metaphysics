package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry that always generates a given item.
 */
public class LootItem extends LootPoolSingletonContainer {
   final Item item;

   LootItem(Item pItem, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.item = pItem;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.ITEM;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      pStackConsumer.accept(new ItemStack(this.item));
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike pItem) {
      return simpleBuilder((p_79583_, p_79584_, p_79585_, p_79586_) -> {
         return new LootItem(pItem.asItem(), p_79583_, p_79584_, p_79585_, p_79586_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<LootItem> {
      public void serializeCustom(JsonObject pObject, LootItem pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(pContext.item);
         if (resourcelocation == null) {
            throw new IllegalArgumentException("Can't serialize unknown item " + pContext.item);
         } else {
            pObject.addProperty("name", resourcelocation.toString());
         }
      }

      protected LootItem deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
         Item item = GsonHelper.getAsItem(pObject, "name");
         return new LootItem(item, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}