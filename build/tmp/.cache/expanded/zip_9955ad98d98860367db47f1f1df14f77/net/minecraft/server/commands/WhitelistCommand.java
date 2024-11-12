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
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.server.players.UserWhiteListEntry;

public class WhitelistCommand {
   private static final SimpleCommandExceptionType ERROR_ALREADY_ENABLED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.alreadyOn"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_DISABLED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.alreadyOff"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_WHITELISTED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.add.failed"));
   private static final SimpleCommandExceptionType ERROR_NOT_WHITELISTED = new SimpleCommandExceptionType(Component.translatable("commands.whitelist.remove.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("whitelist").requires((p_139234_) -> {
         return p_139234_.hasPermission(3);
      }).then(Commands.literal("on").executes((p_139236_) -> {
         return enableWhitelist(p_139236_.getSource());
      })).then(Commands.literal("off").executes((p_139232_) -> {
         return disableWhitelist(p_139232_.getSource());
      })).then(Commands.literal("list").executes((p_139228_) -> {
         return showList(p_139228_.getSource());
      })).then(Commands.literal("add").then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_139216_, p_139217_) -> {
         PlayerList playerlist = p_139216_.getSource().getServer().getPlayerList();
         return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((p_289303_) -> {
            return !playerlist.getWhiteList().isWhiteListed(p_289303_.getGameProfile());
         }).map((p_289304_) -> {
            return p_289304_.getGameProfile().getName();
         }), p_139217_);
      }).executes((p_139224_) -> {
         return addPlayers(p_139224_.getSource(), GameProfileArgument.getGameProfiles(p_139224_, "targets"));
      }))).then(Commands.literal("remove").then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_139206_, p_139207_) -> {
         return SharedSuggestionProvider.suggest(p_139206_.getSource().getServer().getPlayerList().getWhiteListNames(), p_139207_);
      }).executes((p_139214_) -> {
         return removePlayers(p_139214_.getSource(), GameProfileArgument.getGameProfiles(p_139214_, "targets"));
      }))).then(Commands.literal("reload").executes((p_139204_) -> {
         return reload(p_139204_.getSource());
      })));
   }

   private static int reload(CommandSourceStack pSource) {
      pSource.getServer().getPlayerList().reloadWhiteList();
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.whitelist.reloaded");
      }, true);
      pSource.getServer().kickUnlistedPlayers(pSource);
      return 1;
   }

   private static int addPlayers(CommandSourceStack pSource, Collection<GameProfile> pPlayers) throws CommandSyntaxException {
      UserWhiteList userwhitelist = pSource.getServer().getPlayerList().getWhiteList();
      int i = 0;

      for(GameProfile gameprofile : pPlayers) {
         if (!userwhitelist.isWhiteListed(gameprofile)) {
            UserWhiteListEntry userwhitelistentry = new UserWhiteListEntry(gameprofile);
            userwhitelist.add(userwhitelistentry);
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.whitelist.add.success", ComponentUtils.getDisplayName(gameprofile));
            }, true);
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_ALREADY_WHITELISTED.create();
      } else {
         return i;
      }
   }

   private static int removePlayers(CommandSourceStack pSource, Collection<GameProfile> pPlayers) throws CommandSyntaxException {
      UserWhiteList userwhitelist = pSource.getServer().getPlayerList().getWhiteList();
      int i = 0;

      for(GameProfile gameprofile : pPlayers) {
         if (userwhitelist.isWhiteListed(gameprofile)) {
            UserWhiteListEntry userwhitelistentry = new UserWhiteListEntry(gameprofile);
            userwhitelist.remove(userwhitelistentry);
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.whitelist.remove.success", ComponentUtils.getDisplayName(gameprofile));
            }, true);
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_NOT_WHITELISTED.create();
      } else {
         pSource.getServer().kickUnlistedPlayers(pSource);
         return i;
      }
   }

   private static int enableWhitelist(CommandSourceStack pSource) throws CommandSyntaxException {
      PlayerList playerlist = pSource.getServer().getPlayerList();
      if (playerlist.isUsingWhitelist()) {
         throw ERROR_ALREADY_ENABLED.create();
      } else {
         playerlist.setUsingWhiteList(true);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.whitelist.enabled");
         }, true);
         pSource.getServer().kickUnlistedPlayers(pSource);
         return 1;
      }
   }

   private static int disableWhitelist(CommandSourceStack pSource) throws CommandSyntaxException {
      PlayerList playerlist = pSource.getServer().getPlayerList();
      if (!playerlist.isUsingWhitelist()) {
         throw ERROR_ALREADY_DISABLED.create();
      } else {
         playerlist.setUsingWhiteList(false);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.whitelist.disabled");
         }, true);
         return 1;
      }
   }

   private static int showList(CommandSourceStack pSource) {
      String[] astring = pSource.getServer().getPlayerList().getWhiteListNames();
      if (astring.length == 0) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.whitelist.none");
         }, false);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.whitelist.list", astring.length, String.join(", ", astring));
         }, false);
      }

      return astring.length;
   }
}