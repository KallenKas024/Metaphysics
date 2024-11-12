package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.OptionalInt;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
   public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (p_137719_, p_137720_) -> {
      ServerFunctionManager serverfunctionmanager = p_137719_.getSource().getServer().getFunctions();
      SharedSuggestionProvider.suggestResource(serverfunctionmanager.getTagNames(), p_137720_, "#");
      return SharedSuggestionProvider.suggestResource(serverfunctionmanager.getFunctionNames(), p_137720_);
   };

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("function").requires((p_137722_) -> {
         return p_137722_.hasPermission(2);
      }).then(Commands.argument("name", FunctionArgument.functions()).suggests(SUGGEST_FUNCTION).executes((p_137717_) -> {
         return runFunction(p_137717_.getSource(), FunctionArgument.getFunctions(p_137717_, "name"));
      })));
   }

   private static int runFunction(CommandSourceStack pSource, Collection<CommandFunction> pFunctions) {
      int i = 0;
      boolean flag = false;

      for(CommandFunction commandfunction : pFunctions) {
         MutableObject<OptionalInt> mutableobject = new MutableObject<>(OptionalInt.empty());
         int j = pSource.getServer().getFunctions().execute(commandfunction, pSource.withSuppressedOutput().withMaximumPermission(2).withReturnValueConsumer((p_280947_) -> {
            mutableobject.setValue(OptionalInt.of(p_280947_));
         }));
         OptionalInt optionalint = mutableobject.getValue();
         i += optionalint.orElse(j);
         flag |= optionalint.isPresent();
      }

      int k = i;
      if (pFunctions.size() == 1) {
         if (flag) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.function.success.single.result", k, pFunctions.iterator().next().getId());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.function.success.single", k, pFunctions.iterator().next().getId());
            }, true);
         }
      } else if (flag) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.function.success.multiple.result", pFunctions.size());
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.function.success.multiple", k, pFunctions.size());
         }, true);
      }

      return i;
   }
}