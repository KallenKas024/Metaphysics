package net.minecraft.commands.synchronization;

import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
   private static final Map<ResourceLocation, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = Maps.newHashMap();
   private static final ResourceLocation DEFAULT_NAME = new ResourceLocation("ask_server");
   public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = register(DEFAULT_NAME, (p_121673_, p_121674_) -> {
      return p_121673_.getSource().customSuggestion(p_121673_);
   });
   public static final SuggestionProvider<CommandSourceStack> ALL_RECIPES = register(new ResourceLocation("all_recipes"), (p_121670_, p_121671_) -> {
      return SharedSuggestionProvider.suggestResource(p_121670_.getSource().getRecipeNames(), p_121671_);
   });
   public static final SuggestionProvider<CommandSourceStack> AVAILABLE_SOUNDS = register(new ResourceLocation("available_sounds"), (p_121667_, p_121668_) -> {
      return SharedSuggestionProvider.suggestResource(p_121667_.getSource().getAvailableSounds(), p_121668_);
   });
   public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = register(new ResourceLocation("summonable_entities"), (p_258164_, p_258165_) -> {
      return SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter((p_247987_) -> {
         return p_247987_.isEnabled(p_258164_.getSource().enabledFeatures()) && p_247987_.canSummon();
      }), p_258165_, EntityType::getKey, (p_212436_) -> {
         return Component.translatable(Util.makeDescriptionId("entity", EntityType.getKey(p_212436_)));
      });
   });

   public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(ResourceLocation pName, SuggestionProvider<SharedSuggestionProvider> pProvider) {
      if (PROVIDERS_BY_NAME.containsKey(pName)) {
         throw new IllegalArgumentException("A command suggestion provider is already registered with the name " + pName);
      } else {
         PROVIDERS_BY_NAME.put(pName, pProvider);
         return (SuggestionProvider<S>)new SuggestionProviders.Wrapper(pName, pProvider);
      }
   }

   public static SuggestionProvider<SharedSuggestionProvider> getProvider(ResourceLocation pName) {
      return PROVIDERS_BY_NAME.getOrDefault(pName, ASK_SERVER);
   }

   /**
    * Gets the ID for the given provider. If the provider is not a wrapped one created via {@link #register}, then it
    * returns {@link #ASK_SERVER_ID} instead, as there is no known ID but ASK_SERVER always works.
    */
   public static ResourceLocation getName(SuggestionProvider<SharedSuggestionProvider> pProvider) {
      return pProvider instanceof SuggestionProviders.Wrapper ? ((SuggestionProviders.Wrapper)pProvider).name : DEFAULT_NAME;
   }

   /**
    * Checks to make sure that the given suggestion provider is a wrapped one that was created via {@link #register}. If
    * not, returns {@link #ASK_SERVER}. Needed because custom providers don't have a known ID to send to the client, but
    * ASK_SERVER always works.
    */
   public static SuggestionProvider<SharedSuggestionProvider> safelySwap(SuggestionProvider<SharedSuggestionProvider> pProvider) {
      return pProvider instanceof SuggestionProviders.Wrapper ? pProvider : ASK_SERVER;
   }

   protected static class Wrapper implements SuggestionProvider<SharedSuggestionProvider> {
      private final SuggestionProvider<SharedSuggestionProvider> delegate;
      final ResourceLocation name;

      public Wrapper(ResourceLocation pName, SuggestionProvider<SharedSuggestionProvider> pDelegate) {
         this.delegate = pDelegate;
         this.name = pName;
      }

      public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> pContext, SuggestionsBuilder pBuilder) throws CommandSyntaxException {
         return this.delegate.getSuggestions(pContext, pBuilder);
      }
   }
}