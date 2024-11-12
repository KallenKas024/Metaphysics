package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

public class GameRuleCommand {
   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      final LiteralArgumentBuilder<CommandSourceStack> literalargumentbuilder = Commands.literal("gamerule").requires((p_137750_) -> {
         return p_137750_.hasPermission(2);
      });
      GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
         public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> p_137764_, GameRules.Type<T> p_137765_) {
            literalargumentbuilder.then(Commands.literal(p_137764_.getId()).executes((p_137771_) -> {
               return GameRuleCommand.queryRule(p_137771_.getSource(), p_137764_);
            }).then(p_137765_.createArgument("value").executes((p_137768_) -> {
               return GameRuleCommand.setRule(p_137768_, p_137764_);
            })));
         }
      });
      pDispatcher.register(literalargumentbuilder);
   }

   static <T extends GameRules.Value<T>> int setRule(CommandContext<CommandSourceStack> pSource, GameRules.Key<T> pGameRule) {
      CommandSourceStack commandsourcestack = pSource.getSource();
      T t = commandsourcestack.getServer().getGameRules().getRule(pGameRule);
      t.setFromArgument(pSource, "value");
      commandsourcestack.sendSuccess(() -> {
         return Component.translatable("commands.gamerule.set", pGameRule.getId(), t.toString());
      }, true);
      return t.getCommandResult();
   }

   static <T extends GameRules.Value<T>> int queryRule(CommandSourceStack pSource, GameRules.Key<T> pGameRule) {
      T t = pSource.getServer().getGameRules().getRule(pGameRule);
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.gamerule.query", pGameRule.getId(), t.toString());
      }, false);
      return t.getCommandResult();
   }
}