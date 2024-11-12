package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;

public class DeOpCommands {
   private static final SimpleCommandExceptionType ERROR_NOT_OP = new SimpleCommandExceptionType(Component.translatable("commands.deop.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("deop").requires((p_136896_) -> {
         return p_136896_.hasPermission(3);
      }).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_136893_, p_136894_) -> {
         return SharedSuggestionProvider.suggest(p_136893_.getSource().getServer().getPlayerList().getOpNames(), p_136894_);
      }).executes((p_136891_) -> {
         return deopPlayers(p_136891_.getSource(), GameProfileArgument.getGameProfiles(p_136891_, "targets"));
      })));
   }

   private static int deopPlayers(CommandSourceStack pSource, Collection<GameProfile> pPlayers) throws CommandSyntaxException {
      PlayerList playerlist = pSource.getServer().getPlayerList();
      int i = 0;

      for(GameProfile gameprofile : pPlayers) {
         if (playerlist.isOp(gameprofile)) {
            playerlist.deop(gameprofile);
            ++i;
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.deop.success", pPlayers.iterator().next().getName());
            }, true);
         }
      }

      if (i == 0) {
         throw ERROR_NOT_OP.create();
      } else {
         pSource.getServer().kickUnlistedPlayers(pSource);
         return i;
      }
   }
}