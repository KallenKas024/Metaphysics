package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * LootItemFunction that sets a stack's enchantments. If {@code add} is set, will add to any already existing
 * enchantment levels instead of replacing them (ignored for enchanted books).
 */
public class SetEnchantmentsFunction extends LootItemConditionalFunction {
   final Map<Enchantment, NumberProvider> enchantments;
   final boolean add;

   SetEnchantmentsFunction(LootItemCondition[] pConditions, Map<Enchantment, NumberProvider> pEnchantmentLevels, boolean pAdd) {
      super(pConditions);
      this.enchantments = ImmutableMap.copyOf(pEnchantmentLevels);
      this.add = pAdd;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.SET_ENCHANTMENTS;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return this.enchantments.values().stream().flatMap((p_279081_) -> {
         return p_279081_.getReferencedContextParams().stream();
      }).collect(ImmutableSet.toImmutableSet());
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      Object2IntMap<Enchantment> object2intmap = new Object2IntOpenHashMap<>();
      this.enchantments.forEach((p_165353_, p_165354_) -> {
         object2intmap.put(p_165353_, p_165354_.getInt(pContext));
      });
      if (pStack.getItem() == Items.BOOK) {
         ItemStack itemstack = new ItemStack(Items.ENCHANTED_BOOK);
         object2intmap.forEach((p_165343_, p_165344_) -> {
            EnchantedBookItem.addEnchantment(itemstack, new EnchantmentInstance(p_165343_, p_165344_));
         });
         return itemstack;
      } else {
         Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(pStack);
         if (this.add) {
            object2intmap.forEach((p_165366_, p_165367_) -> {
               updateEnchantment(map, p_165366_, Math.max(map.getOrDefault(p_165366_, 0) + p_165367_, 0));
            });
         } else {
            object2intmap.forEach((p_165361_, p_165362_) -> {
               updateEnchantment(map, p_165361_, Math.max(p_165362_, 0));
            });
         }

         EnchantmentHelper.setEnchantments(map, pStack);
         return pStack;
      }
   }

   private static void updateEnchantment(Map<Enchantment, Integer> pExistingEnchantments, Enchantment pEnchantment, int pLevel) {
      if (pLevel == 0) {
         pExistingEnchantments.remove(pEnchantment);
      } else {
         pExistingEnchantments.put(pEnchantment, pLevel);
      }

   }

   public static class Builder extends LootItemConditionalFunction.Builder<SetEnchantmentsFunction.Builder> {
      private final Map<Enchantment, NumberProvider> enchantments = Maps.newHashMap();
      private final boolean add;

      public Builder() {
         this(false);
      }

      public Builder(boolean pAdd) {
         this.add = pAdd;
      }

      protected SetEnchantmentsFunction.Builder getThis() {
         return this;
      }

      public SetEnchantmentsFunction.Builder withEnchantment(Enchantment pEnchantment, NumberProvider pLevelProvider) {
         this.enchantments.put(pEnchantment, pLevelProvider);
         return this;
      }

      public LootItemFunction build() {
         return new SetEnchantmentsFunction(this.getConditions(), this.enchantments, this.add);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<SetEnchantmentsFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetEnchantmentsFunction pLootItemConditionalFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pLootItemConditionalFunction, pSerializationContext);
         JsonObject jsonobject = new JsonObject();
         pLootItemConditionalFunction.enchantments.forEach((p_259023_, p_259024_) -> {
            ResourceLocation resourcelocation = BuiltInRegistries.ENCHANTMENT.getKey(p_259023_);
            if (resourcelocation == null) {
               throw new IllegalArgumentException("Don't know how to serialize enchantment " + p_259023_);
            } else {
               jsonobject.add(resourcelocation.toString(), pSerializationContext.serialize(p_259024_));
            }
         });
         pJson.add("enchantments", jsonobject);
         pJson.addProperty("add", pLootItemConditionalFunction.add);
      }

      public SetEnchantmentsFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         Map<Enchantment, NumberProvider> map = Maps.newHashMap();
         if (pObject.has("enchantments")) {
            JsonObject jsonobject = GsonHelper.getAsJsonObject(pObject, "enchantments");

            for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
               String s = entry.getKey();
               JsonElement jsonelement = entry.getValue();
               Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown enchantment '" + s + "'");
               });
               NumberProvider numberprovider = pDeserializationContext.deserialize(jsonelement, NumberProvider.class);
               map.put(enchantment, numberprovider);
            }
         }

         boolean flag = GsonHelper.getAsBoolean(pObject, "add", false);
         return new SetEnchantmentsFunction(pConditions, map, flag);
      }
   }
}