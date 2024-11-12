package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * A loot pool entry container that generates based on an item tag.
 * If {@code expand} is set to true, it will expand into separate LootPoolEntries for every item in the tag, otherwise
 * it will simply generate all items in the tag.
 */
public class TagEntry extends LootPoolSingletonContainer {
   final TagKey<Item> tag;
   final boolean expand;

   TagEntry(TagKey<Item> pTag, boolean pExpand, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
      super(pWeight, pQuality, pConditions, pFunctions);
      this.tag = pTag;
      this.expand = pExpand;
   }

   public LootPoolEntryType getType() {
      return LootPoolEntries.TAG;
   }

   /**
    * Generate the loot stacks of this entry.
    * Contrary to the method name this method does not always generate one stack, it can also generate zero or multiple
    * stacks.
    */
   public void createItemStack(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach((p_205094_) -> {
         pStackConsumer.accept(new ItemStack(p_205094_));
      });
   }

   private boolean expandTag(LootContext pContext, Consumer<LootPoolEntry> pGeneratorConsumer) {
      if (!this.canRun(pContext)) {
         return false;
      } else {
         for(final Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
            pGeneratorConsumer.accept(new LootPoolSingletonContainer.EntryBase() {
               /**
                * Generate the loot stacks of this entry.
                * Contrary to the method name this method does not always generate one stack, it can also generate zero
                * or multiple stacks.
                */
               public void createItemStack(Consumer<ItemStack> p_79869_, LootContext p_79870_) {
                  p_79869_.accept(new ItemStack(holder));
               }
            });
         }

         return true;
      }
   }

   /**
    * Expand this loot pool entry container by calling {@code entryConsumer} with any applicable entries
    * 
    * @return whether this loot pool entry container successfully expanded or not
    */
   public boolean expand(LootContext pLootContext, Consumer<LootPoolEntry> pEntryConsumer) {
      return this.expand ? this.expandTag(pLootContext, pEntryConsumer) : super.expand(pLootContext, pEntryConsumer);
   }

   public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> pTag) {
      return simpleBuilder((p_205099_, p_205100_, p_205101_, p_205102_) -> {
         return new TagEntry(pTag, false, p_205099_, p_205100_, p_205101_, p_205102_);
      });
   }

   public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> pTag) {
      return simpleBuilder((p_205088_, p_205089_, p_205090_, p_205091_) -> {
         return new TagEntry(pTag, true, p_205088_, p_205089_, p_205090_, p_205091_);
      });
   }

   public static class Serializer extends LootPoolSingletonContainer.Serializer<TagEntry> {
      public void serializeCustom(JsonObject pObject, TagEntry pContext, JsonSerializationContext pConditions) {
         super.serializeCustom(pObject, pContext, pConditions);
         pObject.addProperty("name", pContext.tag.location().toString());
         pObject.addProperty("expand", pContext.expand);
      }

      protected TagEntry deserialize(JsonObject pObject, JsonDeserializationContext pContext, int pWeight, int pQuality, LootItemCondition[] pConditions, LootItemFunction[] pFunctions) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pObject, "name"));
         TagKey<Item> tagkey = TagKey.create(Registries.ITEM, resourcelocation);
         boolean flag = GsonHelper.getAsBoolean(pObject, "expand");
         return new TagEntry(tagkey, flag, pWeight, pQuality, pConditions, pFunctions);
      }
   }
}