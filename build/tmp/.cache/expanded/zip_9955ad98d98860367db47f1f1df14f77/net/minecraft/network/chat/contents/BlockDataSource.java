package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource {
   public BlockDataSource(String pPosPattern) {
      this(pPosPattern, compilePos(pPosPattern));
   }

   @Nullable
   private static Coordinates compilePos(String pPosPattern) {
      try {
         return BlockPosArgument.blockPos().parse(new StringReader(pPosPattern));
      } catch (CommandSyntaxException commandsyntaxexception) {
         return null;
      }
   }

   public Stream<CompoundTag> getData(CommandSourceStack pSource) {
      if (this.compiledPos != null) {
         ServerLevel serverlevel = pSource.getLevel();
         BlockPos blockpos = this.compiledPos.getBlockPos(pSource);
         if (serverlevel.isLoaded(blockpos)) {
            BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
            if (blockentity != null) {
               return Stream.of(blockentity.saveWithFullMetadata());
            }
         }
      }

      return Stream.empty();
   }

   public String toString() {
      return "block=" + this.posPattern;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof BlockDataSource) {
            BlockDataSource blockdatasource = (BlockDataSource)pOther;
            if (this.posPattern.equals(blockdatasource.posPattern)) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return this.posPattern.hashCode();
   }
}