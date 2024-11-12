package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool {
   final LootPoolEntryContainer[] entries;
   final LootItemCondition[] conditions;
   private final Predicate<LootContext> compositeCondition;
   final LootItemFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
   NumberProvider rolls;
   NumberProvider bonusRolls;

   LootPool(LootPoolEntryContainer[] pEntries, LootItemCondition[] pConditions, LootItemFunction[] pFunctions, NumberProvider pRolls, NumberProvider pBonusRolls, @org.jetbrains.annotations.Nullable String name) {
      this.name = name;
      this.entries = pEntries;
      this.conditions = pConditions;
      this.compositeCondition = LootItemConditions.andConditions(pConditions);
      this.functions = pFunctions;
      this.compositeFunction = LootItemFunctions.compose(pFunctions);
      this.rolls = pRolls;
      this.bonusRolls = pBonusRolls;
   }

   private void addRandomItem(Consumer<ItemStack> pStackConsumer, LootContext pContext) {
      RandomSource randomsource = pContext.getRandom();
      List<LootPoolEntry> list = Lists.newArrayList();
      MutableInt mutableint = new MutableInt();

      for(LootPoolEntryContainer lootpoolentrycontainer : this.entries) {
         lootpoolentrycontainer.expand(pContext, (p_79048_) -> {
            int k = p_79048_.getWeight(pContext.getLuck());
            if (k > 0) {
               list.add(p_79048_);
               mutableint.add(k);
            }

         });
      }

      int i = list.size();
      if (mutableint.intValue() != 0 && i != 0) {
         if (i == 1) {
            list.get(0).createItemStack(pStackConsumer, pContext);
         } else {
            int j = randomsource.nextInt(mutableint.intValue());

            for(LootPoolEntry lootpoolentry : list) {
               j -= lootpoolentry.getWeight(pContext.getLuck());
               if (j < 0) {
                  lootpoolentry.createItemStack(pStackConsumer, pContext);
                  return;
               }
            }

         }
      }
   }

   /**
    * Generate the random items from this LootPool to the given {@code stackConsumer}.
    * This first checks this pool's conditions, generating nothing if they do not match.
    * Then the random items are generated based on the {@link LootPoolEntry LootPoolEntries} in this pool according to
    * the rolls and bonusRolls, applying any loot functions.
    */
   public void addRandomItems(Consumer<ItemStack> pStackConsumer, LootContext pLootContext) {
      if (this.compositeCondition.test(pLootContext)) {
         Consumer<ItemStack> consumer = LootItemFunction.decorate(this.compositeFunction, pStackConsumer, pLootContext);
         int i = this.rolls.getInt(pLootContext) + Mth.floor(this.bonusRolls.getFloat(pLootContext) * pLootContext.getLuck());

         for(int j = 0; j < i; ++j) {
            this.addRandomItem(consumer, pLootContext);
         }

      }
   }

   /**
    * Validate this LootPool according to the given context.
    */
   public void validate(ValidationContext pContext) {
      for(int i = 0; i < this.conditions.length; ++i) {
         this.conditions[i].validate(pContext.forChild(".condition[" + i + "]"));
      }

      for(int j = 0; j < this.functions.length; ++j) {
         this.functions[j].validate(pContext.forChild(".functions[" + j + "]"));
      }

      for(int k = 0; k < this.entries.length; ++k) {
         this.entries[k].validate(pContext.forChild(".entries[" + k + "]"));
      }

      this.rolls.validate(pContext.forChild(".rolls"));
      this.bonusRolls.validate(pContext.forChild(".bonusRolls"));
   }
   //======================== FORGE START =============================================
   private boolean isFrozen = false;
   public void freeze() { this.isFrozen = true; }
   public boolean isFrozen(){ return this.isFrozen; }
   private void checkFrozen() {
      if (this.isFrozen())
         throw new RuntimeException("Attempted to modify LootPool after being frozen!");
   }
   @org.jetbrains.annotations.Nullable
   private final String name;
   @org.jetbrains.annotations.Nullable
   public String getName() { return this.name; }
   public NumberProvider getRolls()      { return this.rolls; }
   public NumberProvider getBonusRolls() { return this.bonusRolls; }
   public void setRolls     (NumberProvider v){ checkFrozen(); this.rolls = v; }
   public void setBonusRolls(NumberProvider v){ checkFrozen(); this.bonusRolls = v; }
   //======================== FORGE END ===============================================

   public static LootPool.Builder lootPool() {
      return new LootPool.Builder();
   }

   public static class Builder implements FunctionUserBuilder<LootPool.Builder>, ConditionUserBuilder<LootPool.Builder> {
      private final List<LootPoolEntryContainer> entries = Lists.newArrayList();
      private final List<LootItemCondition> conditions = Lists.newArrayList();
      private final List<LootItemFunction> functions = Lists.newArrayList();
      private NumberProvider rolls = ConstantValue.exactly(1.0F);
      private NumberProvider bonusRolls = ConstantValue.exactly(0.0F);
      private String name;

      public LootPool.Builder setRolls(NumberProvider pRolls) {
         this.rolls = pRolls;
         return this;
      }

      public LootPool.Builder unwrap() {
         return this;
      }

      public LootPool.Builder setBonusRolls(NumberProvider pBonusRolls) {
         this.bonusRolls = pBonusRolls;
         return this;
      }

      public LootPool.Builder add(LootPoolEntryContainer.Builder<?> pEntriesBuilder) {
         this.entries.add(pEntriesBuilder.build());
         return this;
      }

      public LootPool.Builder when(LootItemCondition.Builder pConditionBuilder) {
         this.conditions.add(pConditionBuilder.build());
         return this;
      }

      public LootPool.Builder apply(LootItemFunction.Builder pFunctionBuilder) {
         this.functions.add(pFunctionBuilder.build());
         return this;
      }

      public LootPool.Builder name(String name) {
         this.name = name;
         return this;
      }

      public LootPool build() {
         if (this.rolls == null) {
            throw new IllegalArgumentException("Rolls not set");
         } else {
            return new LootPool(this.entries.toArray(new LootPoolEntryContainer[0]), this.conditions.toArray(new LootItemCondition[0]), this.functions.toArray(new LootItemFunction[0]), this.rolls, this.bonusRolls, name);
         }
      }
   }

   public static class Serializer implements JsonDeserializer<LootPool>, JsonSerializer<LootPool> {
      public LootPool deserialize(JsonElement pJson, Type pTypeOfT, JsonDeserializationContext pContext) throws JsonParseException {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "loot pool");
         LootPoolEntryContainer[] alootpoolentrycontainer = GsonHelper.getAsObject(jsonobject, "entries", pContext, LootPoolEntryContainer[].class);
         LootItemCondition[] alootitemcondition = GsonHelper.getAsObject(jsonobject, "conditions", new LootItemCondition[0], pContext, LootItemCondition[].class);
         LootItemFunction[] alootitemfunction = GsonHelper.getAsObject(jsonobject, "functions", new LootItemFunction[0], pContext, LootItemFunction[].class);
         NumberProvider numberprovider = GsonHelper.getAsObject(jsonobject, "rolls", pContext, NumberProvider.class);
         NumberProvider numberprovider1 = GsonHelper.getAsObject(jsonobject, "bonus_rolls", ConstantValue.exactly(0.0F), pContext, NumberProvider.class);
         return new LootPool(alootpoolentrycontainer, alootitemcondition, alootitemfunction, numberprovider, numberprovider1, net.minecraftforge.common.ForgeHooks.readPoolName(jsonobject));
      }

      public JsonElement serialize(LootPool pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         if (pSrc.name != null && !pSrc.name.startsWith("custom#"))
            jsonobject.add("name", pContext.serialize(pSrc.name));
         jsonobject.add("rolls", pContext.serialize(pSrc.rolls));
         jsonobject.add("bonus_rolls", pContext.serialize(pSrc.bonusRolls));
         jsonobject.add("entries", pContext.serialize(pSrc.entries));
         if (!ArrayUtils.isEmpty((Object[])pSrc.conditions)) {
            jsonobject.add("conditions", pContext.serialize(pSrc.conditions));
         }

         if (!ArrayUtils.isEmpty((Object[])pSrc.functions)) {
            jsonobject.add("functions", pContext.serialize(pSrc.functions));
         }

         return jsonobject;
      }
   }
}
