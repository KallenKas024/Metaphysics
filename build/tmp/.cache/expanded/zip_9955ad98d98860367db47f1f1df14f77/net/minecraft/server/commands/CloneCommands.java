package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class CloneCommands {
   private static final SimpleCommandExceptionType ERROR_OVERLAP = new SimpleCommandExceptionType(Component.translatable("commands.clone.overlap"));
   private static final Dynamic2CommandExceptionType ERROR_AREA_TOO_LARGE = new Dynamic2CommandExceptionType((p_136743_, p_136744_) -> {
      return Component.translatable("commands.clone.toobig", p_136743_, p_136744_);
   });
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.clone.failed"));
   public static final Predicate<BlockInWorld> FILTER_AIR = (p_284652_) -> {
      return !p_284652_.getState().isAir();
   };

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
      pDispatcher.register(Commands.literal("clone").requires((p_136734_) -> {
         return p_136734_.hasPermission(2);
      }).then(beginEndDestinationAndModeSuffix(pContext, (p_264757_) -> {
         return p_264757_.getSource().getLevel();
      })).then(Commands.literal("from").then(Commands.argument("sourceDimension", DimensionArgument.dimension()).then(beginEndDestinationAndModeSuffix(pContext, (p_264743_) -> {
         return DimensionArgument.getDimension(p_264743_, "sourceDimension");
      })))));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> beginEndDestinationAndModeSuffix(CommandBuildContext pBuildContext, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> pLevelGetter) {
      return Commands.argument("begin", BlockPosArgument.blockPos()).then(Commands.argument("end", BlockPosArgument.blockPos()).then(destinationAndModeSuffix(pBuildContext, pLevelGetter, (p_264751_) -> {
         return p_264751_.getSource().getLevel();
      })).then(Commands.literal("to").then(Commands.argument("targetDimension", DimensionArgument.dimension()).then(destinationAndModeSuffix(pBuildContext, pLevelGetter, (p_264756_) -> {
         return DimensionArgument.getDimension(p_264756_, "targetDimension");
      })))));
   }

   private static CloneCommands.DimensionAndPosition getLoadedDimensionAndPosition(CommandContext<CommandSourceStack> pContext, ServerLevel pLevel, String pName) throws CommandSyntaxException {
      BlockPos blockpos = BlockPosArgument.getLoadedBlockPos(pContext, pLevel, pName);
      return new CloneCommands.DimensionAndPosition(pLevel, blockpos);
   }

   private static ArgumentBuilder<CommandSourceStack, ?> destinationAndModeSuffix(CommandBuildContext pBuildContext, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> pSourceLevelGetter, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, ServerLevel> pDestinationLevelGetter) {
      CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandfunction = (p_264737_) -> {
         return getLoadedDimensionAndPosition(p_264737_, pSourceLevelGetter.apply(p_264737_), "begin");
      };
      CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandfunction1 = (p_264735_) -> {
         return getLoadedDimensionAndPosition(p_264735_, pSourceLevelGetter.apply(p_264735_), "end");
      };
      CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> commandfunction2 = (p_264768_) -> {
         return getLoadedDimensionAndPosition(p_264768_, pDestinationLevelGetter.apply(p_264768_), "destination");
      };
      return Commands.argument("destination", BlockPosArgument.blockPos()).executes((p_264761_) -> {
         return clone(p_264761_.getSource(), commandfunction.apply(p_264761_), commandfunction1.apply(p_264761_), commandfunction2.apply(p_264761_), (p_180033_) -> {
            return true;
         }, CloneCommands.Mode.NORMAL);
      }).then(wrapWithCloneMode(commandfunction, commandfunction1, commandfunction2, (p_264738_) -> {
         return (p_180041_) -> {
            return true;
         };
      }, Commands.literal("replace").executes((p_264755_) -> {
         return clone(p_264755_.getSource(), commandfunction.apply(p_264755_), commandfunction1.apply(p_264755_), commandfunction2.apply(p_264755_), (p_180039_) -> {
            return true;
         }, CloneCommands.Mode.NORMAL);
      }))).then(wrapWithCloneMode(commandfunction, commandfunction1, commandfunction2, (p_264744_) -> {
         return FILTER_AIR;
      }, Commands.literal("masked").executes((p_264742_) -> {
         return clone(p_264742_.getSource(), commandfunction.apply(p_264742_), commandfunction1.apply(p_264742_), commandfunction2.apply(p_264742_), FILTER_AIR, CloneCommands.Mode.NORMAL);
      }))).then(Commands.literal("filtered").then(wrapWithCloneMode(commandfunction, commandfunction1, commandfunction2, (p_264745_) -> {
         return BlockPredicateArgument.getBlockPredicate(p_264745_, "filter");
      }, Commands.argument("filter", BlockPredicateArgument.blockPredicate(pBuildContext)).executes((p_264733_) -> {
         return clone(p_264733_.getSource(), commandfunction.apply(p_264733_), commandfunction1.apply(p_264733_), commandfunction2.apply(p_264733_), BlockPredicateArgument.getBlockPredicate(p_264733_, "filter"), CloneCommands.Mode.NORMAL);
      }))));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> wrapWithCloneMode(CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> pBeginGetter, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> pEndGetter, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, CloneCommands.DimensionAndPosition> pTargetGetter, CloneCommands.CommandFunction<CommandContext<CommandSourceStack>, Predicate<BlockInWorld>> pFilterGetter, ArgumentBuilder<CommandSourceStack, ?> pArgumentBuilder) {
      return pArgumentBuilder.then(Commands.literal("force").executes((p_264773_) -> {
         return clone(p_264773_.getSource(), pBeginGetter.apply(p_264773_), pEndGetter.apply(p_264773_), pTargetGetter.apply(p_264773_), pFilterGetter.apply(p_264773_), CloneCommands.Mode.FORCE);
      })).then(Commands.literal("move").executes((p_264766_) -> {
         return clone(p_264766_.getSource(), pBeginGetter.apply(p_264766_), pEndGetter.apply(p_264766_), pTargetGetter.apply(p_264766_), pFilterGetter.apply(p_264766_), CloneCommands.Mode.MOVE);
      })).then(Commands.literal("normal").executes((p_264750_) -> {
         return clone(p_264750_.getSource(), pBeginGetter.apply(p_264750_), pEndGetter.apply(p_264750_), pTargetGetter.apply(p_264750_), pFilterGetter.apply(p_264750_), CloneCommands.Mode.NORMAL);
      }));
   }

   private static int clone(CommandSourceStack pSource, CloneCommands.DimensionAndPosition pBegin, CloneCommands.DimensionAndPosition pEnd, CloneCommands.DimensionAndPosition pTarget, Predicate<BlockInWorld> pFilter, CloneCommands.Mode pMode) throws CommandSyntaxException {
      BlockPos blockpos = pBegin.position();
      BlockPos blockpos1 = pEnd.position();
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos1);
      BlockPos blockpos2 = pTarget.position();
      BlockPos blockpos3 = blockpos2.offset(boundingbox.getLength());
      BoundingBox boundingbox1 = BoundingBox.fromCorners(blockpos2, blockpos3);
      ServerLevel serverlevel = pBegin.dimension();
      ServerLevel serverlevel1 = pTarget.dimension();
      if (!pMode.canOverlap() && serverlevel == serverlevel1 && boundingbox1.intersects(boundingbox)) {
         throw ERROR_OVERLAP.create();
      } else {
         int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
         int j = pSource.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
         if (i > j) {
            throw ERROR_AREA_TOO_LARGE.create(j, i);
         } else if (serverlevel.hasChunksAt(blockpos, blockpos1) && serverlevel1.hasChunksAt(blockpos2, blockpos3)) {
            List<CloneCommands.CloneBlockInfo> list = Lists.newArrayList();
            List<CloneCommands.CloneBlockInfo> list1 = Lists.newArrayList();
            List<CloneCommands.CloneBlockInfo> list2 = Lists.newArrayList();
            Deque<BlockPos> deque = Lists.newLinkedList();
            BlockPos blockpos4 = new BlockPos(boundingbox1.minX() - boundingbox.minX(), boundingbox1.minY() - boundingbox.minY(), boundingbox1.minZ() - boundingbox.minZ());

            for(int k = boundingbox.minZ(); k <= boundingbox.maxZ(); ++k) {
               for(int l = boundingbox.minY(); l <= boundingbox.maxY(); ++l) {
                  for(int i1 = boundingbox.minX(); i1 <= boundingbox.maxX(); ++i1) {
                     BlockPos blockpos5 = new BlockPos(i1, l, k);
                     BlockPos blockpos6 = blockpos5.offset(blockpos4);
                     BlockInWorld blockinworld = new BlockInWorld(serverlevel, blockpos5, false);
                     BlockState blockstate = blockinworld.getState();
                     if (pFilter.test(blockinworld)) {
                        BlockEntity blockentity = serverlevel.getBlockEntity(blockpos5);
                        if (blockentity != null) {
                           CompoundTag compoundtag = blockentity.saveWithoutMetadata();
                           list1.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, compoundtag));
                           deque.addLast(blockpos5);
                        } else if (!blockstate.isSolidRender(serverlevel, blockpos5) && !blockstate.isCollisionShapeFullBlock(serverlevel, blockpos5)) {
                           list2.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, (CompoundTag)null));
                           deque.addFirst(blockpos5);
                        } else {
                           list.add(new CloneCommands.CloneBlockInfo(blockpos6, blockstate, (CompoundTag)null));
                           deque.addLast(blockpos5);
                        }
                     }
                  }
               }
            }

            if (pMode == CloneCommands.Mode.MOVE) {
               for(BlockPos blockpos7 : deque) {
                  BlockEntity blockentity1 = serverlevel.getBlockEntity(blockpos7);
                  Clearable.tryClear(blockentity1);
                  serverlevel.setBlock(blockpos7, Blocks.BARRIER.defaultBlockState(), 2);
               }

               for(BlockPos blockpos8 : deque) {
                  serverlevel.setBlock(blockpos8, Blocks.AIR.defaultBlockState(), 3);
               }
            }

            List<CloneCommands.CloneBlockInfo> list3 = Lists.newArrayList();
            list3.addAll(list);
            list3.addAll(list1);
            list3.addAll(list2);
            List<CloneCommands.CloneBlockInfo> list4 = Lists.reverse(list3);

            for(CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo : list4) {
               BlockEntity blockentity2 = serverlevel1.getBlockEntity(clonecommands$cloneblockinfo.pos);
               Clearable.tryClear(blockentity2);
               serverlevel1.setBlock(clonecommands$cloneblockinfo.pos, Blocks.BARRIER.defaultBlockState(), 2);
            }

            int j1 = 0;

            for(CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo1 : list3) {
               if (serverlevel1.setBlock(clonecommands$cloneblockinfo1.pos, clonecommands$cloneblockinfo1.state, 2)) {
                  ++j1;
               }
            }

            for(CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo2 : list1) {
               BlockEntity blockentity3 = serverlevel1.getBlockEntity(clonecommands$cloneblockinfo2.pos);
               if (clonecommands$cloneblockinfo2.tag != null && blockentity3 != null) {
                  blockentity3.load(clonecommands$cloneblockinfo2.tag);
                  blockentity3.setChanged();
               }

               serverlevel1.setBlock(clonecommands$cloneblockinfo2.pos, clonecommands$cloneblockinfo2.state, 2);
            }

            for(CloneCommands.CloneBlockInfo clonecommands$cloneblockinfo3 : list4) {
               serverlevel1.blockUpdated(clonecommands$cloneblockinfo3.pos, clonecommands$cloneblockinfo3.state.getBlock());
            }

            serverlevel1.getBlockTicks().copyAreaFrom(serverlevel.getBlockTicks(), boundingbox, blockpos4);
            if (j1 == 0) {
               throw ERROR_FAILED.create();
            } else {
               int k1 = j1;
               pSource.sendSuccess(() -> {
                  return Component.translatable("commands.clone.success", k1);
               }, true);
               return j1;
            }
         } else {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
         }
      }
   }

   static class CloneBlockInfo {
      public final BlockPos pos;
      public final BlockState state;
      @Nullable
      public final CompoundTag tag;

      public CloneBlockInfo(BlockPos pPos, BlockState pState, @Nullable CompoundTag pTag) {
         this.pos = pPos;
         this.state = pState;
         this.tag = pTag;
      }
   }

   @FunctionalInterface
   interface CommandFunction<T, R> {
      R apply(T pInput) throws CommandSyntaxException;
   }

   static record DimensionAndPosition(ServerLevel dimension, BlockPos position) {
   }

   static enum Mode {
      FORCE(true),
      MOVE(true),
      NORMAL(false);

      private final boolean canOverlap;

      private Mode(boolean pCanOverlap) {
         this.canOverlap = pCanOverlap;
      }

      public boolean canOverlap() {
         return this.canOverlap;
      }
   }
}