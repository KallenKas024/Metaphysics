package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;

public class TeamMsgCommand {
   private static final Style SUGGEST_STYLE = Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.type.team.hover"))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/teammsg "));
   private static final SimpleCommandExceptionType ERROR_NOT_ON_TEAM = new SimpleCommandExceptionType(Component.translatable("commands.teammsg.failed.noteam"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      LiteralCommandNode<CommandSourceStack> literalcommandnode = pDispatcher.register(Commands.literal("teammsg").then(Commands.argument("message", MessageArgument.message()).executes((p_248184_) -> {
         CommandSourceStack commandsourcestack = p_248184_.getSource();
         Entity entity = commandsourcestack.getEntityOrException();
         PlayerTeam playerteam = (PlayerTeam)entity.getTeam();
         if (playerteam == null) {
            throw ERROR_NOT_ON_TEAM.create();
         } else {
            List<ServerPlayer> list = commandsourcestack.getServer().getPlayerList().getPlayers().stream().filter((p_288679_) -> {
               return p_288679_ == entity || p_288679_.getTeam() == playerteam;
            }).toList();
            if (!list.isEmpty()) {
               MessageArgument.resolveChatMessage(p_248184_, "message", (p_248180_) -> {
                  sendMessage(commandsourcestack, entity, playerteam, list, p_248180_);
               });
            }

            return list.size();
         }
      })));
      pDispatcher.register(Commands.literal("tm").redirect(literalcommandnode));
   }

   private static void sendMessage(CommandSourceStack pSource, Entity pSender, PlayerTeam pTeam, List<ServerPlayer> pTeamMembers, PlayerChatMessage pChatMessage) {
      Component component = pTeam.getFormattedDisplayName().withStyle(SUGGEST_STYLE);
      ChatType.Bound chattype$bound = ChatType.bind(ChatType.TEAM_MSG_COMMAND_INCOMING, pSource).withTargetName(component);
      ChatType.Bound chattype$bound1 = ChatType.bind(ChatType.TEAM_MSG_COMMAND_OUTGOING, pSource).withTargetName(component);
      OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(pChatMessage);
      boolean flag = false;

      for(ServerPlayer serverplayer : pTeamMembers) {
         ChatType.Bound chattype$bound2 = serverplayer == pSender ? chattype$bound1 : chattype$bound;
         boolean flag1 = pSource.shouldFilterMessageTo(serverplayer);
         serverplayer.sendChatMessage(outgoingchatmessage, flag1, chattype$bound2);
         flag |= flag1 && pChatMessage.isFullyFiltered();
      }

      if (flag) {
         pSource.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
      }

   }
}