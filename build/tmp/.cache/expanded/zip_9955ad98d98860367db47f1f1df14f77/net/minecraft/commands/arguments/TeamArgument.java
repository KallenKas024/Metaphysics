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
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeamArgument implements ArgumentType<String> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "123");
   private static final DynamicCommandExceptionType ERROR_TEAM_NOT_FOUND = new DynamicCommandExceptionType((p_112095_) -> {
      return Component.translatable("team.notFound", p_112095_);
   });

   public static TeamArgument team() {
      return new TeamArgument();
   }

   public static PlayerTeam getTeam(CommandContext<CommandSourceStack> pContext, String pName) throws CommandSyntaxException {
      String s = pContext.getArgument(pName, String.class);
      Scoreboard scoreboard = pContext.getSource().getScoreboard();
      PlayerTeam playerteam = scoreboard.getPlayerTeam(s);
      if (playerteam == null) {
         throw ERROR_TEAM_NOT_FOUND.create(s);
      } else {
         return playerteam;
      }
   }

   public String parse(StringReader pReader) throws CommandSyntaxException {
      return pReader.readUnquotedString();
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
      return pContext.getSource() instanceof SharedSuggestionProvider ? SharedSuggestionProvider.suggest(((SharedSuggestionProvider)pContext.getSource()).getAllTeams(), pBuilder) : Suggestions.empty();
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
