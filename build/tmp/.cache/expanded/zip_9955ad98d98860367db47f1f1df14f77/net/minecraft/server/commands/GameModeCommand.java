package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class GameModeCommand {
   public static final int PERMISSION_LEVEL = 2;

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("gamemode").requires((p_137736_) -> {
         return p_137736_.hasPermission(2);
      }).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes((p_258228_) -> {
         return setMode(p_258228_, Collections.singleton(p_258228_.getSource().getPlayerOrException()), GameModeArgument.getGameMode(p_258228_, "gamemode"));
      }).then(Commands.argument("target", EntityArgument.players()).executes((p_258229_) -> {
         return setMode(p_258229_, EntityArgument.getPlayers(p_258229_, "target"), GameModeArgument.getGameMode(p_258229_, "gamemode"));
      }))));
   }

   private static void logGamemodeChange(CommandSourceStack pSource, ServerPlayer pPlayer, GameType pGameType) {
      Component component = Component.translatable("gameMode." + pGameType.getName());
      if (pSource.getEntity() == pPlayer) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.gamemode.success.self", component);
         }, true);
      } else {
         if (pSource.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            pPlayer.sendSystemMessage(Component.translatable("gameMode.changed", component));
         }

         pSource.sendSuccess(() -> {
            return Component.translatable("commands.gamemode.success.other", pPlayer.getDisplayName(), component);
         }, true);
      }

   }

   private static int setMode(CommandContext<CommandSourceStack> pSource, Collection<ServerPlayer> pPlayers, GameType pGameType) {
      int i = 0;

      for(ServerPlayer serverplayer : pPlayers) {
         if (serverplayer.setGameMode(pGameType)) {
            logGamemodeChange(pSource.getSource(), serverplayer, pGameType);
            ++i;
         }
      }

      return i;
   }
}