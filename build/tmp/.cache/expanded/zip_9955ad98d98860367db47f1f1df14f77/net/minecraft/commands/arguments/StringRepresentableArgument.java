package net.minecraft.commands.arguments;

import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public class StringRepresentableArgument<T extends Enum<T> & StringRepresentable> implements ArgumentType<T> {
   private static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((p_234071_) -> {
      return Component.translatable("argument.enum.invalid", p_234071_);
   });
   private final Codec<T> codec;
   private final Supplier<T[]> values;

   protected StringRepresentableArgument(Codec<T> pCodec, Supplier<T[]> pValues) {
      this.codec = pCodec;
      this.values = pValues;
   }

   public T parse(StringReader pStringReader) throws CommandSyntaxException {
      String s = pStringReader.readUnquotedString();
      return this.codec.parse(JsonOps.INSTANCE, new JsonPrimitive(s)).result().orElseThrow(() -> {
         return ERROR_INVALID_VALUE.create(s);
      });
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggest(Arrays.<Enum>stream((Enum[])this.values.get()).map((p_234069_) -> {
         return ((StringRepresentable)p_234069_).getSerializedName();
      }).map(this::convertId).collect(Collectors.toList()), pBuilder);
   }

   public Collection<String> getExamples() {
      return Arrays.<Enum>stream((Enum[])this.values.get()).map((p_234065_) -> {
         return ((StringRepresentable)p_234065_).getSerializedName();
      }).map(this::convertId).limit(2L).collect(Collectors.toList());
   }

   protected String convertId(String p_275436_) {
      return p_275436_;
   }
}