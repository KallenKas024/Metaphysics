package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType((p_212392_) -> {
      return Component.translatable("commands.place.feature.invalid", p_212392_);
   });
   private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType((p_212385_) -> {
      return Component.translatable("commands.place.structure.invalid", p_212385_);
   });
   private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType((p_233264_) -> {
      return Component.translatable("commands.place.jigsaw.invalid", p_233264_);
   });
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceKeyArgument(ResourceKey<? extends Registry<T>> pRegistryKey) {
      this.registryKey = pRegistryKey;
   }

   public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> pRegistryKey) {
      return new ResourceKeyArgument<>(pRegistryKey);
   }

   private static <T> ResourceKey<T> getRegistryKey(CommandContext<CommandSourceStack> pContext, String pArgument, ResourceKey<Registry<T>> pRegistryKey, DynamicCommandExceptionType pException) throws CommandSyntaxException {
      ResourceKey<?> resourcekey = pContext.getArgument(pArgument, ResourceKey.class);
      Optional<ResourceKey<T>> optional = resourcekey.cast(pRegistryKey);
      return optional.orElseThrow(() -> {
         return pException.create(resourcekey);
      });
   }

   private static <T> Registry<T> getRegistry(CommandContext<CommandSourceStack> pContext, ResourceKey<? extends Registry<T>> pRegistryKey) {
      return pContext.getSource().getServer().registryAccess().registryOrThrow(pRegistryKey);
   }

   private static <T> Holder.Reference<T> resolveKey(CommandContext<CommandSourceStack> pContext, String pArgument, ResourceKey<Registry<T>> pRegistryKey, DynamicCommandExceptionType pException) throws CommandSyntaxException {
      ResourceKey<T> resourcekey = getRegistryKey(pContext, pArgument, pRegistryKey, pException);
      return getRegistry(pContext, pRegistryKey).getHolder(resourcekey).orElseThrow(() -> {
         return pException.create(resourcekey.location());
      });
   }

   public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return resolveKey(pContext, pArgument, Registries.CONFIGURED_FEATURE, ERROR_INVALID_FEATURE);
   }

   public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return resolveKey(pContext, pArgument, Registries.STRUCTURE, ERROR_INVALID_STRUCTURE);
   }

   public static Holder.Reference<StructureTemplatePool> getStructureTemplatePool(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return resolveKey(pContext, pArgument, Registries.TEMPLATE_POOL, ERROR_INVALID_TEMPLATE_POOL);
   }

   public ResourceKey<T> parse(StringReader pReader) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.read(pReader);
      return ResourceKey.create(this.registryKey, resourcelocation);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      Object object = pContext.getSource();
      if (object instanceof SharedSuggestionProvider sharedsuggestionprovider) {
         return sharedsuggestionprovider.suggestRegistryElements(this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS, pBuilder, pContext);
      } else {
         return pBuilder.buildFuture();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceKeyArgument<T>, ResourceKeyArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceKeyArgument.Info<T>.Template pTemplate, FriendlyByteBuf pBuffer) {
         pBuffer.writeResourceLocation(pTemplate.registryKey.location());
      }

      public ResourceKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
         ResourceLocation resourcelocation = pBuffer.readResourceLocation();
         return new ResourceKeyArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceKeyArgument.Info<T>.Template pTemplate, JsonObject pJson) {
         pJson.addProperty("registry", pTemplate.registryKey.location().toString());
      }

      public ResourceKeyArgument.Info<T>.Template unpack(ResourceKeyArgument<T> pArgument) {
         return new ResourceKeyArgument.Info.Template(pArgument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> pRegistryKey) {
            this.registryKey = pRegistryKey;
         }

         public ResourceKeyArgument<T> instantiate(CommandBuildContext pContext) {
            return new ResourceKeyArgument<>(this.registryKey);
         }

         public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}