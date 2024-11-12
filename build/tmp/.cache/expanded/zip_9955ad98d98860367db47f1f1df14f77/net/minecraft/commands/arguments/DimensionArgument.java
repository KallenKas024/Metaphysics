package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class DimensionArgument implements ArgumentType<ResourceLocation> {
   private static final Collection<String> EXAMPLES = Stream.of(Level.OVERWORLD, Level.NETHER).map((p_88814_) -> {
      return p_88814_.location().toString();
   }).collect(Collectors.toList());
   private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_88812_) -> {
      return Component.translatable("argument.dimension.invalid", p_88812_);
   });

   public ResourceLocation parse(StringReader pReader) throws CommandSyntaxException {
      return ResourceLocation.read(pReader);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return pContext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)pContext.getSource()).levels().stream().map(ResourceKey::location), pBuilder) : Suggestions.empty();
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static DimensionArgument dimension() {
      return new DimensionArgument();
   }

   public static ServerLevel getDimension(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      ResourceLocation resourcelocation = pContext.getArgument(pName, ResourceLocation.class);
      ResourceKey<Level> resourcekey = ResourceKey.create(Registries.DIMENSION, resourcelocation);
      ServerLevel serverlevel = pContext.getSource().getServer().getLevel(resourcekey);
      if (serverlevel == null) {
         throw ERROR_INVALID_VALUE.create(resourcelocation);
      } else {
         return serverlevel;
      }
   }
}