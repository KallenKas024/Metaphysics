package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public class ResourceOrTagArgument<T> implements ArgumentType<ResourceOrTagArgument.Result<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   private static final Dynamic2CommandExceptionType ERROR_UNKNOWN_TAG = new Dynamic2CommandExceptionType((p_250953_, p_249704_) -> {
      return Component.translatable("argument.resource_tag.not_found", p_250953_, p_249704_);
   });
   private static final Dynamic3CommandExceptionType ERROR_INVALID_TAG_TYPE = new Dynamic3CommandExceptionType((p_250188_, p_252173_, p_251453_) -> {
      return Component.translatable("argument.resource_tag.invalid_type", p_250188_, p_252173_, p_251453_);
   });
   private final HolderLookup<T> registryLookup;
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceOrTagArgument(CommandBuildContext pContext, ResourceKey<? extends Registry<T>> pRegistryKey) {
      this.registryKey = pRegistryKey;
      this.registryLookup = pContext.holderLookup(pRegistryKey);
   }

   public static <T> ResourceOrTagArgument<T> resourceOrTag(CommandBuildContext pContext, ResourceKey<? extends Registry<T>> pRegistryKey) {
      return new ResourceOrTagArgument<>(pContext, pRegistryKey);
   }

   public static <T> ResourceOrTagArgument.Result<T> getResourceOrTag(CommandContext<CommandSourceStack> pContext, String pArgument, ResourceKey<Registry<T>> pRegistryKey) throws CommandSyntaxException {
      ResourceOrTagArgument.Result<?> result = pContext.getArgument(pArgument, ResourceOrTagArgument.Result.class);
      Optional<ResourceOrTagArgument.Result<T>> optional = result.cast(pRegistryKey);
      return optional.orElseThrow(() -> {
         return result.unwrap().map((p_252340_) -> {
            ResourceKey<?> resourcekey = p_252340_.key();
            return ResourceArgument.ERROR_INVALID_RESOURCE_TYPE.create(resourcekey.location(), resourcekey.registry(), pRegistryKey.location());
         }, (p_250301_) -> {
            TagKey<?> tagkey = p_250301_.key();
            return ERROR_INVALID_TAG_TYPE.create(tagkey.location(), tagkey.registry(), pRegistryKey.location());
         });
      });
   }

   public ResourceOrTagArgument.Result<T> parse(StringReader pReader) throws CommandSyntaxException {
      if (pReader.canRead() && pReader.peek() == '#') {
         int i = pReader.getCursor();

         try {
            pReader.skip();
            ResourceLocation resourcelocation1 = ResourceLocation.read(pReader);
            TagKey<T> tagkey = TagKey.create(this.registryKey, resourcelocation1);
            HolderSet.Named<T> named = this.registryLookup.get(tagkey).orElseThrow(() -> {
               return ERROR_UNKNOWN_TAG.create(resourcelocation1, this.registryKey.location());
            });
            return new ResourceOrTagArgument.TagResult<>(named);
         } catch (CommandSyntaxException commandsyntaxexception) {
            pReader.setCursor(i);
            throw commandsyntaxexception;
         }
      } else {
         ResourceLocation resourcelocation = ResourceLocation.read(pReader);
         ResourceKey<T> resourcekey = ResourceKey.create(this.registryKey, resourcelocation);
         Holder.Reference<T> reference = this.registryLookup.get(resourcekey).orElseThrow(() -> {
            return ResourceArgument.ERROR_UNKNOWN_RESOURCE.create(resourcelocation, this.registryKey.location());
         });
         return new ResourceOrTagArgument.ResourceResult<>(reference);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      SharedSuggestionProvider.suggestResource(this.registryLookup.listTagIds().map(TagKey::location), pBuilder, "#");
      return SharedSuggestionProvider.suggestResource(this.registryLookup.listElementIds().map(ResourceKey::location), pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagArgument<T>, ResourceOrTagArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceOrTagArgument.Info<T>.Template pTemplate, FriendlyByteBuf pBuffer) {
         pBuffer.writeResourceLocation(pTemplate.registryKey.location());
      }

      public ResourceOrTagArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf pBuffer) {
         ResourceLocation resourcelocation = pBuffer.readResourceLocation();
         return new ResourceOrTagArgument.Info.Template(ResourceKey.createRegistryKey(resourcelocation));
      }

      public void serializeToJson(ResourceOrTagArgument.Info<T>.Template pTemplate, JsonObject pJson) {
         pJson.addProperty("registry", pTemplate.registryKey.location().toString());
      }

      public ResourceOrTagArgument.Info<T>.Template unpack(ResourceOrTagArgument<T> pArgument) {
         return new ResourceOrTagArgument.Info.Template(pArgument.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(ResourceKey<? extends Registry<T>> pRegistryKey) {
            this.registryKey = pRegistryKey;
         }

         public ResourceOrTagArgument<T> instantiate(CommandBuildContext pContext) {
            return new ResourceOrTagArgument<>(pContext, this.registryKey);
         }

         public ArgumentTypeInfo<ResourceOrTagArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }

   static record ResourceResult<T>(Holder.Reference<T> value) implements ResourceOrTagArgument.Result<T> {
      public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
         return Either.left(this.value);
      }

      public <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey) {
         return this.value.key().isFor(pRegistryKey) ? Optional.of((ResourceOrTagArgument.Result) this) : Optional.empty();
      }

      public boolean test(Holder<T> pHolder) {
         return pHolder.equals(this.value);
      }

      public String asPrintable() {
         return this.value.key().location().toString();
      }
   }

   public interface Result<T> extends Predicate<Holder<T>> {
      Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap();

      <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey);

      String asPrintable();
   }

   static record TagResult<T>(HolderSet.Named<T> tag) implements ResourceOrTagArgument.Result<T> {
      public Either<Holder.Reference<T>, HolderSet.Named<T>> unwrap() {
         return Either.right(this.tag);
      }

      public <E> Optional<ResourceOrTagArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> pRegistryKey) {
         return this.tag.key().isFor(pRegistryKey) ? Optional.of((ResourceOrTagArgument.Result) this) : Optional.empty();
      }

      public boolean test(Holder<T> pHolder) {
         return this.tag.contains(pHolder);
      }

      public String asPrintable() {
         return "#" + this.tag.key().location();
      }
   }
}