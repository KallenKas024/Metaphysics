package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
   private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.unprimed"));
   private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.invalid"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("trigger").then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((p_139146_, p_139147_) -> {
         return suggestObjectives(p_139146_.getSource(), p_139147_);
      }).executes((p_139165_) -> {
         return simpleTrigger(p_139165_.getSource(), getScore(p_139165_.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(p_139165_, "objective")));
      }).then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer()).executes((p_139159_) -> {
         return addValue(p_139159_.getSource(), getScore(p_139159_.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(p_139159_, "objective")), IntegerArgumentType.getInteger(p_139159_, "value"));
      }))).then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer()).executes((p_139144_) -> {
         return setValue(p_139144_.getSource(), getScore(p_139144_.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(p_139144_, "objective")), IntegerArgumentType.getInteger(p_139144_, "value"));
      })))));
   }

   public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack pSource, SuggestionsBuilder pBuilder) {
      Entity entity = pSource.getEntity();
      List<String> list = Lists.newArrayList();
      if (entity != null) {
         Scoreboard scoreboard = pSource.getServer().getScoreboard();
         String s = entity.getScoreboardName();

         for(Objective objective : scoreboard.getObjectives()) {
            if (objective.getCriteria() == ObjectiveCriteria.TRIGGER && scoreboard.hasPlayerScore(s, objective)) {
               Score score = scoreboard.getOrCreatePlayerScore(s, objective);
               if (!score.isLocked()) {
                  list.add(objective.getName());
               }
            }
         }
      }

      return SharedSuggestionProvider.suggest(list, pBuilder);
   }

   private static int addValue(CommandSourceStack pSource, Score pObjective, int pAmount) {
      pObjective.add(pAmount);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.trigger.add.success", pObjective.getObjective().getFormattedDisplayName(), pAmount);
      }, true);
      return pObjective.getScore();
   }

   private static int setValue(CommandSourceStack pSource, Score pObjective, int pValue) {
      pObjective.setScore(pValue);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.trigger.set.success", pObjective.getObjective().getFormattedDisplayName(), pValue);
      }, true);
      return pValue;
   }

   private static int simpleTrigger(CommandSourceStack pSource, Score pObjectives) {
      pObjectives.add(1);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.trigger.simple.success", pObjectives.getObjective().getFormattedDisplayName());
      }, true);
      return pObjectives.getScore();
   }

   private static Score getScore(ServerPlayer pPlayer, Objective pObjective) throws CommandSyntaxException {
      if (pObjective.getCriteria() != ObjectiveCriteria.TRIGGER) {
         throw ERROR_INVALID_OBJECTIVE.create();
      } else {
         Scoreboard scoreboard = pPlayer.getScoreboard();
         String s = pPlayer.getScoreboardName();
         if (!scoreboard.hasPlayerScore(s, pObjective)) {
            throw ERROR_NOT_PRIMED.create();
         } else {
            Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
            if (score.isLocked()) {
               throw ERROR_NOT_PRIMED.create();
            } else {
               score.setLocked(true);
               return score;
            }
         }
      }
   }
}