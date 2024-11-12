package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry container that will generate the dynamic drops with a given name.
 * 
 * @see LootContext.DynamicDrops
 */
public class DynamicLoot extends LootPoolSingletonContainer {
   final ResourceLocation name;

   DynamicLoot(ResourceLocation pDynamicDropsName, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.name = pDynamicDropsName;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.DYNAMIC;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      pLootContext.addDynamicDrops(this.name, pStackConsumer);
   }

   public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation pDynamicDropsName) {
      return simpleBuilder((p_79487_, p_79488_, p_79489_, p_79490_) -> {
         return new DynamicLoot(pDynamicDropsName, p_79487_, p_79488_, p_79489_, p_79490_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<DynamicLoot> {
      public void serializeCustom(JsonObject pObject, DynamicLoot pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         pObject.addProperty("name", pContext.name.toString());
      }

      protected DynamicLoot deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pObject, "name"));
         return new DynamicLoot(resourcelocation, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}