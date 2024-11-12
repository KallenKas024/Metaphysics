package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardCommand {
   private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.add.duplicate"));
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty"));
   private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.objectives.display.alreadySet"));
   private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.players.enable.failed"));
   private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(Component.translatable("commands.scoreboard.players.enable.invalid"));
   private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType((p_138534_, p_138535_) -> {
      return Component.translatable("commands.scoreboard.players.get.null", p_138534_, p_138535_);
   });

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("scoreboard").requires((p_138552_) -> {
         return p_138552_.hasPermission(2);
      }).then(Commands.literal("objectives").then(Commands.literal("list").executes((p_138585_) -> {
         return listObjectives(p_138585_.getSource());
      })).then(Commands.literal("add").then(Commands.argument("objective", StringArgumentType.word()).then(Commands.argument("criteria", ObjectiveCriteriaArgument.criteria()).executes((p_138583_) -> {
         return addObjective(p_138583_.getSource(), StringArgumentType.getString(p_138583_, "objective"), ObjectiveCriteriaArgument.getCriteria(p_138583_, "criteria"), Component.literal(StringArgumentType.getString(p_138583_, "objective")));
      }).then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_138581_) -> {
         return addObjective(p_138581_.getSource(), StringArgumentType.getString(p_138581_, "objective"), ObjectiveCriteriaArgument.getCriteria(p_138581_, "criteria"), ComponentArgument.getComponent(p_138581_, "displayName"));
      }))))).then(Commands.literal("modify").then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.literal("displayname").then(Commands.argument("displayName", ComponentArgument.textComponent()).executes((p_138579_) -> {
         return setDisplayName(p_138579_.getSource(), ObjectiveArgument.getObjective(p_138579_, "objective"), ComponentArgument.getComponent(p_138579_, "displayName"));
      }))).then(createRenderTypeModify()))).then(Commands.literal("remove").then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_138577_) -> {
         return removeObjective(p_138577_.getSource(), ObjectiveArgument.getObjective(p_138577_, "objective"));
      }))).then(Commands.literal("setdisplay").then(Commands.argument("slot", ScoreboardSlotArgument.displaySlot()).executes((p_138575_) -> {
         return clearDisplaySlot(p_138575_.getSource(), ScoreboardSlotArgument.getDisplaySlot(p_138575_, "slot"));
      }).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_138573_) -> {
         return setDisplaySlot(p_138573_.getSource(), ScoreboardSlotArgument.getDisplaySlot(p_138573_, "slot"), ObjectiveArgument.getObjective(p_138573_, "objective"));
      }))))).then(Commands.literal("players").then(Commands.literal("list").executes((p_138571_) -> {
         return listTrackedPlayers(p_138571_.getSource());
      }).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_138569_) -> {
         return listTrackedPlayerScores(p_138569_.getSource(), ScoreHolderArgument.getName(p_138569_, "target"));
      }))).then(Commands.literal("set").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer()).executes((p_138567_) -> {
         return setScore(p_138567_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138567_, "targets"), ObjectiveArgument.getWritableObjective(p_138567_, "objective"), IntegerArgumentType.getInteger(p_138567_, "score"));
      }))))).then(Commands.literal("get").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_138565_) -> {
         return getScore(p_138565_.getSource(), ScoreHolderArgument.getName(p_138565_, "target"), ObjectiveArgument.getObjective(p_138565_, "objective"));
      })))).then(Commands.literal("add").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((p_138563_) -> {
         return addScore(p_138563_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138563_, "targets"), ObjectiveArgument.getWritableObjective(p_138563_, "objective"), IntegerArgumentType.getInteger(p_138563_, "score"));
      }))))).then(Commands.literal("remove").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((p_138561_) -> {
         return removeScore(p_138561_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138561_, "targets"), ObjectiveArgument.getWritableObjective(p_138561_, "objective"), IntegerArgumentType.getInteger(p_138561_, "score"));
      }))))).then(Commands.literal("reset").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).executes((p_138559_) -> {
         return resetScores(p_138559_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138559_, "targets"));
      }).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_138550_) -> {
         return resetScore(p_138550_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138550_, "targets"), ObjectiveArgument.getObjective(p_138550_, "objective"));
      })))).then(Commands.literal("enable").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((p_138473_, p_138474_) -> {
         return suggestTriggers(p_138473_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138473_, "targets"), p_138474_);
      }).executes((p_138537_) -> {
         return enableTrigger(p_138537_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138537_, "targets"), ObjectiveArgument.getObjective(p_138537_, "objective"));
      })))).then(Commands.literal("operation").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.argument("operation", OperationArgument.operation()).then(Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS).then(Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes((p_138471_) -> {
         return performOperation(p_138471_.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138471_, "targets"), ObjectiveArgument.getWritableObjective(p_138471_, "targetObjective"), OperationArgument.getOperation(p_138471_, "operation"), ScoreHolderArgument.getNamesWithDefaultWildcard(p_138471_, "source"), ObjectiveArgument.getObjective(p_138471_, "sourceObjective"));
      })))))))));
   }

   private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
      LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("rendertype");

      for(ObjectiveCriteria.RenderType objectivecriteria$rendertype : ObjectiveCriteria.RenderType.values()) {
         literalargumentbuilder.then(Commands.literal(objectivecriteria$rendertype.getId()).executes((p_138532_) -> {
            return setRenderType(p_138532_.getSource(), ObjectiveArgument.getObjective(p_138532_, "objective"), objectivecriteria$rendertype);
         }));
      }

      return literalargumentbuilder;
   }

   private static CompletableFuture<Suggestions> suggestTriggers(CommandSourceStack pSource, Collection<String> pTargets, SuggestionsBuilder pSuggestions) {
      List<String> list = Lists.newArrayList();
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(Objective objective : scoreboard.getObjectives()) {
         if (objective.getCriteria() == ObjectiveCriteria.TRIGGER) {
            boolean flag = false;

            for(String s : pTargets) {
               if (!scoreboard.hasPlayerScore(s, objective) || scoreboard.getOrCreatePlayerScore(s, objective).isLocked()) {
                  flag = true;
                  break;
               }
            }

            if (flag) {
               list.add(objective.getName());
            }
         }
      }

      return SharedSuggestionProvider.suggest(list, pSuggestions);
   }

   private static int getScore(CommandSourceStack pSource, String pPlayer, Objective pObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (!scoreboard.hasPlayerScore(pPlayer, pObjective)) {
         throw ERROR_NO_VALUE.create(pObjective.getName(), pPlayer);
      } else {
         Score score = scoreboard.getOrCreatePlayerScore(pPlayer, pObjective);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.get.success", pPlayer, score.getScore(), pObjective.getFormattedDisplayName());
         }, false);
         return score.getScore();
      }
   }

   private static int performOperation(CommandSourceStack pSource, Collection<String> pTargetEntities, Objective pTargetObjectives, OperationArgument.Operation pOperation, Collection<String> pSourceEntities, Objective pSourceObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      int i = 0;

      for(String s : pTargetEntities) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pTargetObjectives);

         for(String s1 : pSourceEntities) {
            Score score1 = scoreboard.getOrCreatePlayerScore(s1, pSourceObjective);
            pOperation.apply(score, score1);
         }

         i += score.getScore();
      }

      if (pTargetEntities.size() == 1) {
         int j = i;
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.operation.success.single", pTargetObjectives.getFormattedDisplayName(), pTargetEntities.iterator().next(), j);
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.operation.success.multiple", pTargetObjectives.getFormattedDisplayName(), pTargetEntities.size());
         }, true);
      }

      return i;
   }

   private static int enableTrigger(CommandSourceStack pSource, Collection<String> pTargets, Objective pObjective) throws CommandSyntaxException {
      if (pObjective.getCriteria() != ObjectiveCriteria.TRIGGER) {
         throw ERROR_NOT_TRIGGER.create();
      } else {
         Scoreboard scoreboard = pSource.getServer().getScoreboard();
         int i = 0;

         for(String s : pTargets) {
            Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
            if (score.isLocked()) {
               score.setLocked(false);
               ++i;
            }
         }

         if (i == 0) {
            throw ERROR_TRIGGER_ALREADY_ENABLED.create();
         } else {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(() -> {
                  return Component.translatable("commands.scoreboard.players.enable.success.single", pObjective.getFormattedDisplayName(), pTargets.iterator().next());
               }, true);
            } else {
               pSource.sendSuccess(() -> {
                  return Component.translatable("commands.scoreboard.players.enable.success.multiple", pObjective.getFormattedDisplayName(), pTargets.size());
               }, true);
            }

            return i;
         }
      }
   }

   private static int resetScores(CommandSourceStack pSource, Collection<String> pTargets) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pTargets) {
         scoreboard.resetPlayerScore(s, (Objective)null);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.reset.all.single", pTargets.iterator().next());
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.reset.all.multiple", pTargets.size());
         }, true);
      }

      return pTargets.size();
   }

   private static int resetScore(CommandSourceStack pSource, Collection<String> pTargets, Objective pObjective) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pTargets) {
         scoreboard.resetPlayerScore(s, pObjective);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.reset.specific.single", pObjective.getFormattedDisplayName(), pTargets.iterator().next());
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.reset.specific.multiple", pObjective.getFormattedDisplayName(), pTargets.size());
         }, true);
      }

      return pTargets.size();
   }

   private static int setScore(CommandSourceStack pSource, Collection<String> pTargets, Objective pObjective, int pNewValue) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();

      for(String s : pTargets) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
         score.setScore(pNewValue);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.set.success.single", pObjective.getFormattedDisplayName(), pTargets.iterator().next(), pNewValue);
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.set.success.multiple", pObjective.getFormattedDisplayName(), pTargets.size(), pNewValue);
         }, true);
      }

      return pNewValue * pTargets.size();
   }

   private static int addScore(CommandSourceStack pSource, Collection<String> pTargets, Objective pObjective, int pAmount) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      int i = 0;

      for(String s : pTargets) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
         score.setScore(score.getScore() + pAmount);
         i += score.getScore();
      }

      if (pTargets.size() == 1) {
         int j = i;
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.add.success.single", pAmount, pObjective.getFormattedDisplayName(), pTargets.iterator().next(), j);
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.add.success.multiple", pAmount, pObjective.getFormattedDisplayName(), pTargets.size());
         }, true);
      }

      return i;
   }

   private static int removeScore(CommandSourceStack pSource, Collection<String> pTargets, Objective pObjective, int pAmount) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      int i = 0;

      for(String s : pTargets) {
         Score score = scoreboard.getOrCreatePlayerScore(s, pObjective);
         score.setScore(score.getScore() - pAmount);
         i += score.getScore();
      }

      if (pTargets.size() == 1) {
         int j = i;
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.remove.success.single", pAmount, pObjective.getFormattedDisplayName(), pTargets.iterator().next(), j);
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.remove.success.multiple", pAmount, pObjective.getFormattedDisplayName(), pTargets.size());
         }, true);
      }

      return i;
   }

   private static int listTrackedPlayers(CommandSourceStack pSource) {
      Collection<String> collection = pSource.getServer().getScoreboard().getTrackedPlayers();
      if (collection.isEmpty()) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.list.empty");
         }, false);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.list.success", collection.size(), ComponentUtils.formatList(collection));
         }, false);
      }

      return collection.size();
   }

   private static int listTrackedPlayerScores(CommandSourceStack pSource, String pPlayer) {
      Map<Objective, Score> map = pSource.getServer().getScoreboard().getPlayerScores(pPlayer);
      if (map.isEmpty()) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.list.entity.empty", pPlayer);
         }, false);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.players.list.entity.success", pPlayer, map.size());
         }, false);

         for(Map.Entry<Objective, Score> entry : map.entrySet()) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.scoreboard.players.list.entity.entry", entry.getKey().getFormattedDisplayName(), entry.getValue().getScore());
            }, false);
         }
      }

      return map.size();
   }

   private static int clearDisplaySlot(CommandSourceStack pSource, int pSlotId) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getDisplayObjective(pSlotId) == null) {
         throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
      } else {
         scoreboard.setDisplayObjective(pSlotId, (Objective)null);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.display.cleared", Scoreboard.getDisplaySlotNames()[pSlotId]);
         }, true);
         return 0;
      }
   }

   private static int setDisplaySlot(CommandSourceStack pSource, int pSlotId, Objective pObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getDisplayObjective(pSlotId) == pObjective) {
         throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
      } else {
         scoreboard.setDisplayObjective(pSlotId, pObjective);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.display.set", Scoreboard.getDisplaySlotNames()[pSlotId], pObjective.getDisplayName());
         }, true);
         return 0;
      }
   }

   private static int setDisplayName(CommandSourceStack pSource, Objective pObjective, Component pDisplayName) {
      if (!pObjective.getDisplayName().equals(pDisplayName)) {
         pObjective.setDisplayName(pDisplayName);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.modify.displayname", pObjective.getName(), pObjective.getFormattedDisplayName());
         }, true);
      }

      return 0;
   }

   private static int setRenderType(CommandSourceStack pSource, Objective pObjective, ObjectiveCriteria.RenderType pRenderType) {
      if (pObjective.getRenderType() != pRenderType) {
         pObjective.setRenderType(pRenderType);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.modify.rendertype", pObjective.getFormattedDisplayName());
         }, true);
      }

      return 0;
   }

   private static int removeObjective(CommandSourceStack pSource, Objective pObjective) {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      scoreboard.removeObjective(pObjective);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.scoreboard.objectives.remove.success", pObjective.getFormattedDisplayName());
      }, true);
      return scoreboard.getObjectives().size();
   }

   private static int addObjective(CommandSourceStack pSource, String pName, ObjectiveCriteria pCriteria, Component pDisplayName) throws CommandSyntaxException {
      Scoreboard scoreboard = pSource.getServer().getScoreboard();
      if (scoreboard.getObjective(pName) != null) {
         throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
      } else {
         scoreboard.addObjective(pName, pCriteria, pDisplayName, pCriteria.getDefaultRenderType());
         Objective objective = scoreboard.getObjective(pName);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.add.success", objective.getFormattedDisplayName());
         }, true);
         return scoreboard.getObjectives().size();
      }
   }

   private static int listObjectives(CommandSourceStack pSource) {
      Collection<Objective> collection = pSource.getServer().getScoreboard().getObjectives();
      if (collection.isEmpty()) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.list.empty");
         }, false);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.scoreboard.objectives.list.success", collection.size(), ComponentUtils.formatList(collection, Objective::getFormattedDisplayName));
         }, false);
      }

      return collection.size();
   }
}