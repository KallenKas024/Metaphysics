package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
   private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (p_136344_, p_136345_) -> {
      Collection<Advancement> collection = p_136344_.getSource().getServer().getAdvancements().getAllAdvancements();
      return SharedSuggestionProvider.suggestResource(collection.stream().map(Advancement::getId), p_136345_);
   };

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("advancement").requires((p_136318_) -> {
         return p_136318_.hasPermission(2);
      }).then(Commands.literal("grant").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136363_) -> {
         return perform(p_136363_.getSource(), EntityArgument.getPlayers(p_136363_, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_136363_, "advancement"), AdvancementCommands.Mode.ONLY));
      }).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((p_136339_, p_136340_) -> {
         return SharedSuggestionProvider.suggest(ResourceLocationArgument.getAdvancement(p_136339_, "advancement").getCriteria().keySet(), p_136340_);
      }).executes((p_136361_) -> {
         return performCriterion(p_136361_.getSource(), EntityArgument.getPlayers(p_136361_, "targets"), AdvancementCommands.Action.GRANT, ResourceLocationArgument.getAdvancement(p_136361_, "advancement"), StringArgumentType.getString(p_136361_, "criterion"));
      })))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136359_) -> {
         return perform(p_136359_.getSource(), EntityArgument.getPlayers(p_136359_, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_136359_, "advancement"), AdvancementCommands.Mode.FROM));
      }))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136357_) -> {
         return perform(p_136357_.getSource(), EntityArgument.getPlayers(p_136357_, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_136357_, "advancement"), AdvancementCommands.Mode.UNTIL));
      }))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136355_) -> {
         return perform(p_136355_.getSource(), EntityArgument.getPlayers(p_136355_, "targets"), AdvancementCommands.Action.GRANT, getAdvancements(ResourceLocationArgument.getAdvancement(p_136355_, "advancement"), AdvancementCommands.Mode.THROUGH));
      }))).then(Commands.literal("everything").executes((p_136353_) -> {
         return perform(p_136353_.getSource(), EntityArgument.getPlayers(p_136353_, "targets"), AdvancementCommands.Action.GRANT, p_136353_.getSource().getServer().getAdvancements().getAllAdvancements());
      })))).then(Commands.literal("revoke").then(Commands.argument("targets", EntityArgument.players()).then(Commands.literal("only").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136351_) -> {
         return perform(p_136351_.getSource(), EntityArgument.getPlayers(p_136351_, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_136351_, "advancement"), AdvancementCommands.Mode.ONLY));
      }).then(Commands.argument("criterion", StringArgumentType.greedyString()).suggests((p_136315_, p_136316_) -> {
         return SharedSuggestionProvider.suggest(ResourceLocationArgument.getAdvancement(p_136315_, "advancement").getCriteria().keySet(), p_136316_);
      }).executes((p_136349_) -> {
         return performCriterion(p_136349_.getSource(), EntityArgument.getPlayers(p_136349_, "targets"), AdvancementCommands.Action.REVOKE, ResourceLocationArgument.getAdvancement(p_136349_, "advancement"), StringArgumentType.getString(p_136349_, "criterion"));
      })))).then(Commands.literal("from").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136347_) -> {
         return perform(p_136347_.getSource(), EntityArgument.getPlayers(p_136347_, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_136347_, "advancement"), AdvancementCommands.Mode.FROM));
      }))).then(Commands.literal("until").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136342_) -> {
         return perform(p_136342_.getSource(), EntityArgument.getPlayers(p_136342_, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_136342_, "advancement"), AdvancementCommands.Mode.UNTIL));
      }))).then(Commands.literal("through").then(Commands.argument("advancement", ResourceLocationArgument.id()).suggests(SUGGEST_ADVANCEMENTS).executes((p_136337_) -> {
         return perform(p_136337_.getSource(), EntityArgument.getPlayers(p_136337_, "targets"), AdvancementCommands.Action.REVOKE, getAdvancements(ResourceLocationArgument.getAdvancement(p_136337_, "advancement"), AdvancementCommands.Mode.THROUGH));
      }))).then(Commands.literal("everything").executes((p_136313_) -> {
         return perform(p_136313_.getSource(), EntityArgument.getPlayers(p_136313_, "targets"), AdvancementCommands.Action.REVOKE, p_136313_.getSource().getServer().getAdvancements().getAllAdvancements());
      })))));
   }

   /**
    * Performs the given action on each advancement in the list, for each player.
    * 
    * @return The number of affected advancements across all players.
    */
   private static int perform(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, AdvancementCommands.Action pAction, Collection<Advancement> pAdvancements) {
      int i = 0;

      for(ServerPlayer serverplayer : pTargets) {
         i += pAction.perform(serverplayer, pAdvancements);
      }

      if (i == 0) {
         if (pAdvancements.size() == 1) {
            if (pTargets.size() == 1) {
               throw new CommandRuntimeException(Component.translatable(pAction.getKey() + ".one.to.one.failure", pAdvancements.iterator().next().getChatComponent(), pTargets.iterator().next().getDisplayName()));
            } else {
               throw new CommandRuntimeException(Component.translatable(pAction.getKey() + ".one.to.many.failure", pAdvancements.iterator().next().getChatComponent(), pTargets.size()));
            }
         } else if (pTargets.size() == 1) {
            throw new CommandRuntimeException(Component.translatable(pAction.getKey() + ".many.to.one.failure", pAdvancements.size(), pTargets.iterator().next().getDisplayName()));
         } else {
            throw new CommandRuntimeException(Component.translatable(pAction.getKey() + ".many.to.many.failure", pAdvancements.size(), pTargets.size()));
         }
      } else {
         if (pAdvancements.size() == 1) {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(() -> {
                  return Component.translatable(pAction.getKey() + ".one.to.one.success", pAdvancements.iterator().next().getChatComponent(), pTargets.iterator().next().getDisplayName());
               }, true);
            } else {
               pSource.sendSuccess(() -> {
                  return Component.translatable(pAction.getKey() + ".one.to.many.success", pAdvancements.iterator().next().getChatComponent(), pTargets.size());
               }, true);
            }
         } else if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> {
               return Component.translatable(pAction.getKey() + ".many.to.one.success", pAdvancements.size(), pTargets.iterator().next().getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable(pAction.getKey() + ".many.to.many.success", pAdvancements.size(), pTargets.size());
            }, true);
         }

         return i;
      }
   }

   /**
    * Updates a single criterion based on the given action.
    * 
    * @return The number of affected criteria across all players.
    */
   private static int performCriterion(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, AdvancementCommands.Action pAction, Advancement pAdvancement, String pCriterionName) {
      int i = 0;
      if (!pAdvancement.getCriteria().containsKey(pCriterionName)) {
         throw new CommandRuntimeException(Component.translatable("commands.advancement.criterionNotFound", pAdvancement.getChatComponent(), pCriterionName));
      } else {
         for(ServerPlayer serverplayer : pTargets) {
            if (pAction.performCriterion(serverplayer, pAdvancement, pCriterionName)) {
               ++i;
            }
         }

         if (i == 0) {
            if (pTargets.size() == 1) {
               throw new CommandRuntimeException(Component.translatable(pAction.getKey() + ".criterion.to.one.failure", pCriterionName, pAdvancement.getChatComponent(), pTargets.iterator().next().getDisplayName()));
            } else {
               throw new CommandRuntimeException(Component.translatable(pAction.getKey() + ".criterion.to.many.failure", pCriterionName, pAdvancement.getChatComponent(), pTargets.size()));
            }
         } else {
            if (pTargets.size() == 1) {
               pSource.sendSuccess(() -> {
                  return Component.translatable(pAction.getKey() + ".criterion.to.one.success", pCriterionName, pAdvancement.getChatComponent(), pTargets.iterator().next().getDisplayName());
               }, true);
            } else {
               pSource.sendSuccess(() -> {
                  return Component.translatable(pAction.getKey() + ".criterion.to.many.success", pCriterionName, pAdvancement.getChatComponent(), pTargets.size());
               }, true);
            }

            return i;
         }
      }
   }

   /**
    * Gets all advancements that match the given mode.
    */
   private static List<Advancement> getAdvancements(Advancement pAdvancement, AdvancementCommands.Mode pMode) {
      List<Advancement> list = Lists.newArrayList();
      if (pMode.parents) {
         for(Advancement advancement = pAdvancement.getParent(); advancement != null; advancement = advancement.getParent()) {
            list.add(advancement);
         }
      }

      list.add(pAdvancement);
      if (pMode.children) {
         addChildren(pAdvancement, list);
      }

      return list;
   }

   /**
    * Recursively adds all children of the given advancement to the given list. Does not add the advancement itself to
    * the list.
    */
   private static void addChildren(Advancement pAdvancement, List<Advancement> pList) {
      for(Advancement advancement : pAdvancement.getChildren()) {
         pList.add(advancement);
         addChildren(advancement, pList);
      }

   }

   static enum Action {
      GRANT("grant") {
         /**
          * Applies this action to the given advancement.
          * 
          * @return True if the player was affected.
          */
         protected boolean perform(ServerPlayer p_136395_, Advancement p_136396_) {
            AdvancementProgress advancementprogress = p_136395_.getAdvancements().getOrStartProgress(p_136396_);
            if (advancementprogress.isDone()) {
               return false;
            } else {
               for(String s : advancementprogress.getRemainingCriteria()) {
                  p_136395_.getAdvancements().award(p_136396_, s);
               }

               return true;
            }
         }

         /**
          * Applies this action to the given criterion.
          * 
          * @return True if the player was affected.
          */
         protected boolean performCriterion(ServerPlayer p_136398_, Advancement p_136399_, String p_136400_) {
            return p_136398_.getAdvancements().award(p_136399_, p_136400_);
         }
      },
      REVOKE("revoke") {
         /**
          * Applies this action to the given advancement.
          * 
          * @return True if the player was affected.
          */
         protected boolean perform(ServerPlayer p_136406_, Advancement p_136407_) {
            AdvancementProgress advancementprogress = p_136406_.getAdvancements().getOrStartProgress(p_136407_);
            if (!advancementprogress.hasProgress()) {
               return false;
            } else {
               for(String s : advancementprogress.getCompletedCriteria()) {
                  p_136406_.getAdvancements().revoke(p_136407_, s);
               }

               return true;
            }
         }

         /**
          * Applies this action to the given criterion.
          * 
          * @return True if the player was affected.
          */
         protected boolean performCriterion(ServerPlayer p_136409_, Advancement p_136410_, String p_136411_) {
            return p_136409_.getAdvancements().revoke(p_136410_, p_136411_);
         }
      };

      private final String key;

      Action(String pKey) {
         this.key = "commands.advancement." + pKey;
      }

      /**
       * Applies this action to all the given advancements.
       * 
       * @return The number of players affected.
       */
      public int perform(ServerPlayer pPlayer, Iterable<Advancement> pAdvancements) {
         int i = 0;

         for(Advancement advancement : pAdvancements) {
            if (this.perform(pPlayer, advancement)) {
               ++i;
            }
         }

         return i;
      }

      /**
       * Applies this action to the given advancement.
       * 
       * @return True if the player was affected.
       */
      protected abstract boolean perform(ServerPlayer pPlayer, Advancement pAdvancement);

      /**
       * Applies this action to the given criterion.
       * 
       * @return True if the player was affected.
       */
      protected abstract boolean performCriterion(ServerPlayer pPlayer, Advancement pAdvancement, String pCriterionName);

      protected String getKey() {
         return this.key;
      }
   }

   static enum Mode {
      ONLY(false, false),
      THROUGH(true, true),
      FROM(false, true),
      UNTIL(true, false),
      EVERYTHING(true, true);

      final boolean parents;
      final boolean children;

      private Mode(boolean pParents, boolean pChildren) {
         this.parents = pParents;
         this.children = pChildren;
      }
   }
}