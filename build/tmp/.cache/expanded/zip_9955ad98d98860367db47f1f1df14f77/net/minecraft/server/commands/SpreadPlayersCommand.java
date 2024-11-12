package net.minecraft.server.commands;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic4CommandExceptionType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.scores.Team;

public class SpreadPlayersCommand {
   private static final int MAX_ITERATION_COUNT = 10000;
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_TEAMS = new Dynamic4CommandExceptionType((p_138745_, p_138746_, p_138747_, p_138748_) -> {
      return Component.translatable("commands.spreadplayers.failed.teams", p_138745_, p_138746_, p_138747_, p_138748_);
   });
   private static final Dynamic4CommandExceptionType ERROR_FAILED_TO_SPREAD_ENTITIES = new Dynamic4CommandExceptionType((p_138723_, p_138724_, p_138725_, p_138726_) -> {
      return Component.translatable("commands.spreadplayers.failed.entities", p_138723_, p_138724_, p_138725_, p_138726_);
   });
   private static final Dynamic2CommandExceptionType ERROR_INVALID_MAX_HEIGHT = new Dynamic2CommandExceptionType((p_201854_, p_201855_) -> {
      return Component.translatable("commands.spreadplayers.failed.invalid.height", p_201854_, p_201855_);
   });

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("spreadplayers").requires((p_201852_) -> {
         return p_201852_.hasPermission(2);
      }).then(Commands.argument("center", Vec2Argument.vec2()).then(Commands.argument("spreadDistance", FloatArgumentType.floatArg(0.0F)).then(Commands.argument("maxRange", FloatArgumentType.floatArg(1.0F)).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes((p_288627_) -> {
         return spreadPlayers(p_288627_.getSource(), Vec2Argument.getVec2(p_288627_, "center"), FloatArgumentType.getFloat(p_288627_, "spreadDistance"), FloatArgumentType.getFloat(p_288627_, "maxRange"), p_288627_.getSource().getLevel().getMaxBuildHeight(), BoolArgumentType.getBool(p_288627_, "respectTeams"), EntityArgument.getEntities(p_288627_, "targets"));
      }))).then(Commands.literal("under").then(Commands.argument("maxHeight", IntegerArgumentType.integer()).then(Commands.argument("respectTeams", BoolArgumentType.bool()).then(Commands.argument("targets", EntityArgument.entities()).executes((p_201850_) -> {
         return spreadPlayers(p_201850_.getSource(), Vec2Argument.getVec2(p_201850_, "center"), FloatArgumentType.getFloat(p_201850_, "spreadDistance"), FloatArgumentType.getFloat(p_201850_, "maxRange"), IntegerArgumentType.getInteger(p_201850_, "maxHeight"), BoolArgumentType.getBool(p_201850_, "respectTeams"), EntityArgument.getEntities(p_201850_, "targets"));
      })))))))));
   }

   private static int spreadPlayers(CommandSourceStack pSource, Vec2 pCenter, float pSpreadDistance, float pMaxRange, int pMaxHeight, boolean pRespectTeams, Collection<? extends Entity> pTargets) throws CommandSyntaxException {
      ServerLevel serverlevel = pSource.getLevel();
      int i = serverlevel.getMinBuildHeight();
      if (pMaxHeight < i) {
         throw ERROR_INVALID_MAX_HEIGHT.create(pMaxHeight, i);
      } else {
         RandomSource randomsource = RandomSource.create();
         double d0 = (double)(pCenter.x - pMaxRange);
         double d1 = (double)(pCenter.y - pMaxRange);
         double d2 = (double)(pCenter.x + pMaxRange);
         double d3 = (double)(pCenter.y + pMaxRange);
         SpreadPlayersCommand.Position[] aspreadplayerscommand$position = createInitialPositions(randomsource, pRespectTeams ? getNumberOfTeams(pTargets) : pTargets.size(), d0, d1, d2, d3);
         spreadPositions(pCenter, (double)pSpreadDistance, serverlevel, randomsource, d0, d1, d2, d3, pMaxHeight, aspreadplayerscommand$position, pRespectTeams);
         double d4 = setPlayerPositions(pTargets, serverlevel, aspreadplayerscommand$position, pMaxHeight, pRespectTeams);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.spreadplayers.success." + (pRespectTeams ? "teams" : "entities"), aspreadplayerscommand$position.length, pCenter.x, pCenter.y, String.format(Locale.ROOT, "%.2f", d4));
         }, true);
         return aspreadplayerscommand$position.length;
      }
   }

   /**
    * Gets the number of unique teams for the given list of entities.
    */
   private static int getNumberOfTeams(Collection<? extends Entity> pEntities) {
      Set<Team> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         if (entity instanceof Player) {
            set.add(entity.getTeam());
         } else {
            set.add((Team)null);
         }
      }

      return set.size();
   }

   private static void spreadPositions(Vec2 pCenter, double pSpreadDistance, ServerLevel pLevel, RandomSource pRandom, double pMinX, double pMinZ, double pMaxX, double pMaxZ, int pMaxHeight, SpreadPlayersCommand.Position[] pPositions, boolean pRespectTeams) throws CommandSyntaxException {
      boolean flag = true;
      double d0 = (double)Float.MAX_VALUE;

      int i;
      for(i = 0; i < 10000 && flag; ++i) {
         flag = false;
         d0 = (double)Float.MAX_VALUE;

         for(int j = 0; j < pPositions.length; ++j) {
            SpreadPlayersCommand.Position spreadplayerscommand$position = pPositions[j];
            int k = 0;
            SpreadPlayersCommand.Position spreadplayerscommand$position1 = new SpreadPlayersCommand.Position();

            for(int l = 0; l < pPositions.length; ++l) {
               if (j != l) {
                  SpreadPlayersCommand.Position spreadplayerscommand$position2 = pPositions[l];
                  double d1 = spreadplayerscommand$position.dist(spreadplayerscommand$position2);
                  d0 = Math.min(d1, d0);
                  if (d1 < pSpreadDistance) {
                     ++k;
                     spreadplayerscommand$position1.x += spreadplayerscommand$position2.x - spreadplayerscommand$position.x;
                     spreadplayerscommand$position1.z += spreadplayerscommand$position2.z - spreadplayerscommand$position.z;
                  }
               }
            }

            if (k > 0) {
               spreadplayerscommand$position1.x /= (double)k;
               spreadplayerscommand$position1.z /= (double)k;
               double d2 = spreadplayerscommand$position1.getLength();
               if (d2 > 0.0D) {
                  spreadplayerscommand$position1.normalize();
                  spreadplayerscommand$position.moveAway(spreadplayerscommand$position1);
               } else {
                  spreadplayerscommand$position.randomize(pRandom, pMinX, pMinZ, pMaxX, pMaxZ);
               }

               flag = true;
            }

            if (spreadplayerscommand$position.clamp(pMinX, pMinZ, pMaxX, pMaxZ)) {
               flag = true;
            }
         }

         if (!flag) {
            for(SpreadPlayersCommand.Position spreadplayerscommand$position3 : pPositions) {
               if (!spreadplayerscommand$position3.isSafe(pLevel, pMaxHeight)) {
                  spreadplayerscommand$position3.randomize(pRandom, pMinX, pMinZ, pMaxX, pMaxZ);
                  flag = true;
               }
            }
         }
      }

      if (d0 == (double)Float.MAX_VALUE) {
         d0 = 0.0D;
      }

      if (i >= 10000) {
         if (pRespectTeams) {
            throw ERROR_FAILED_TO_SPREAD_TEAMS.create(pPositions.length, pCenter.x, pCenter.y, String.format(Locale.ROOT, "%.2f", d0));
         } else {
            throw ERROR_FAILED_TO_SPREAD_ENTITIES.create(pPositions.length, pCenter.x, pCenter.y, String.format(Locale.ROOT, "%.2f", d0));
         }
      }
   }

   private static double setPlayerPositions(Collection<? extends Entity> pTargets, ServerLevel pLevel, SpreadPlayersCommand.Position[] pPositions, int pMaxHeight, boolean pRespectTeams) {
      double d0 = 0.0D;
      int i = 0;
      Map<Team, SpreadPlayersCommand.Position> map = Maps.newHashMap();

      for(Entity entity : pTargets) {
         SpreadPlayersCommand.Position spreadplayerscommand$position;
         if (pRespectTeams) {
            Team team = entity instanceof Player ? entity.getTeam() : null;
            if (!map.containsKey(team)) {
               map.put(team, pPositions[i++]);
            }

            spreadplayerscommand$position = map.get(team);
         } else {
            spreadplayerscommand$position = pPositions[i++];
         }

         net.minecraftforge.event.entity.EntityTeleportEvent.SpreadPlayersCommand event = net.minecraftforge.event.ForgeEventFactory.onEntityTeleportSpreadPlayersCommand(entity, (double)Mth.floor(spreadplayerscommand$position.x) + 0.5D, (double)spreadplayerscommand$position.getSpawnY(pLevel, pMaxHeight), (double)Mth.floor(spreadplayerscommand$position.z) + 0.5D);
         if (!event.isCanceled()) entity.teleportToWithTicket(event.getTargetX(), event.getTargetY(), event.getTargetZ());
         double d2 = Double.MAX_VALUE;

         for(SpreadPlayersCommand.Position spreadplayerscommand$position1 : pPositions) {
            if (spreadplayerscommand$position != spreadplayerscommand$position1) {
               double d1 = spreadplayerscommand$position.dist(spreadplayerscommand$position1);
               d2 = Math.min(d1, d2);
            }
         }

         d0 += d2;
      }

      return pTargets.size() < 2 ? 0.0D : d0 / (double)pTargets.size();
   }

   private static SpreadPlayersCommand.Position[] createInitialPositions(RandomSource pRandom, int pCount, double pMinX, double pMinZ, double pMaxX, double pMaxZ) {
      SpreadPlayersCommand.Position[] aspreadplayerscommand$position = new SpreadPlayersCommand.Position[pCount];

      for(int i = 0; i < aspreadplayerscommand$position.length; ++i) {
         SpreadPlayersCommand.Position spreadplayerscommand$position = new SpreadPlayersCommand.Position();
         spreadplayerscommand$position.randomize(pRandom, pMinX, pMinZ, pMaxX, pMaxZ);
         aspreadplayerscommand$position[i] = spreadplayerscommand$position;
      }

      return aspreadplayerscommand$position;
   }

   static class Position {
      double x;
      double z;

      double dist(SpreadPlayersCommand.Position pOther) {
         double d0 = this.x - pOther.x;
         double d1 = this.z - pOther.z;
         return Math.sqrt(d0 * d0 + d1 * d1);
      }

      void normalize() {
         double d0 = this.getLength();
         this.x /= d0;
         this.z /= d0;
      }

      double getLength() {
         return Math.sqrt(this.x * this.x + this.z * this.z);
      }

      public void moveAway(SpreadPlayersCommand.Position pOther) {
         this.x -= pOther.x;
         this.z -= pOther.z;
      }

      public boolean clamp(double pMinX, double pMinZ, double pMaxX, double pMaxZ) {
         boolean flag = false;
         if (this.x < pMinX) {
            this.x = pMinX;
            flag = true;
         } else if (this.x > pMaxX) {
            this.x = pMaxX;
            flag = true;
         }

         if (this.z < pMinZ) {
            this.z = pMinZ;
            flag = true;
         } else if (this.z > pMaxZ) {
            this.z = pMaxZ;
            flag = true;
         }

         return flag;
      }

      public int getSpawnY(BlockGetter pLevel, int pY) {
         BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(this.x, (double)(pY + 1), this.z);
         boolean flag = pLevel.getBlockState(blockpos$mutableblockpos).isAir();
         blockpos$mutableblockpos.move(Direction.DOWN);

         boolean flag2;
         for(boolean flag1 = pLevel.getBlockState(blockpos$mutableblockpos).isAir(); blockpos$mutableblockpos.getY() > pLevel.getMinBuildHeight(); flag1 = flag2) {
            blockpos$mutableblockpos.move(Direction.DOWN);
            flag2 = pLevel.getBlockState(blockpos$mutableblockpos).isAir();
            if (!flag2 && flag1 && flag) {
               return blockpos$mutableblockpos.getY() + 1;
            }

            flag = flag1;
         }

         return pY + 1;
      }

      public boolean isSafe(BlockGetter pLevel, int pY) {
         BlockPos blockpos = BlockPos.containing(this.x, (double)(this.getSpawnY(pLevel, pY) - 1), this.z);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         return blockpos.getY() < pY && !blockstate.liquid() && !blockstate.is(BlockTags.FIRE);
      }

      public void randomize(RandomSource pRandom, double pMinX, double pMinZ, double pMaxX, double pMaxZ) {
         this.x = Mth.nextDouble(pRandom, pMinX, pMaxX);
         this.z = Mth.nextDouble(pRandom, pMinZ, pMaxZ);
      }
   }
}
