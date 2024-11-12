package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
   private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType(Component.translatable("commands.banip.invalid"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(Component.translatable("commands.banip.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("ban-ip").requires((p_136532_) -> {
         return p_136532_.hasPermission(3);
      }).then(Commands.argument("target", StringArgumentType.word()).executes((p_136538_) -> {
         return banIpOrName(p_136538_.getSource(), StringArgumentType.getString(p_136538_, "target"), (Component)null);
      }).then(Commands.argument("reason", MessageArgument.message()).executes((p_136530_) -> {
         return banIpOrName(p_136530_.getSource(), StringArgumentType.getString(p_136530_, "target"), MessageArgument.getMessage(p_136530_, "reason"));
      }))));
   }

   private static int banIpOrName(CommandSourceStack pSource, String pUsername, @Nullable Component pReason) throws CommandSyntaxException {
      if (InetAddresses.isInetAddress(pUsername)) {
         return banIp(pSource, pUsername, pReason);
      } else {
         ServerPlayer serverplayer = pSource.getServer().getPlayerList().getPlayerByName(pUsername);
         if (serverplayer != null) {
            return banIp(pSource, serverplayer.getIpAddress(), pReason);
         } else {
            throw ERROR_INVALID_IP.create();
         }
      }
   }

   private static int banIp(CommandSourceStack pSource, String pIp, @Nullable Component pReason) throws CommandSyntaxException {
      IpBanList ipbanlist = pSource.getServer().getPlayerList().getIpBans();
      if (ipbanlist.isBanned(pIp)) {
         throw ERROR_ALREADY_BANNED.create();
      } else {
         List<ServerPlayer> list = pSource.getServer().getPlayerList().getPlayersWithAddress(pIp);
         IpBanListEntry ipbanlistentry = new IpBanListEntry(pIp, (Date)null, pSource.getTextName(), (Date)null, pReason == null ? null : pReason.getString());
         ipbanlist.add(ipbanlistentry);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.banip.success", pIp, ipbanlistentry.getReason());
         }, true);
         if (!list.isEmpty()) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list));
            }, true);
         }

         for(ServerPlayer serverplayer : list) {
            serverplayer.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
         }

         return list.size();
      }
   }
}