package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SetPlayerIdleTimeoutCommand {
   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("setidletimeout").requires((p_138639_) -> {
         return p_138639_.hasPermission(3);
      }).then(Commands.argument("minutes", IntegerArgumentType.integer(0)).executes((p_138637_) -> {
         return setIdleTimeout(p_138637_.getSource(), IntegerArgumentType.getInteger(p_138637_, "minutes"));
      })));
   }

   private static int setIdleTimeout(CommandSourceStack pSource, int pIdleTimeout) {
      pSource.getServer().setPlayerIdleTimeout(pIdleTimeout);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.setidletimeout.success", pIdleTimeout);
      }, true);
      return pIdleTimeout;
   }
}