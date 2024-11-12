package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SetSpawnCommand {
   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("spawnpoint").requires((p_138648_) -> {
         return p_138648_.hasPermission(2);
      }).executes((p_274828_) -> {
         return setSpawn(p_274828_.getSource(), Collections.singleton(p_274828_.getSource().getPlayerOrException()), BlockPos.containing(p_274828_.getSource().getPosition()), 0.0F);
      }).then(Commands.argument("targets", EntityArgument.players()).executes((p_274829_) -> {
         return setSpawn(p_274829_.getSource(), EntityArgument.getPlayers(p_274829_, "targets"), BlockPos.containing(p_274829_.getSource().getPosition()), 0.0F);
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_138655_) -> {
         return setSpawn(p_138655_.getSource(), EntityArgument.getPlayers(p_138655_, "targets"), BlockPosArgument.getSpawnablePos(p_138655_, "pos"), 0.0F);
      }).then(Commands.argument("angle", AngleArgument.angle()).executes((p_138646_) -> {
         return setSpawn(p_138646_.getSource(), EntityArgument.getPlayers(p_138646_, "targets"), BlockPosArgument.getSpawnablePos(p_138646_, "pos"), AngleArgument.getAngle(p_138646_, "angle"));
      })))));
   }

   private static int setSpawn(CommandSourceStack pSource, Collection<ServerPlayer> pTargets, BlockPos pPos, float pAngle) {
      ResourceKey<Level> resourcekey = pSource.getLevel().dimension();

      for(ServerPlayer serverplayer : pTargets) {
         serverplayer.setRespawnPosition(resourcekey, pPos, pAngle, true, false);
      }

      String s = resourcekey.location().toString();
      if (pTargets.size() == 1) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.spawnpoint.success.single", pPos.getX(), pPos.getY(), pPos.getZ(), pAngle, s, pTargets.iterator().next().getDisplayName());
         }, true);
      } else {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.spawnpoint.success.multiple", pPos.getX(), pPos.getY(), pPos.getZ(), pAngle, s, pTargets.size());
         }, true);
      }

      return pTargets.size();
   }
}