package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class ObjectiveArgument implements ArgumentType<String> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
   private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType((p_101971_) -> {
      return Component.translatable("arguments.objective.notFound", p_101971_);
   });
   private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType((p_101969_) -> {
      return Component.translatable("arguments.objective.readonly", p_101969_);
   });

   public static ObjectiveArgument objective() {
      return new ObjectiveArgument();
   }

   public static Objective getObjective(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      String s = pContext.getArgument(pName, String.class);
      Scoreboard scoreboard = pContext.getSource().getScoreboard();
      Objective objective = scoreboard.getObjective(s);
      if (objective == null) {
         throw ERROR_OBJECTIVE_NOT_FOUND.create(s);
      } else {
         return objective;
      }
   }

   public static Objective getWritableObjective(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      Objective objective = getObjective(pContext, pName);
      if (objective.getCriteria().isReadOnly()) {
         throw ERROR_OBJECTIVE_READ_ONLY.create(objective.getName());
      } else {
         return objective;
      }
   }

   public String parse(StringReader pReader) throws CommandSyntaxException {
      return pReader.readUnquotedString();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      S s = pContext.getSource();
      if (s instanceof CommandSourceStack commandsourcestack) {
         return SharedSuggestionProvider.suggest(commandsourcestack.getScoreboard().getObjectiveNames(), pBuilder);
      } else if (s instanceof SharedSuggestionProvider sharedsuggestionprovider) {
         return sharedsuggestionprovider.customSuggestion(pContext);
      } else {
         return Suggestions.empty();
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
