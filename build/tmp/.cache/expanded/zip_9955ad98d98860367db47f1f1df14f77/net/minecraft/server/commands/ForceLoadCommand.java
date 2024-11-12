package net.minecraft.server.commands;

import com.google.common.base.Joiner;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ForceLoadCommand {
   private static final int MAX_CHUNK_LIMIT = 256;
   private static final Dynamic2CommandExceptionType ERROR_TOO_MANY_CHUNKS = new Dynamic2CommandExceptionType((p_137698_, p_137699_) -> {
      return Component.translatable("commands.forceload.toobig", p_137698_, p_137699_);
   });
   private static final Dynamic2CommandExceptionType ERROR_NOT_TICKING = new Dynamic2CommandExceptionType((p_137691_, p_137692_) -> {
      return Component.translatable("commands.forceload.query.failure", p_137691_, p_137692_);
   });
   private static final SimpleCommandExceptionType ERROR_ALL_ADDED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.added.failure"));
   private static final SimpleCommandExceptionType ERROR_NONE_REMOVED = new SimpleCommandExceptionType(Component.translatable("commands.forceload.removed.failure"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("forceload").requires((p_137703_) -> {
         return p_137703_.hasPermission(2);
      }).then(Commands.literal("add").then(Commands.argument("from", ColumnPosArgument.columnPos()).executes((p_137711_) -> {
         return changeForceLoad(p_137711_.getSource(), ColumnPosArgument.getColumnPos(p_137711_, "from"), ColumnPosArgument.getColumnPos(p_137711_, "from"), true);
      }).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes((p_137709_) -> {
         return changeForceLoad(p_137709_.getSource(), ColumnPosArgument.getColumnPos(p_137709_, "from"), ColumnPosArgument.getColumnPos(p_137709_, "to"), true);
      })))).then(Commands.literal("remove").then(Commands.argument("from", ColumnPosArgument.columnPos()).executes((p_137707_) -> {
         return changeForceLoad(p_137707_.getSource(), ColumnPosArgument.getColumnPos(p_137707_, "from"), ColumnPosArgument.getColumnPos(p_137707_, "from"), false);
      }).then(Commands.argument("to", ColumnPosArgument.columnPos()).executes((p_137705_) -> {
         return changeForceLoad(p_137705_.getSource(), ColumnPosArgument.getColumnPos(p_137705_, "from"), ColumnPosArgument.getColumnPos(p_137705_, "to"), false);
      }))).then(Commands.literal("all").executes((p_137701_) -> {
         return removeAll(p_137701_.getSource());
      }))).then(Commands.literal("query").executes((p_137694_) -> {
         return listForceLoad(p_137694_.getSource());
      }).then(Commands.argument("pos", ColumnPosArgument.columnPos()).executes((p_137679_) -> {
         return queryForceLoad(p_137679_.getSource(), ColumnPosArgument.getColumnPos(p_137679_, "pos"));
      }))));
   }

   private static int queryForceLoad(CommandSourceStack pSource, ColumnPos pPos) throws CommandSyntaxException {
      ChunkPos chunkpos = pPos.toChunkPos();
      ServerLevel serverlevel = pSource.getLevel();
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      boolean flag = serverlevel.getForcedChunks().contains(chunkpos.toLong());
      if (flag) {
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.forceload.query.success", chunkpos, resourcekey.location());
         }, false);
         return 1;
      } else {
         throw ERROR_NOT_TICKING.create(chunkpos, resourcekey.location());
      }
   }

   private static int listForceLoad(CommandSourceStack pSource) {
      ServerLevel serverlevel = pSource.getLevel();
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      LongSet longset = serverlevel.getForcedChunks();
      int i = longset.size();
      if (i > 0) {
         String s = Joiner.on(", ").join(longset.stream().sorted().map(ChunkPos::new).map(ChunkPos::toString).iterator());
         if (i == 1) {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.forceload.list.single", resourcekey.location(), s);
            }, false);
         } else {
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.forceload.list.multiple", i, resourcekey.location(), s);
            }, false);
         }
      } else {
         pSource.sendFailure(Component.translatable("commands.forceload.added.none", resourcekey.location()));
      }

      return i;
   }

   private static int removeAll(CommandSourceStack pSource) {
      ServerLevel serverlevel = pSource.getLevel();
      ResourceKey<Level> resourcekey = serverlevel.dimension();
      LongSet longset = serverlevel.getForcedChunks();
      longset.forEach((p_137675_) -> {
         serverlevel.setChunkForced(ChunkPos.getX(p_137675_), ChunkPos.getZ(p_137675_), false);
      });
      pSource.sendSuccess(() -> {
         return Component.translatable("commands.forceload.removed.all", resourcekey.location());
      }, true);
      return 0;
   }

   private static int changeForceLoad(CommandSourceStack pSource, ColumnPos pFrom, ColumnPos pTo, boolean pAdd) throws CommandSyntaxException {
      int i = Math.min(pFrom.x(), pTo.x());
      int j = Math.min(pFrom.z(), pTo.z());
      int k = Math.max(pFrom.x(), pTo.x());
      int l = Math.max(pFrom.z(), pTo.z());
      if (i >= -30000000 && j >= -30000000 && k < 30000000 && l < 30000000) {
         int i1 = SectionPos.blockToSectionCoord(i);
         int j1 = SectionPos.blockToSectionCoord(j);
         int k1 = SectionPos.blockToSectionCoord(k);
         int l1 = SectionPos.blockToSectionCoord(l);
         long i2 = ((long)(k1 - i1) + 1L) * ((long)(l1 - j1) + 1L);
         if (i2 > 256L) {
            throw ERROR_TOO_MANY_CHUNKS.create(256, i2);
         } else {
            ServerLevel serverlevel = pSource.getLevel();
            ResourceKey<Level> resourcekey = serverlevel.dimension();
            ChunkPos chunkpos = null;
            int j2 = 0;

            for(int k2 = i1; k2 <= k1; ++k2) {
               for(int l2 = j1; l2 <= l1; ++l2) {
                  boolean flag = serverlevel.setChunkForced(k2, l2, pAdd);
                  if (flag) {
                     ++j2;
                     if (chunkpos == null) {
                        chunkpos = new ChunkPos(k2, l2);
                     }
                  }
               }
            }

            ChunkPos chunkpos1 = chunkpos;
            if (j2 == 0) {
               throw (pAdd ? ERROR_ALL_ADDED : ERROR_NONE_REMOVED).create();
            } else {
               if (j2 == 1) {
                  pSource.sendSuccess(() -> {
                     return Component.translatable("commands.forceload." + (pAdd ? "added" : "removed") + ".single", chunkpos1, resourcekey.location());
                  }, true);
               } else {
                  ChunkPos chunkpos2 = new ChunkPos(i1, j1);
                  ChunkPos chunkpos3 = new ChunkPos(k1, l1);
                  pSource.sendSuccess(() -> {
                     return Component.translatable("commands.forceload." + (pAdd ? "added" : "removed") + ".multiple", chunkpos1, resourcekey.location(), chunkpos2, chunkpos3);
                  }, true);
               }

               return j2;
            }
         }
      } else {
         throw BlockPosArgument.ERROR_OUT_OF_WORLD.create();
      }
   }
}