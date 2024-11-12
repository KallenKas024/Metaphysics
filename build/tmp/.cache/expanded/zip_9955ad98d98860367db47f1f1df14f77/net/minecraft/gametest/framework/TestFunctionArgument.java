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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class TestFunctionArgument implements ArgumentType<TestFunction> {
   private static final Collection<String> EXAMPLES = Arrays.asList("techtests.piston", "techtests");

   public TestFunction parse(StringReader pReader) throws CommandSyntaxException {
      String s = pReader.readUnquotedString();
      Optional<TestFunction> optional = GameTestRegistry.findTestFunction(s);
      if (optional.isPresent()) {
         return optional.get();
      } else {
         Message message = Component.literal("No such test: " + s);
         throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
      }
   }

   public static TestFunctionArgument testFunctionArgument() {
      return new TestFunctionArgument();
   }

   public static TestFunction getTestFunction(CommandContext<CommandSourceStack> pContext, String pArgument) {
      return pContext.getArgument(pArgument, TestFunction.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      Stream<String> stream = GameTestRegistry.getAllTestFunctions().stream().map(TestFunction::getTestName);
      return SharedSuggestionProvider.suggest(stream, pBuilder);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}