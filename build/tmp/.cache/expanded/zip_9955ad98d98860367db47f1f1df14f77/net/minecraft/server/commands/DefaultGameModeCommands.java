package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("defaultgamemode").requires((p_136929_) -> {
         return p_136929_.hasPermission(2);
      }).then(Commands.argument("gamemode", GameModeArgument.gameMode()).executes((p_258227_) -> {
         return setMode(p_258227_.getSource(), GameModeArgument.getGameMode(p_258227_, "gamemode"));
      })));
   }

   /**
    * Sets the {@link net.minecraft.world.level.GameType} of the player who ran the command.
    */
   private static int setMode(CommandSourceStack pCommandSource, GameType pGamemode) {
      int i = 0;
      MinecraftServer minecraftserver = pCommandSource.getServer();
      minecraftserver.setDefaultGameType(pGamemode);
      GameType gametype = minecraftserver.getForcedGameType();
      if (gametype != null) {
         for(ServerPlayer serverplayer : minecraftserver.getPlayerList().getPlayers()) {
            if (serverplayer.setGameMode(gametype)) {
               ++i;
            }
         }
      }

      pCommandSource.sendSuccess(() -> {
         return Component.translatable("commands.defaultgamemode.success", pGamemode.getLongDisplayName());
      }, true);
      return i;
   }
}