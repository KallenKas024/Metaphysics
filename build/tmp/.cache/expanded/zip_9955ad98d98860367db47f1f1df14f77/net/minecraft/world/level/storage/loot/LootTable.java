package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class LootTable {
   static final Logger LOGGER = LogUtils.getLogger();
   public static final LootTable EMPTY = new LootTable(LootContextParamSets.EMPTY, (ResourceLocation)null, new LootPool[0], new LootItemFunction[0]);
   public static final LootContextParamSet DEFAULT_PARAM_SET = LootContextParamSets.ALL_PARAMS;
   final LootContextParamSet paramSet;
   @Nullable
   final ResourceLocation randomSequence;
   private final List<LootPool> pools;
   final LootItemFunction[] functions;
   private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

   LootTable(LootContextParamSet pParamSet, @Nullable ResourceLocation pRandomSequence, LootPool[] pPools, LootItemFunction[] pFunctions) {
      this.paramSet = pParamSet;
      this.randomSequence = pRandomSequence;
      this.pools = Lists.newArrayList(pPools);
      this.functions = pFunctions;
      this.compositeFunction = LootItemFunctions.compose(pFunctions);
   }

   public static Consumer<ItemStack> createStackSplitter(ServerLevel pLevel, Consumer<ItemStack> pOutput) {
      return (p_287570_) -> {
         if (p_287570_.isItemEnabled(pLevel.enabledFeatures())) {
            if (p_287570_.getCount() < p_287570_.getMaxStackSize()) {
               pOutput.accept(p_287570_);
            } else {
               int i = p_287570_.getCount();

               while(i > 0) {
                  ItemStack itemstack = p_287570_.copyWithCount(Math.min(p_287570_.getMaxStackSize(), i));
                  i -= itemstack.getCount();
                  pOutput.accept(itemstack);
               }
            }

         }
      };
   }

   @Deprecated // Use a non-'Raw' version of 'getRandomItems', so that the Forge Global Loot Modifiers will be applied
   public void getRandomItemsRaw(LootParams pParams, Consumer<ItemStack> pOutput) {
      this.getRandomItemsRaw((new LootContext.Builder(pParams)).create(this.randomSequence), pOutput);
   }

   /**
    * Generate items to the given Consumer, ignoring maximum stack size.
    */
   @Deprecated // Use a non-'Raw' version of 'getRandomItems', so that the Forge Global Loot Modifiers will be applied
   public void getRandomItemsRaw(LootContext pContext, Consumer<ItemStack> pOutput) {
      LootContext.VisitedEntry<?> visitedentry = LootContext.createVisitedEntry(this);
      if (pContext.pushVisitedElement(visitedentry)) {
         Consumer<ItemStack> consumer = LootItemFunction.decorate(this.compositeFunction, pOutput, pContext);

         for(LootPool lootpool : this.pools) {
            lootpool.addRandomItems(consumer, pContext);
         }

         pContext.popVisitedElement(visitedentry);
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
      }

   }

   public void getRandomItems(LootParams pParams, long pSeed, Consumer<ItemStack> pOutput) {
      this.getRandomItems((new LootContext.Builder(pParams)).withOptionalRandomSeed(pSeed).create(this.randomSequence)).forEach(pOutput);
   }

   public void getRandomItems(LootParams pParams, Consumer<ItemStack> pOutput) {
      this.getRandomItems(pParams).forEach(pOutput);
   }

   /**
    * Generate random items to the given Consumer, ensuring they do not exceed their maximum stack size.
    */
   public void getRandomItems(LootContext pContextData, Consumer<ItemStack> pOutput) {
      this.getRandomItems(pContextData).forEach(pOutput);
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams pParams, long pSeed) {
      return this.getRandomItems((new LootContext.Builder(pParams)).withOptionalRandomSeed(pSeed).create(this.randomSequence));
   }

   public ObjectArrayList<ItemStack> getRandomItems(LootParams pParams) {
      return this.getRandomItems((new LootContext.Builder(pParams)).create(this.randomSequence));
   }

   /**
    * Generate random items to a List.
    */
   private ObjectArrayList<ItemStack> getRandomItems(LootContext pContext) {
      ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
      this.getRandomItemsRaw(pContext, createStackSplitter(pContext.getLevel(), objectarraylist::add));
      objectarraylist = net.minecraftforge.common.ForgeHooks.modifyLoot(this.getLootTableId(), objectarraylist, pContext);
      return objectarraylist;
   }

   /**
    * Get the parameter set for this LootTable.
    */
   public LootContextParamSet getParamSet() {
      return this.paramSet;
   }

   /**
    * Validate this LootTable using the given ValidationContext.
    */
   public void validate(ValidationContext pValidator) {
      for(int i = 0; i < this.pools.size(); ++i) {
         this.pools.get(i).validate(pValidator.forChild(".pools[" + i + "]"));
      }

      for(int j = 0; j < this.functions.length; ++j) {
         this.functions[j].validate(pValidator.forChild(".functions[" + j + "]"));
      }

   }

   public void fill(Container pContainer, LootParams pParams, long pSeed) {
      LootContext lootcontext = (new LootContext.Builder(pParams)).withOptionalRandomSeed(pSeed).create(this.randomSequence);
      ObjectArrayList<ItemStack> objectarraylist = this.getRandomItems(lootcontext);
      RandomSource randomsource = lootcontext.getRandom();
      List<Integer> list = this.getAvailableSlots(pContainer, randomsource);
      this.shuffleAndSplitItems(objectarraylist, list.size(), randomsource);

      for(ItemStack itemstack : objectarraylist) {
         if (list.isEmpty()) {
            LOGGER.warn("Tried to over-fill a container");
            return;
         }

         if (itemstack.isEmpty()) {
            pContainer.setItem(list.remove(list.size() - 1), ItemStack.EMPTY);
         } else {
            pContainer.setItem(list.remove(list.size() - 1), itemstack);
         }
      }

   }

   /**
    * Shuffles items by changing their order and splitting stacks
    */
   private void shuffleAndSplitItems(ObjectArrayList<ItemStack> pStacks, int pEmptySlotsCount, RandomSource pRandom) {
      List<ItemStack> list = Lists.newArrayList();
      Iterator<ItemStack> iterator = pStacks.iterator();

      while(iterator.hasNext()) {
         ItemStack itemstack = iterator.next();
         if (itemstack.isEmpty()) {
            iterator.remove();
         } else if (itemstack.getCount() > 1) {
            list.add(itemstack);
            iterator.remove();
         }
      }

      while(pEmptySlotsCount - pStacks.size() - list.size() > 0 && !list.isEmpty()) {
         ItemStack itemstack2 = list.remove(Mth.nextInt(pRandom, 0, list.size() - 1));
         int i = Mth.nextInt(pRandom, 1, itemstack2.getCount() / 2);
         ItemStack itemstack1 = itemstack2.split(i);
         if (itemstack2.getCount() > 1 && pRandom.nextBoolean()) {
            list.add(itemstack2);
         } else {
            pStacks.add(itemstack2);
         }

         if (itemstack1.getCount() > 1 && pRandom.nextBoolean()) {
            list.add(itemstack1);
         } else {
            pStacks.add(itemstack1);
         }
      }

      pStacks.addAll(list);
      Util.shuffle(pStacks, pRandom);
   }

   private List<Integer> getAvailableSlots(Container pInventory, RandomSource pRandom) {
      ObjectArrayList<Integer> objectarraylist = new ObjectArrayList<>();

      for(int i = 0; i < pInventory.getContainerSize(); ++i) {
         if (pInventory.getItem(i).isEmpty()) {
            objectarraylist.add(i);
         }
      }

      Util.shuffle(objectarraylist, pRandom);
      return objectarraylist;
   }

   public static LootTable.Builder lootTable() {
      return new LootTable.Builder();
   }

   //======================== FORGE START =============================================
   private boolean isFrozen = false;
   public void freeze() {
      this.isFrozen = true;
      this.pools.forEach(LootPool::freeze);
   }
   public boolean isFrozen(){ return this.isFrozen; }
   private void checkFrozen() {
      if (this.isFrozen())
         throw new RuntimeException("Attempted to modify LootTable after being finalized!");
   }

   private ResourceLocation lootTableId;
   public void setLootTableId(final ResourceLocation id) {
      if (this.lootTableId != null) throw new IllegalStateException("Attempted to rename loot table from '" + this.lootTableId + "' to '" + id + "': this is not supported");
      this.lootTableId = java.util.Objects.requireNonNull(id);
   }
   public ResourceLocation getLootTableId() { return this.lootTableId; }

   @org.jetbrains.annotations.Nullable
   public LootPool getPool(String name) {
      return pools.stream().filter(e -> name.equals(e.getName())).findFirst().orElse(null);
   }

   @org.jetbrains.annotations.Nullable
   public LootPool removePool(String name) {
      checkFrozen();
      for (LootPool pool : this.pools) {
         if (name.equals(pool.getName())) {
            this.pools.remove(pool);
            return pool;
         }
      }
      return null;
   }

   public void addPool(LootPool pool) {
      checkFrozen();
      if (pools.stream().anyMatch(e -> e == pool || e.getName() != null && e.getName().equals(pool.getName())))
         throw new RuntimeException("Attempted to add a duplicate pool to loot table: " + pool.getName());
      this.pools.add(pool);
   }
   //======================== FORGE END ===============================================

   public static class Builder implements FunctionUserBuilder<LootTable.Builder> {
      private final List<LootPool> pools = Lists.newArrayList();
      private final List<LootItemFunction> functions = Lists.newArrayList();
      private LootContextParamSet paramSet = LootTable.DEFAULT_PARAM_SET;
      @Nullable
      private ResourceLocation randomSequence = null;

      public LootTable.Builder withPool(LootPool.Builder pLootPool) {
         this.pools.add(pLootPool.build());
         return this;
      }

      public LootTable.Builder setParamSet(LootContextParamSet pParameterSet) {
         this.paramSet = pParameterSet;
         return this;
      }

      public LootTable.Builder setRandomSequence(ResourceLocation pRandomSequence) {
         this.randomSequence = pRandomSequence;
         return this;
      }

      public LootTable.Builder apply(LootItemFunction.Builder pFunctionBuilder) {
         this.functions.add(pFunctionBuilder.build());
         return this;
      }

      public LootTable.Builder unwrap() {
         return this;
      }

      public LootTable build() {
         return new LootTable(this.paramSet, this.randomSequence, this.pools.toArray(new LootPool[0]), this.functions.toArray(new LootItemFunction[0]));
      }
   }

   public static class Serializer implements JsonDeserializer<LootTable>, JsonSerializer<LootTable> {
      public LootTable deserialize(JsonElement pJson, Type pTypeOfT, JsonDeserializationContext pContext) throws JsonParseException {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "loot table");
         LootPool[] alootpool = GsonHelper.getAsObject(jsonobject, "pools", new LootPool[0], pContext, LootPool[].class);
         LootContextParamSet lootcontextparamset = null;
         if (jsonobject.has("type")) {
            String s = GsonHelper.getAsString(jsonobject, "type");
            lootcontextparamset = LootContextParamSets.get(new ResourceLocation(s));
         }

         ResourceLocation resourcelocation;
         if (jsonobject.has("random_sequence")) {
            String s1 = GsonHelper.getAsString(jsonobject, "random_sequence");
            resourcelocation = new ResourceLocation(s1);
         } else {
            resourcelocation = null;
         }

         LootItemFunction[] alootitemfunction = GsonHelper.getAsObject(jsonobject, "functions", new LootItemFunction[0], pContext, LootItemFunction[].class);
         return new LootTable(lootcontextparamset != null ? lootcontextparamset : LootContextParamSets.ALL_PARAMS, resourcelocation, alootpool, alootitemfunction);
      }

      public JsonElement serialize(LootTable pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         if (pSrc.paramSet != LootTable.DEFAULT_PARAM_SET) {
            ResourceLocation resourcelocation = LootContextParamSets.getKey(pSrc.paramSet);
            if (resourcelocation != null) {
               jsonobject.addProperty("type", resourcelocation.toString());
            } else {
               LootTable.LOGGER.warn("Failed to find id for param set {}", (Object)pSrc.paramSet);
            }
         }

         if (pSrc.randomSequence != null) {
            jsonobject.addProperty("random_sequence", pSrc.randomSequence.toString());
         }

         if (!pSrc.pools.isEmpty()) {
            jsonobject.add("pools", pContext.serialize(pSrc.pools));
         }

         if (!ArrayUtils.isEmpty((Object[])pSrc.functions)) {
            jsonobject.add("functions", pContext.serialize(pSrc.functions));
         }

         return jsonobject;
      }
   }
}
