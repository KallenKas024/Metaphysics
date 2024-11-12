package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

/**
 * LootItemFunction that applies a random enchantment to the stack. If an empty list is given, chooses from all
 * enchantments.
 */
public class EnchantRandomlyFunction extends LootItemConditionalFunction {
   private static final Logger LOGGER = LogUtils.getLogger();
   final List<Enchantment> enchantments;

   EnchantRandomlyFunction(LootItemCondition[] pConditions, Collection<Enchantment> pPossibleEnchantments) {
      super(pConditions);
      this.enchantments = ImmutableList.copyOf(pPossibleEnchantments);
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.ENCHANT_RANDOMLY;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      RandomSource randomsource = pContext.getRandom();
      Enchantment enchantment;
      if (this.enchantments.isEmpty()) {
         boolean flag = pStack.is(Items.BOOK);
         List<Enchantment> list = BuiltInRegistries.ENCHANTMENT.stream().filter(Enchantment::isDiscoverable).filter((p_80436_) -> {
            return flag || p_80436_.canEnchant(pStack);
         }).collect(Collectors.toList());
         if (list.isEmpty()) {
            LOGGER.warn("Couldn't find a compatible enchantment for {}", (Object)pStack);
            return pStack;
         }

         enchantment = list.get(randomsource.nextInt(list.size()));
      } else {
         enchantment = this.enchantments.get(randomsource.nextInt(this.enchantments.size()));
      }

      return enchantItem(pStack, enchantment, randomsource);
   }

   private static ItemStack enchantItem(ItemStack pStack, Enchantment pEnchantment, RandomSource pRandom) {
      int i = Mth.nextInt(pRandom, pEnchantment.getMinLevel(), pEnchantment.getMaxLevel());
      if (pStack.is(Items.BOOK)) {
         pStack = new ItemStack(Items.ENCHANTED_BOOK);
         EnchantedBookItem.addEnchantment(pStack, new EnchantmentInstance(pEnchantment, i));
      } else {
         pStack.enchant(pEnchantment, i);
      }

      return pStack;
   }

   public static EnchantRandomlyFunction.Builder randomEnchantment() {
      return new EnchantRandomlyFunction.Builder();
   }

   public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
      return simpleBuilder((p_80438_) -> {
         return new EnchantRandomlyFunction(p_80438_, ImmutableList.of());
      });
   }

   public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
      private final Set<Enchantment> enchantments = Sets.newHashSet();

      protected EnchantRandomlyFunction.Builder getThis() {
         return this;
      }

      public EnchantRandomlyFunction.Builder withEnchantment(Enchantment pEnchantment) {
         this.enchantments.add(pEnchantment);
         return this;
      }

      public LootItemFunction build() {
         return new EnchantRandomlyFunction(this.getConditions(), this.enchantments);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantRandomlyFunction> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, EnchantRandomlyFunction pLootItemConditionalFunction, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pLootItemConditionalFunction, pSerializationContext);
         if (!pLootItemConditionalFunction.enchantments.isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(Enchantment enchantment : pLootItemConditionalFunction.enchantments) {
               ResourceLocation resourcelocation = BuiltInRegistries.ENCHANTMENT.getKey(enchantment);
               if (resourcelocation == null) {
                  throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
               }

               jsonarray.add(new JsonPrimitive(resourcelocation.toString()));
            }

            pJson.add("enchantments", jsonarray);
         }

      }

      public EnchantRandomlyFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         List<Enchantment> list = Lists.newArrayList();
         if (pObject.has("enchantments")) {
            for(JsonElement jsonelement : GsonHelper.getAsJsonArray(pObject, "enchantments")) {
               String s = GsonHelper.convertToString(jsonelement, "enchantment");
               Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
                  return new JsonSyntaxException("Unknown enchantment '" + s + "'");
               });
               list.add(enchantment);
            }
         }

         return new EnchantRandomlyFunction(pConditions, list);
      }
   }
}