package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry container that generates loot by referencing another loot table.
 */
public class LootTableReference extends LootPoolSingletonContainer {
   final ResourceLocation name;

   LootTableReference(ResourceLocation pLootTableId, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.name = pLootTableId;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.REFERENCE;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      LootTable loottable = pLootContext.getResolver().getLootTable(this.name);
      loottable.getRandomItemsRaw(pLootContext, pStackConsumer);
   }

   public void validate(ValidationContext pValidationContext) {
      LootDataId<LootTable> lootdataid = new LootDataId<>(LootDataType.TABLE, this.name);
      if (pValidationContext.hasVisitedElement(lootdataid)) {
         pValidationContext.reportProblem("Table " + this.name + " is recursively called");
      } else {
         super.validate(pValidationContext);
         pValidationContext.resolver().getElementOptional(lootdataid).ifPresentOrElse((p_279078_) -> {
            p_279078_.validate(pValidationContext.enterElement("->{" + this.name + "}", lootdataid));
         }, () -> {
            pValidationContext.reportProblem("Unknown loot table called " + this.name);
         });
      }
   }

   public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceLocation pTable) {
      return simpleBuilder((p_79780_, p_79781_, p_79782_, p_79783_) -> {
         return new LootTableReference(pTable, p_79780_, p_79781_, p_79782_, p_79783_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<LootTableReference> {
      public void serializeCustom(JsonObject pObject, LootTableReference pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         pObject.addProperty("name", pContext.name.toString());
      }

      protected LootTableReference deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pObject, "name"));
         return new LootTableReference(resourcelocation, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}