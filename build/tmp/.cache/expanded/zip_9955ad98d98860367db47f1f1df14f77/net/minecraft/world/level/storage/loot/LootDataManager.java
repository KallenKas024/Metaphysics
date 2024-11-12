package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.slf4j.Logger;

public class LootDataManager implements PreparableReloadListener, LootDataResolver {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataId<LootTable> EMPTY_LOOT_TABLE_KEY = new LootDataId<>(LootDataType.TABLE, BuiltInLootTables.EMPTY);
   private Map<LootDataId<?>, ?> elements = Map.of();
   private Multimap<LootDataType<?>, ResourceLocation> typeKeys = ImmutableMultimap.of();

   public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pPreparationBarrier, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      Map<LootDataType<?>, Map<ResourceLocation, ?>> map = new HashMap<>();
      CompletableFuture<?>[] completablefuture = LootDataType.values().map((p_279242_) -> {
         return scheduleElementParse(p_279242_, pResourceManager, pBackgroundExecutor, map);
      }).toArray((p_279126_) -> {
         return new CompletableFuture[p_279126_];
      });
      return CompletableFuture.allOf(completablefuture).thenCompose(pPreparationBarrier::wait).thenAcceptAsync((p_279096_) -> {
         this.apply(map);
      }, pGameExecutor);
   }

   private static <T> CompletableFuture<?> scheduleElementParse(LootDataType<T> pLootDataType, ResourceManager pResourceManager, Executor pBackgroundExecutor, Map<LootDataType<?>, Map<ResourceLocation, ?>> pElementCollector) {
      Map<ResourceLocation, T> map = new HashMap<>();
      pElementCollector.put(pLootDataType, map);
      return CompletableFuture.runAsync(() -> {
         Map<ResourceLocation, JsonElement> map1 = new HashMap<>();
         SimpleJsonResourceReloadListener.scanDirectory(pResourceManager, pLootDataType.directory(), pLootDataType.parser(), map1);
         map1.forEach((p_279416_, p_279151_) -> {
            pLootDataType.deserialize(p_279416_, p_279151_, pResourceManager).ifPresent((p_279295_) -> {
               map.put(p_279416_, p_279295_);
            });
         });
      }, pBackgroundExecutor);
   }

   private void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> pCollectedElements) {
      Object object = pCollectedElements.get(LootDataType.TABLE).remove(BuiltInLootTables.EMPTY);
      if (object != null) {
         LOGGER.warn("Datapack tried to redefine {} loot table, ignoring", (Object)BuiltInLootTables.EMPTY);
      }

      ImmutableMap.Builder<LootDataId<?>, Object> builder = ImmutableMap.builder();
      ImmutableMultimap.Builder<LootDataType<?>, ResourceLocation> builder1 = ImmutableMultimap.builder();
      pCollectedElements.forEach((p_279449_, p_279262_) -> {
         p_279262_.forEach((p_279130_, p_279313_) -> {
            builder.put(new LootDataId(p_279449_, p_279130_), p_279313_);
            builder1.put(p_279449_, p_279130_);
         });
      });
      builder.put(EMPTY_LOOT_TABLE_KEY, LootTable.EMPTY);
      final Map<LootDataId<?>, ?> map = builder.build();
      ValidationContext validationcontext = new ValidationContext(LootContextParamSets.ALL_PARAMS, new LootDataResolver() {
         @Nullable
         public <T> T getElement(LootDataId<T> p_279194_) {
            return (T)map.get(p_279194_);
         }
      });
      map.forEach((p_279387_, p_279087_) -> {
         castAndValidate(validationcontext, p_279387_, p_279087_);
      });
      validationcontext.getProblems().forEach((p_279487_, p_279312_) -> {
         LOGGER.warn("Found loot table element validation problem in {}: {}", p_279487_, p_279312_);
      });
      this.elements = map;
      this.typeKeys = builder1.build();
   }

   private static <T> void castAndValidate(ValidationContext pContext, LootDataId<T> pId, Object pElement) {
      pId.type().runValidation(pContext, pId, (T)pElement);
   }

   @Nullable
   public <T> T getElement(LootDataId<T> pId) {
      return (T)this.elements.get(pId);
   }

   public Collection<ResourceLocation> getKeys(LootDataType<?> pType) {
      return this.typeKeys.get(pType);
   }

   public static LootItemCondition createComposite(LootItemCondition[] pTerms) {
      return new LootDataManager.CompositePredicate(pTerms);
   }

   public static LootItemFunction createComposite(LootItemFunction[] pFunctions) {
      return new LootDataManager.FunctionSequence(pFunctions);
   }

   static class CompositePredicate implements LootItemCondition {
      private final LootItemCondition[] terms;
      private final Predicate<LootContext> composedPredicate;

      CompositePredicate(LootItemCondition[] pTerms) {
         this.terms = pTerms;
         this.composedPredicate = LootItemConditions.andConditions(pTerms);
      }

      public final boolean test(LootContext pContext) {
         return this.composedPredicate.test(pContext);
      }

      /**
       * Validate that this object is used correctly according to the given ValidationContext.
       */
      public void validate(ValidationContext pContext) {
         LootItemCondition.super.validate(pContext);

         for(int i = 0; i < this.terms.length; ++i) {
            this.terms[i].validate(pContext.forChild(".term[" + i + "]"));
         }

      }

      public LootItemConditionType getType() {
         throw new UnsupportedOperationException();
      }
   }

   static class FunctionSequence implements LootItemFunction {
      protected final LootItemFunction[] functions;
      private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;

      public FunctionSequence(LootItemFunction[] pFunctions) {
         this.functions = pFunctions;
         this.compositeFunction = LootItemFunctions.compose(pFunctions);
      }

      public ItemStack apply(ItemStack pStack, LootContext pContext) {
         return this.compositeFunction.apply(pStack, pContext);
      }

      /**
       * Validate that this object is used correctly according to the given ValidationContext.
       */
      public void validate(ValidationContext pContext) {
         LootItemFunction.super.validate(pContext);

         for(int i = 0; i < this.functions.length; ++i) {
            this.functions[i].validate(pContext.forChild(".function[" + i + "]"));
         }

      }

      public LootItemFunctionType getType() {
         throw new UnsupportedOperationException();
      }
   }
}
