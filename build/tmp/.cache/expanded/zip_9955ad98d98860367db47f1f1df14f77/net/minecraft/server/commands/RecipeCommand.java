package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeCommand {
   private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.recipe.give.failed"));
   private static final SimpleCommandExceptionType ERROR_TAKE_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.recipe.take.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("recipe").requires((p_138205_) -> {
         return p_138205_.hasPermission(2);
      }).then(Commands.literal("give").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceLocationArgument.id()).suggests(SuggestionProviders.ALL_RECIPES).executes((p_138219_) -> {
         return giveRecipes(p_138219_.getSource(), EntityArgument.getPlayers(p_138219_, "targets"), Collections.singleton(ResourceLocationArgument.getRecipe(p_138219_, "recipe")));
      })).then(Commands.literal("*").executes((p_138217_) -> {
         return giveRecipes(p_138217_.getSource(), EntityArgument.getPlayers(p_138217_, "targets"), p_138217_.getSource().getServer().getRecipeManager().getRecipes());
      })))).then(Commands.literal("take").then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceLocationArgument.id()).suggests(SuggestionProviders.ALL_RECIPES).executes((p_138211_) -> {
         return takeRecipes(p_138211_.getSource(), EntityArgument.getPlayers(p_138211_, "targets"), Collections.singleton(ResourceLocationArgument.getRecipe(p_138211_, "recipe")));
      })).then(Commands.literal("*").executes((p_138203_) -> {
         return takeRecipes(p_138203_.getSource(), EntityArgument.getPlayers(p_138203_, "targets"), p_138203_.getSource().getServer().getRecipeManager().getRecipes());
      })))));
   }

   private static int giveRecipes(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, Collection<Recipe<?>> pRecipes) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayer serverplayer : pTargets) {
         i += serverplayer.awardRecipes(pRecipes);
      }

      if (i == 0) {
         throw ERROR_GIVE_FAILED.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.recipe.give.success.single", pRecipes.size(), pTargets.iterator().next().getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.recipe.give.success.multiple", pRecipes.size(), pTargets.size());
            }, true);
         }

         return i;
      }
   }

   private static int takeRecipes(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, Collection<Recipe<?>> pRecipes) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayer serverplayer : pTargets) {
         i += serverplayer.resetRecipes(pRecipes);
      }

      if (i == 0) {
         throw ERROR_TAKE_FAILED.create();
      } else {
         if (pTargets.size() == 1) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.recipe.take.success.single", pRecipes.size(), pTargets.iterator().next().getDisplayName());
            }, true);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.recipe.take.success.multiple", pRecipes.size(), pTargets.size());
            }, true);
         }

         return i;
      }
   }
}