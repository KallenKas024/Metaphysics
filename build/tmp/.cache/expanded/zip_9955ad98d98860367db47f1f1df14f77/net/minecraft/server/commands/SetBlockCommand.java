package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class SetBlockCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.setblock.failed"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
      pDispatcher.register(Commands.literal("setblock").requires((p_138606_) -> {
         return p_138606_.hasPermission(2);
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).then(Commands.argument("block", BlockStateArgument.block(pContext)).executes((p_138618_) -> {
         return setBlock(p_138618_.getSource(), BlockPosArgument.getLoadedBlockPos(p_138618_, "pos"), BlockStateArgument.getBlock(p_138618_, "block"), SetBlockCommand.Mode.REPLACE, (Predicate<BlockInWorld>)null);
      }).then(Commands.literal("destroy").executes((p_138616_) -> {
         return setBlock(p_138616_.getSource(), BlockPosArgument.getLoadedBlockPos(p_138616_, "pos"), BlockStateArgument.getBlock(p_138616_, "block"), SetBlockCommand.Mode.DESTROY, (Predicate<BlockInWorld>)null);
      })).then(Commands.literal("keep").executes((p_138614_) -> {
         return setBlock(p_138614_.getSource(), BlockPosArgument.getLoadedBlockPos(p_138614_, "pos"), BlockStateArgument.getBlock(p_138614_, "block"), SetBlockCommand.Mode.REPLACE, (p_180517_) -> {
            return p_180517_.getLevel().isEmptyBlock(p_180517_.getPos());
         });
      })).then(Commands.literal("replace").executes((p_138604_) -> {
         return setBlock(p_138604_.getSource(), BlockPosArgument.getLoadedBlockPos(p_138604_, "pos"), BlockStateArgument.getBlock(p_138604_, "block"), SetBlockCommand.Mode.REPLACE, (Predicate<BlockInWorld>)null);
      })))));
   }

   private static int setBlock(CommandSourceStack pSource, BlockPos pPos, BlockInput pState, SetBlockCommand.Mode pMode, @Nullable Predicate<BlockInWorld> pPredicate) throws CommandSyntaxException {
      ServerLevel serverlevel = pSource.getLevel();
      if (pPredicate != null && !pPredicate.test(new BlockInWorld(serverlevel, pPos, true))) {
         throw ERROR_FAILED.create();
      } else {
         boolean flag;
         if (pMode == SetBlockCommand.Mode.DESTROY) {
            serverlevel.destroyBlock(pPos, true);
            flag = !pState.getState().isAir() || !serverlevel.getBlockState(pPos).isAir();
         } else {
            BlockEntity blockentity = serverlevel.getBlockEntity(pPos);
            Clearable.tryClear(blockentity);
            flag = true;
         }

         if (flag && !pState.place(serverlevel, pPos, 2)) {
            throw ERROR_FAILED.create();
         } else {
            serverlevel.blockUpdated(pPos, pState.getState().getBlock());
            pSource.sendSuccess(() -> {
               return Component.translatable("commands.setblock.success", pPos.getX(), pPos.getY(), pPos.getZ());
            }, true);
            return 1;
         }
      }
   }

   public interface Filter {
      @Nullable
      BlockInput filter(BoundingBox pBoundingBox, BlockPos pPos, BlockInput pBlockInput, ServerLevel pLevel);
   }

   public static enum Mode {
      REPLACE,
      DESTROY;
   }
}