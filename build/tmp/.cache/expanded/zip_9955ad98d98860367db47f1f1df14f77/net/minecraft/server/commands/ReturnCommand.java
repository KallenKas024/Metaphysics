package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReturnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("return").requires((p_281281_) -> {
         return p_281281_.hasPermission(2);
      }).then(Commands.argument("value", IntegerArgumentType.integer()).executes((p_281464_) -> {
         return setReturn(p_281464_.getSource(), IntegerArgumentType.getInteger(p_281464_, "value"));
      })));
   }

   private static int setReturn(CommandSourceStack pSource, int pValue) {
      pSource.getReturnValueConsumer().accept(pValue);
      return pValue;
   }
}