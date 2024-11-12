package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * LootItemFunction that modifies the stack's count based on an enchantment level on the {@linkplain
 * LootContextParams#TOOL tool} using various formulas.
 */
public class ApplyBonusCount extends LootItemConditionalFunction {
   static final Map<ResourceLocation, ApplyBonusCount.FormulaDeserializer> FORMULAS = Maps.newHashMap();
   final Enchantment enchantment;
   final ApplyBonusCount.Formula formula;

   ApplyBonusCount(LootItemCondition[] pConditions, Enchantment pEnchantment, ApplyBonusCount.Formula pFormula) {
      super(pConditions);
      this.enchantment = pEnchantment;
      this.formula = pFormula;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.APPLY_BONUS;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.TOOL);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      ItemStack itemstack = pContext.getParamOrNull(LootContextParams.TOOL);
      if (itemstack != null) {
         int i = EnchantmentHelper.getItemEnchantmentLevel(this.enchantment, itemstack);
         int j = this.formula.calculateNewCount(pContext.getRandom(), pStack.getCount(), i);
         pStack.setCount(j);
      }

      return pStack;
   }

   public static LootItemConditionalFunction.Builder<?> addBonusBinomialDistributionCount(Enchantment pEnchantment, float pProbability, int pExtraRounds) {
      return simpleBuilder((p_79928_) -> {
         return new ApplyBonusCount(p_79928_, pEnchantment, new ApplyBonusCount.BinomialWithBonusCount(pExtraRounds, pProbability));
      });
   }

   public static LootItemConditionalFunction.Builder<?> addOreBonusCount(Enchantment pEnchantment) {
      return simpleBuilder((p_79943_) -> {
         return new ApplyBonusCount(p_79943_, pEnchantment, new ApplyBonusCount.OreDrops());
      });
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment pEnchantment) {
      return simpleBuilder((p_79935_) -> {
         return new ApplyBonusCount(p_79935_, pEnchantment, new ApplyBonusCount.UniformBonusCount(1));
      });
   }

   public static LootItemConditionalFunction.Builder<?> addUniformBonusCount(Enchantment pEnchantment, int pBonusMultiplier) {
      return simpleBuilder((p_79932_) -> {
         return new ApplyBonusCount(p_79932_, pEnchantment, new ApplyBonusCount.UniformBonusCount(pBonusMultiplier));
      });
   }

   static {
      FORMULAS.put(ApplyBonusCount.BinomialWithBonusCount.TYPE, ApplyBonusCount.BinomialWithBonusCount::deserialize);
      FORMULAS.put(ApplyBonusCount.OreDrops.TYPE, ApplyBonusCount.OreDrops::deserialize);
      FORMULAS.put(ApplyBonusCount.UniformBonusCount.TYPE, ApplyBonusCount.UniformBonusCount::deserialize);
   }

   /**
    * Applies a bonus based on a binomial distribution with {@code n = enchantmentLevel + extraRounds} and {@code p =
    * probability}.
    */
   static final class BinomialWithBonusCount implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("binomial_with_bonus_count");
      private final int extraRounds;
      private final float probability;

      public BinomialWithBonusCount(int pExtraRounds, float pProbability) {
         this.extraRounds = pExtraRounds;
         this.probability = pProbability;
      }

      public int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel) {
         for(int i = 0; i < pEnchantmentLevel + this.extraRounds; ++i) {
            if (pRandom.nextFloat() < this.probability) {
               ++pOriginalCount;
            }
         }

         return pOriginalCount;
      }

      public void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("extra", this.extraRounds);
         pJson.addProperty("probability", this.probability);
      }

      public static ApplyBonusCount.Formula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext) {
         int i = GsonHelper.getAsInt(pJson, "extra");
         float f = GsonHelper.getAsFloat(pJson, "probability");
         return new ApplyBonusCount.BinomialWithBonusCount(i, f);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   interface Formula {
      int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel);

      void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext);

      ResourceLocation getType();
   }

   interface FormulaDeserializer {
      ApplyBonusCount.Formula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext);
   }

   /**
    * Applies a bonus count with a special formula used for fortune ore drops.
    */
   static final class OreDrops implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("ore_drops");

      public int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel) {
         if (pEnchantmentLevel > 0) {
            int i = pRandom.nextInt(pEnchantmentLevel + 2) - 1;
            if (i < 0) {
               i = 0;
            }

            return pOriginalCount * (i + 1);
         } else {
            return pOriginalCount;
         }
      }

      public void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext) {
      }

      public static ApplyBonusCount.Formula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext) {
         return new ApplyBonusCount.OreDrops();
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ApplyBonusCount> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, ApplyBonusCount pApplyBonusCount, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pApplyBonusCount, pSerializationContext);
         pJson.addProperty("enchantment", BuiltInRegistries.ENCHANTMENT.getKey(pApplyBonusCount.enchantment).toString());
         pJson.addProperty("formula", pApplyBonusCount.formula.getType().toString());
         JsonObject jsonobject = new JsonObject();
         pApplyBonusCount.formula.serializeParams(jsonobject, pSerializationContext);
         if (jsonobject.size() > 0) {
            pJson.add("parameters", jsonobject);
         }

      }

      public ApplyBonusCount deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
         ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(pObject, "enchantment"));
         Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonParseException("Invalid enchantment id: " + resourcelocation);
         });
         ResourceLocation resourcelocation1 = new ResourceLocation(GsonHelper.getAsString(pObject, "formula"));
         ApplyBonusCount.FormulaDeserializer applybonuscount$formuladeserializer = ApplyBonusCount.FORMULAS.get(resourcelocation1);
         if (applybonuscount$formuladeserializer == null) {
            throw new JsonParseException("Invalid formula id: " + resourcelocation1);
         } else {
            ApplyBonusCount.Formula applybonuscount$formula;
            if (pObject.has("parameters")) {
               applybonuscount$formula = applybonuscount$formuladeserializer.deserialize(GsonHelper.getAsJsonObject(pObject, "parameters"), pDeserializationContext);
            } else {
               applybonuscount$formula = applybonuscount$formuladeserializer.deserialize(new JsonObject(), pDeserializationContext);
            }

            return new ApplyBonusCount(pConditions, enchantment, applybonuscount$formula);
         }
      }
   }

   /**
    * Adds a bonus count based on the enchantment level scaled by a constant multiplier.
    */
   static final class UniformBonusCount implements ApplyBonusCount.Formula {
      public static final ResourceLocation TYPE = new ResourceLocation("uniform_bonus_count");
      private final int bonusMultiplier;

      public UniformBonusCount(int pBonusMultiplier) {
         this.bonusMultiplier = pBonusMultiplier;
      }

      public int calculateNewCount(RandomSource pRandom, int pOriginalCount, int pEnchantmentLevel) {
         return pOriginalCount + pRandom.nextInt(this.bonusMultiplier * pEnchantmentLevel + 1);
      }

      public void serializeParams(JsonObject pJson, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("bonusMultiplier", this.bonusMultiplier);
      }

      public static ApplyBonusCount.Formula deserialize(JsonObject pJson, JsonDeserializationContext pDeserializationContext) {
         int i = GsonHelper.getAsInt(pJson, "bonusMultiplier");
         return new ApplyBonusCount.UniformBonusCount(i);
      }

      public ResourceLocation getType() {
         return TYPE;
      }
   }
}