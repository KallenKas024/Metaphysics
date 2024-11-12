package net.minecraft.world.level.storage.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class LootDataType<T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final LootDataType<LootItemCondition> PREDICATE = new LootDataType<>(Deserializers.createConditionSerializer().create(), createSingleOrMultipleDeserialiser(LootItemCondition.class, LootDataManager::createComposite), "predicates", createSimpleValidator());
   public static final LootDataType<LootItemFunction> MODIFIER = new LootDataType<>(Deserializers.createFunctionSerializer().create(), createSingleOrMultipleDeserialiser(LootItemFunction.class, LootDataManager::createComposite), "item_modifiers", createSimpleValidator());
   public static final LootDataType<LootTable> TABLE = new LootDataType<>(Deserializers.createLootTableSerializer().create(), net.minecraftforge.common.ForgeHooks::getLootTableDeserializer, "loot_tables", createLootTableValidator());
   private final Gson parser;
   private final org.apache.commons.lang3.function.TriFunction<ResourceLocation, JsonElement, net.minecraft.server.packs.resources.ResourceManager, Optional<T>> topDeserializer;
   private final String directory;
   private final LootDataType.Validator<T> validator;

   private LootDataType(Gson pParser, BiFunction<Gson, String, org.apache.commons.lang3.function.TriFunction<ResourceLocation, JsonElement, net.minecraft.server.packs.resources.ResourceManager, Optional<T>>> pTopDeserializerGetter, String pDirectory, LootDataType.Validator<T> pValidator) {
      this.parser = pParser;
      this.directory = pDirectory;
      this.validator = pValidator;
      this.topDeserializer = pTopDeserializerGetter.apply(pParser, pDirectory);
   }

   public Gson parser() {
      return this.parser;
   }

   public String directory() {
      return this.directory;
   }

   public void runValidation(ValidationContext pContext, LootDataId<T> pId, T pElement) {
      this.validator.run(pContext, pId, pElement);
   }

   public Optional<T> deserialize(ResourceLocation pLocation, JsonElement pJson, net.minecraft.server.packs.resources.ResourceManager resourceManager) {
      return this.topDeserializer.apply(pLocation, pJson, resourceManager);
   }

   public static Stream<LootDataType<?>> values() {
      return Stream.of(PREDICATE, MODIFIER, TABLE);
   }

   private static <T> BiFunction<Gson, String, org.apache.commons.lang3.function.TriFunction<ResourceLocation, JsonElement, net.minecraft.server.packs.resources.ResourceManager, Optional<T>>> createSingleDeserialiser(Class<T> pValueType) {
      return (p_279398_, p_279358_) -> {
         return (p_279297_, p_279222_, resourceManager) -> {
            try {
               return Optional.of(p_279398_.fromJson(p_279222_, pValueType));
            } catch (Exception exception) {
               LOGGER.error("Couldn't parse element {}:{}", p_279358_, p_279297_, exception);
               return Optional.empty();
            }
         };
      };
   }

   private static <T> BiFunction<Gson, String, org.apache.commons.lang3.function.TriFunction<ResourceLocation, JsonElement, net.minecraft.server.packs.resources.ResourceManager, Optional<T>>> createSingleOrMultipleDeserialiser(Class<T> pValueType, Function<T[], T> pCombiner) {
      Class<T[]> oclass = (Class<T[]>)pValueType.arrayType();
      return (p_279462_, p_279351_) -> {
         return (p_279495_, p_279409_, resourceManager) -> {
            try {
               if (p_279409_.isJsonArray()) {
                  T[] at = (T[])((Object[])p_279462_.fromJson(p_279409_, oclass));
                  return Optional.of(pCombiner.apply(at));
               } else {
                  return Optional.of(p_279462_.fromJson(p_279409_, pValueType));
               }
            } catch (Exception exception) {
               LOGGER.error("Couldn't parse element {}:{}", p_279351_, p_279495_, exception);
               return Optional.empty();
            }
         };
      };
   }

   private static <T extends LootContextUser> LootDataType.Validator<T> createSimpleValidator() {
      return (p_279353_, p_279374_, p_279097_) -> {
         p_279097_.validate(p_279353_.enterElement("{" + p_279374_.type().directory + ":" + p_279374_.location() + "}", p_279374_));
      };
   }

   private static LootDataType.Validator<LootTable> createLootTableValidator() {
      return (p_279333_, p_279227_, p_279406_) -> {
         p_279406_.validate(p_279333_.setParams(p_279406_.getParamSet()).enterElement("{" + p_279227_.type().directory + ":" + p_279227_.location() + "}", p_279227_));
      };
   }

   @FunctionalInterface
   public interface Validator<T> {
      void run(ValidationContext pContext, LootDataId<T> pId, T pElement);
   }
}
