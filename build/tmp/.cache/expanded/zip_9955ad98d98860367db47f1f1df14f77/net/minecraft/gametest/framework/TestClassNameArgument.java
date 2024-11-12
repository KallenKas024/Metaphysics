package net.minecraft.gametest.framework;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class TestClassNameArgument implements ArgumentType<String> {
   private static final Collection<String> EXAMPLES = Arrays.asList("techtests", "mobtests");

   public String parse(StringReader pReader) throws CommandSyntaxException {
      String s = pReader.readUnquotedString();
      if (GameTestRegistry.isTestClass(s)) {
         return s;
      } else {
         Message message = Component.literal("No such test class: " + s);
         throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
      }
   }

   public static TestClassNameArgument testClassName() {
      return new TestClassNameArgument();
   }

   public static String getTestClassName(CommandContext<CommandSourceStack> pContext, String pArgument) {
      return pContext.getArgument(pArgument, String.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return SharedSuggestionProvider.suggest(GameTestRegistry.getAllTestClassNames().stream(), pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}