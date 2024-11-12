package net.minecraft.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;

public interface SharedSuggestionProvider {
   Collection<String> getOnlinePlayerNames();

   default Collection<String> getCustomTabSugggestions() {
      return this.getOnlinePlayerNames();
   }

   default Collection<String> getSelectedEntities() {
      return Collections.emptyList();
   }

   Collection<String> getAllTeams();

   Stream<ResourceLocation> getAvailableSounds();

   Stream<ResourceLocation> getRecipeNames();

   CompletableFuture<Suggestions> customSuggestion(CommandContext<?> pContext);

   default Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
      return Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_GLOBAL);
   }

   default Collection<SharedSuggestionProvider.TextCoordinates> getAbsoluteCoordinates() {
      return Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_GLOBAL);
   }

   Set<ResourceKey<Level>> levels();

   RegistryAccess registryAccess();

   FeatureFlagSet enabledFeatures();

   default void suggestRegistryElements(Registry<?> pRegistry, SharedSuggestionProvider.ElementSuggestionType pType, SuggestionsBuilder pBuilder) {
      if (pType.shouldSuggestTags()) {
         suggestResource(pRegistry.getTagNames().map(TagKey::location), pBuilder, "#");
      }

      if (pType.shouldSuggestElements()) {
         suggestResource(pRegistry.keySet(), pBuilder);
      }

   }

   CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> pResourceKey, SharedSuggestionProvider.ElementSuggestionType pRegistryKey, SuggestionsBuilder pBuilder, CommandContext<?> pContext);

   boolean hasPermission(int pPermissionLevel);

   static <T> void filterResources(Iterable<T> pResources, String pInput, Function<T, ResourceLocation> pLocationFunction, Consumer<T> pResourceConsumer) {
      boolean flag = pInput.indexOf(58) > -1;

      for(T t : pResources) {
         ResourceLocation resourcelocation = pLocationFunction.apply(t);
         if (flag) {
            String s = resourcelocation.toString();
            if (matchesSubStr(pInput, s)) {
               pResourceConsumer.accept(t);
            }
         } else if (matchesSubStr(pInput, resourcelocation.getNamespace()) || resourcelocation.getNamespace().equals("minecraft") && matchesSubStr(pInput, resourcelocation.getPath())) {
            pResourceConsumer.accept(t);
         }
      }

   }

   static <T> void filterResources(Iterable<T> pResources, String pRemaining, String pPrefix, Function<T, ResourceLocation> pLocationFunction, Consumer<T> pResourceConsumer) {
      if (pRemaining.isEmpty()) {
         pResources.forEach(pResourceConsumer);
      } else {
         String s = Strings.commonPrefix(pRemaining, pPrefix);
         if (!s.isEmpty()) {
            String s1 = pRemaining.substring(s.length());
            filterResources(pResources, s1, pLocationFunction, pResourceConsumer);
         }
      }

   }

   static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> pResources, SuggestionsBuilder pBuilder, String pPrefix) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(pResources, s, pPrefix, (p_82985_) -> {
         return p_82985_;
      }, (p_82917_) -> {
         pBuilder.suggest(pPrefix + p_82917_);
      });
      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> pResources, SuggestionsBuilder pBuilder, String pPrefix) {
      return suggestResource(pResources::iterator, pBuilder, pPrefix);
   }

   static CompletableFuture<Suggestions> suggestResource(Iterable<ResourceLocation> pResources, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(pResources, s, (p_82966_) -> {
         return p_82966_;
      }, (p_82925_) -> {
         pBuilder.suggest(p_82925_.toString());
      });
      return pBuilder.buildFuture();
   }

   static <T> CompletableFuture<Suggestions> suggestResource(Iterable<T> pResources, SuggestionsBuilder pBuilder, Function<T, ResourceLocation> pLocationFunction, Function<T, Message> pSuggestionFunction) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      filterResources(pResources, s, pLocationFunction, (p_82922_) -> {
         pBuilder.suggest(pLocationFunction.apply(p_82922_).toString(), pSuggestionFunction.apply(p_82922_));
      });
      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggestResource(Stream<ResourceLocation> pResourceLocations, SuggestionsBuilder pBuilder) {
      return suggestResource(pResourceLocations::iterator, pBuilder);
   }

   static <T> CompletableFuture<Suggestions> suggestResource(Stream<T> pResources, SuggestionsBuilder pBuilder, Function<T, ResourceLocation> pLocationFunction, Function<T, Message> pSuggestionFunction) {
      return suggestResource(pResources::iterator, pBuilder, pLocationFunction, pSuggestionFunction);
   }

   static CompletableFuture<Suggestions> suggestCoordinates(String pRemaining, Collection<SharedSuggestionProvider.TextCoordinates> pCoordinates, SuggestionsBuilder pBuilder, Predicate<String> pValidator) {
      List<String> list = Lists.newArrayList();
      if (Strings.isNullOrEmpty(pRemaining)) {
         for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider$textcoordinates : pCoordinates) {
            String s = sharedsuggestionprovider$textcoordinates.x + " " + sharedsuggestionprovider$textcoordinates.y + " " + sharedsuggestionprovider$textcoordinates.z;
            if (pValidator.test(s)) {
               list.add(sharedsuggestionprovider$textcoordinates.x);
               list.add(sharedsuggestionprovider$textcoordinates.x + " " + sharedsuggestionprovider$textcoordinates.y);
               list.add(s);
            }
         }
      } else {
         String[] astring = pRemaining.split(" ");
         if (astring.length == 1) {
            for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider$textcoordinates1 : pCoordinates) {
               String s1 = astring[0] + " " + sharedsuggestionprovider$textcoordinates1.y + " " + sharedsuggestionprovider$textcoordinates1.z;
               if (pValidator.test(s1)) {
                  list.add(astring[0] + " " + sharedsuggestionprovider$textcoordinates1.y);
                  list.add(s1);
               }
            }
         } else if (astring.length == 2) {
            for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider$textcoordinates2 : pCoordinates) {
               String s2 = astring[0] + " " + astring[1] + " " + sharedsuggestionprovider$textcoordinates2.z;
               if (pValidator.test(s2)) {
                  list.add(s2);
               }
            }
         }
      }

      return suggest(list, pBuilder);
   }

   static CompletableFuture<Suggestions> suggest2DCoordinates(String pRemaining, Collection<SharedSuggestionProvider.TextCoordinates> pCoordinates, SuggestionsBuilder pBuilder, Predicate<String> pValidator) {
      List<String> list = Lists.newArrayList();
      if (Strings.isNullOrEmpty(pRemaining)) {
         for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider$textcoordinates : pCoordinates) {
            String s = sharedsuggestionprovider$textcoordinates.x + " " + sharedsuggestionprovider$textcoordinates.z;
            if (pValidator.test(s)) {
               list.add(sharedsuggestionprovider$textcoordinates.x);
               list.add(s);
            }
         }
      } else {
         String[] astring = pRemaining.split(" ");
         if (astring.length == 1) {
            for(SharedSuggestionProvider.TextCoordinates sharedsuggestionprovider$textcoordinates1 : pCoordinates) {
               String s1 = astring[0] + " " + sharedsuggestionprovider$textcoordinates1.z;
               if (pValidator.test(s1)) {
                  list.add(s1);
               }
            }
         }
      }

      return suggest(list, pBuilder);
   }

   static CompletableFuture<Suggestions> suggest(Iterable<String> pStrings, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(String s1 : pStrings) {
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            pBuilder.suggest(s1);
         }
      }

      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggest(Stream<String> pStrings, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);
      pStrings.filter((p_82975_) -> {
         return matchesSubStr(s, p_82975_.toLowerCase(Locale.ROOT));
      }).forEach(pBuilder::suggest);
      return pBuilder.buildFuture();
   }

   static CompletableFuture<Suggestions> suggest(String[] pStrings, SuggestionsBuilder pBuilder) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(String s1 : pStrings) {
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            pBuilder.suggest(s1);
         }
      }

      return pBuilder.buildFuture();
   }

   static <T> CompletableFuture<Suggestions> suggest(Iterable<T> pResources, SuggestionsBuilder pBuilder, Function<T, String> pStringFunction, Function<T, Message> pSuggestionFunction) {
      String s = pBuilder.getRemaining().toLowerCase(Locale.ROOT);

      for(T t : pResources) {
         String s1 = pStringFunction.apply(t);
         if (matchesSubStr(s, s1.toLowerCase(Locale.ROOT))) {
            pBuilder.suggest(s1, pSuggestionFunction.apply(t));
         }
      }

      return pBuilder.buildFuture();
   }

   static boolean matchesSubStr(String pInput, String pSubstring) {
      for(int i = 0; !pSubstring.startsWith(pInput, i); ++i) {
         i = pSubstring.indexOf(95, i);
         if (i < 0) {
            return false;
         }
      }

      return true;
   }

   public static enum ElementSuggestionType {
      TAGS,
      ELEMENTS,
      ALL;

      public boolean shouldSuggestTags() {
         return this == TAGS || this == ALL;
      }

      public boolean shouldSuggestElements() {
         return this == ELEMENTS || this == ALL;
      }
   }

   public static class TextCoordinates {
      public static final SharedSuggestionProvider.TextCoordinates DEFAULT_LOCAL = new SharedSuggestionProvider.TextCoordinates("^", "^", "^");
      public static final SharedSuggestionProvider.TextCoordinates DEFAULT_GLOBAL = new SharedSuggestionProvider.TextCoordinates("~", "~", "~");
      public final String x;
      public final String y;
      public final String z;

      public TextCoordinates(String pX, String pY, String pZ) {
         this.x = pX;
         this.y = pY;
         this.z = pZ;
      }
   }
}