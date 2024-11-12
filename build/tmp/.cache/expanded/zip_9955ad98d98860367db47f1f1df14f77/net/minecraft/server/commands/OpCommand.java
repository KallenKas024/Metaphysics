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

public class OpCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(Component.translatable("commands.op.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("op").requires((p_138087_) -> {
         return p_138087_.hasPermission(3);
      }).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_138084_, p_138085_) -> {
         PlayerList playerlist = p_138084_.getSource().getServer().getPlayerList();
         return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((p_289286_) -> {
            return !playerlist.isOp(p_289286_.getGameProfile());
         }).map((p_289284_) -> {
            return p_289284_.getGameProfile().getName();
         }), p_138085_);
      }).executes((p_138082_) -> {
         return opPlayers(p_138082_.getSource(), GameProfileArgument.getGameProfiles(p_138082_, "targets"));
      })));
   }

   private static int opPlayers(CommandSourceStack pSource, Collection<GameProfile> pGameProfiles) throws CommandSyntaxException {
      PlayerList playerlist = pSource.getServer().getPlayerList();
      int i = 0;

      for(GameProfile gameprofile : pGameProfiles) {
         if (!playerlist.isOp(gameprofile)) {
            playerlist.op(gameprofile);
            ++i;
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.op.success", pGameProfiles.iterator().next().getName());
            }, true);
         }
      }

      if (i == 0) {
         throw ERROR_ALREADY_OP.create();
      } else {
         return i;
      }
   }
}