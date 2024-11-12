package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(Component.translatable("commands.experience.set.points.invalid"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = pDispatcher.register(Commands.literal("experience").requires((p_137324_) -> {
         return p_137324_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer()).executes((p_137341_) -> {
         return addExperience(p_137341_.getSource(), EntityArgument.getPlayers(p_137341_, "targets"), IntegerArgumentType.getInteger(p_137341_, "amount"), ExperienceCommand.Type.POINTS);
      }).then(Commands.literal("points").executes((p_137339_) -> {
         return addExperience(p_137339_.getSource(), EntityArgument.getPlayers(p_137339_, "targets"), IntegerArgumentType.getInteger(p_137339_, "amount"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("levels").executes((p_137337_) -> {
         return addExperience(p_137337_.getSource(), EntityArgument.getPlayers(p_137337_, "targets"), IntegerArgumentType.getInteger(p_137337_, "amount"), ExperienceCommand.Type.LEVELS);
      }))))).then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("amount", IntegerArgumentType.integer(0)).executes((p_137335_) -> {
         return setExperience(p_137335_.getSource(), EntityArgument.getPlayers(p_137335_, "targets"), IntegerArgumentType.getInteger(p_137335_, "amount"), ExperienceCommand.Type.POINTS);
      }).then(Commands.literal("points").executes((p_137333_) -> {
         return setExperience(p_137333_.getSource(), EntityArgument.getPlayers(p_137333_, "targets"), IntegerArgumentType.getInteger(p_137333_, "amount"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("levels").executes((p_137331_) -> {
         return setExperience(p_137331_.getSource(), EntityArgument.getPlayers(p_137331_, "targets"), IntegerArgumentType.getInteger(p_137331_, "amount"), ExperienceCommand.Type.LEVELS);
      }))))).then(Commands.literal("query").then(Commands.argument("targets", EntityArgument.player()).then(Commands.literal("points").executes((p_137322_) -> {
         return queryExperience(p_137322_.getSource(), EntityArgument.getPlayer(p_137322_, "targets"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("levels").executes((p_137309_) -> {
         return queryExperience(p_137309_.getSource(), EntityArgument.getPlayer(p_137309_, "targets"), ExperienceCommand.Type.LEVELS);
      })))));
      pDispatcher.register(Commands.literal("xp").requires((p_137311_) -> {
         return p_137311_.hasPermission(2);
      }).redirect(literalcommandnode));
   }

   private static int queryExperience(CommandSourceStack pSource, ServerPlayer pPlayer, ExperienceCommand.Type pType) {
      int i = pType.query.applyAsInt(pPlayer);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.experience.query." + pType.name, pPlayer.getDisplayName(), i);
      }, false);
      return i;
   }

   private static int addExperience(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pAmount, ExperienceCommand.Type pType) {
      for(ServerPlayer serverplayer : pTargets) {
         pType.add.accept(serverplayer, pAmount);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.experience.add." + pType.name + ".success.single", pAmount, pTargets.iterator().next().getDisplayName());
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.experience.add." + pType.name + ".success.multiple", pAmount, pTargets.size());
         }, true);
      }

      return pTargets.size();
   }

   private static int setExperience(CommandSourceStack pSource, Collection<? extends ServerPlayer> pTargets, int pAmount, ExperienceCommand.Type pType) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayer serverplayer : pTargets) {
         if (pType.set.test(serverplayer, pAmount)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_SET_POINTS_INVALID.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.experience.set." + pType.name + ".success.single", pAmount, pTargets.iterator().next().getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.experience.set." + pType.name + ".success.multiple", pAmount, pTargets.size());
            }, true);
         }

         return pTargets.size();
      }
   }

   static enum Type {
      POINTS("points", Player::giveExperiencePoints, (p_289274_, p_289275_) -> {
         if (p_289275_ >= p_289274_.getXpNeededForNextLevel()) {
            return false;
         } else {
            p_289274_.setExperiencePoints(p_289275_);
            return true;
         }
      }, (p_289273_) -> {
         return Mth.floor(p_289273_.experienceProgress * (float)p_289273_.getXpNeededForNextLevel());
      }),
      LEVELS("levels", ServerPlayer::giveExperienceLevels, (p_137360_, p_137361_) -> {
         p_137360_.setExperienceLevels(p_137361_);
         return true;
      }, (p_287335_) -> {
         return p_287335_.experienceLevel;
      });

      public final BiConsumer<ServerPlayer, Integer> add;
      public final BiPredicate<ServerPlayer, Integer> set;
      public final String name;
      final ToIntFunction<ServerPlayer> query;

      private Type(String pName, BiConsumer<ServerPlayer, Integer> pAdd, BiPredicate<ServerPlayer, Integer> pSet, ToIntFunction<ServerPlayer> pQuery) {
         this.add = pAdd;
         this.name = pName;
         this.set = pSet;
         this.query = pQuery;
      }
   }
}