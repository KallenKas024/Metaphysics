package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T> implements ArgumentType<Holder.Reference<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType((p_248875_) -> {
      return Component.translatable("entity.not_summonable", p_248875_);
   });
   public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType((p_248525_, p_251552_) -> {
      return Component.translatable("argument.resource.not_found", p_248525_, p_251552_);
   });
   public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType((p_250883_, p_249983_, p_249882_) -> {
      return Component.translatable("argument.resource.invalid_type", p_250883_, p_249983_, p_249882_);
   });
   final ResourceKey<? extends Registry<T>> registryKey;
   private final HolderLookup<T> registryLookup;

   public ResourceArgument(CommandBuildContext pContext, ResourceKey<? extends Registry<T>> pRegistryKey) {
      this.registryKey = pRegistryKey;
      this.registryLookup = pContext.holderLookup(pRegistryKey);
   }

   public static <T> ResourceArgument<T> resource(CommandBuildContext pContext, ResourceKey<? extends Registry<T>> pRegistryKey) {
      return new ResourceArgument<>(pContext, pRegistryKey);
   }

   public static <T> Holder.Reference<T> getResource(CommandContext<CommandSourceStack> pContext, String pArgument, ResourceKey<Registry<T>> pRegistryKey) throws CommandSyntaxException {
      Holder.Reference<T> reference = pContext.getArgument(pArgument, Holder.Reference.class);
      ResourceKey<?> resourcekey = reference.key();
      if (resourcekey.isFor(pRegistryKey)) {
         return reference;
      } else {
         throw ERROR_INVALID_RESOURCE_TYPE.create(resourcekey.location(), resourcekey.registry(), pRegistryKey.location());
      }
   }

   public static Holder.Reference<Attribute> getAttribute(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return getResource(pContext, pArgument, Registries.ATTRIBUTE);
   }

   public static Holder.Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return getResource(pContext, pArgument, Registries.CONFIGURED_FEATURE);
   }

   public static Holder.Reference<Structure> getStructure(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return getResource(pContext, pArgument, Registries.STRUCTURE);
   }

   public static Holder.Reference<EntityType<?>> getEntityType(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return getResource(pContext, pArgument, Registries.ENTITY_TYPE);
   }

   public static Holder.Reference<EntityType<?>> getSummonableEntityType(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      Holder.Reference<EntityType<?>> reference = getResource(pContext, pArgument, Registries.ENTITY_TYPE);
      if (!reference.value().canSummon()) {
         throw ERROR_NOT_SUMMONABLE_ENTITY.create(reference.key().location().toString());
      } else {
         return reference;
      }
   }

   public static Holder.Reference<MobEffect> getMobEffect(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return getResource(pContext, pArgument, Registries.MOB_EFFECT);
   }

   public static Holder.Reference<Enchantment> getEnchantment(CommandContext<CommandSourceStack> pContext, String pArgument) throws CommandSyntaxException {
      return getResource(pContext, pArgument, Registries.ENCHANTMENT);
   }

   public Holder.Reference<T> parse(StringReader pBuilder) throws CommandSyntaxException {
      ResourceLocation resourcelocation = ResourceLocation.read(pBuilder);
      ResourceKey<T> resourcekey = ResourceKey.create(this.registryKey, resourcelocation);
      return this.registryLookup.get(resourcekey).orElseThrow(() -> {
         return ERROR_UNKNOWN_RESOURCE.create(resourcelocation, this.registryKey.location());
      });
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceArgument<T>, ResourceArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceArgument.Info<T>.Template pTemplate, FriendlyByteBuf pBuffer) {
         pBuffer.writeResourceLocation(pTemplate.registryKey.location());
      }

      public ResourceArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
         ResourceLocation resourcelocation = pBuffer.readResourceLocation();
         return new ResourceArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceArgument.Info<T>.Template pTemplate, JsonObject pJson) {
         pJson.addProperty("registry", pTemplate.registryKey.location().toString());
      }

      public ResourceArgument.Info<T>.Template unpack(ResourceArgument<T> pArgument) {
         return new ResourceArgument.Info.Template(pArgument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> pRegistryKey) {
            this.registryKey = pRegistryKey;
         }

         public ResourceArgument<T> instantiate(CommandBuildContext pContext) {
            return new ResourceArgument<>(pContext, this.registryKey);
         }

         public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}