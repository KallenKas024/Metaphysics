package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
   public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
   private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType((p_262025_, p_261647_) -> {
      return Component.translatable("commands.fillbiome.toobig", p_262025_, p_261647_);
   });

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
      pDispatcher.register(Commands.literal("fillbiome").requires((p_261890_) -> {
         return p_261890_.hasPermission(2);
      }).then(Commands.argument("from", BlockPosArgument.blockPos()).then(Commands.argument("to", BlockPosArgument.blockPos()).then(Commands.argument("biome", ResourceArgument.resource(pContext, Registries.BIOME)).executes((p_262554_) -> {
         return fill(p_262554_.getSource(), BlockPosArgument.getLoadedBlockPos(p_262554_, "from"), BlockPosArgument.getLoadedBlockPos(p_262554_, "to"), ResourceArgument.getResource(p_262554_, "biome", Registries.BIOME), (p_262543_) -> {
            return true;
         });
      }).then(Commands.literal("replace").then(Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(pContext, Registries.BIOME)).executes((p_262544_) -> {
         return fill(p_262544_.getSource(), BlockPosArgument.getLoadedBlockPos(p_262544_, "from"), BlockPosArgument.getLoadedBlockPos(p_262544_, "to"), ResourceArgument.getResource(p_262544_, "biome", Registries.BIOME), ResourceOrTagArgument.getResourceOrTag(p_262544_, "filter", Registries.BIOME)::test);
      })))))));
   }

   private static int quantize(int pValue) {
      return QuartPos.toBlock(QuartPos.fromBlock(pValue));
   }

   private static BlockPos quantize(BlockPos pPos) {
      return new BlockPos(quantize(pPos.getX()), quantize(pPos.getY()), quantize(pPos.getZ()));
   }

   private static BiomeResolver makeResolver(MutableInt pBiomeEntries, ChunkAccess pChunk, BoundingBox pTargetRegion, Holder<Biome> pReplacementBiome, Predicate<Holder<Biome>> pFilter) {
      return (p_262550_, p_262551_, p_262552_, p_262553_) -> {
         int i = QuartPos.toBlock(p_262550_);
         int j = QuartPos.toBlock(p_262551_);
         int k = QuartPos.toBlock(p_262552_);
         Holder<Biome> holder = pChunk.getNoiseBiome(p_262550_, p_262551_, p_262552_);
         if (pTargetRegion.isInside(i, j, k) && pFilter.test(holder)) {
            pBiomeEntries.increment();
            return pReplacementBiome;
         } else {
            return holder;
         }
      };
   }

   private static int fill(CommandSourceStack pSource, BlockPos pFrom, BlockPos pTo, Holder.Reference<Biome> pBiome, Predicate<Holder<Biome>> pFilter) throws CommandSyntaxException {
      BlockPos blockpos = quantize(pFrom);
      BlockPos blockpos1 = quantize(pTo);
      BoundingBox boundingbox = BoundingBox.fromCorners(blockpos, blockpos1);
      int i = boundingbox.getXSpan() * boundingbox.getYSpan() * boundingbox.getZSpan();
      int j = pSource.getLevel().getGameRules().getInt(GameRules.RULE_COMMAND_MODIFICATION_BLOCK_LIMIT);
      if (i > j) {
         throw ERROR_VOLUME_TOO_LARGE.create(j, i);
      } else {
         ServerLevel serverlevel = pSource.getLevel();
         List<ChunkAccess> list = new ArrayList<>();

         for(int k = SectionPos.blockToSectionCoord(boundingbox.minZ()); k <= SectionPos.blockToSectionCoord(boundingbox.maxZ()); ++k) {
            for(int l = SectionPos.blockToSectionCoord(boundingbox.minX()); l <= SectionPos.blockToSectionCoord(boundingbox.maxX()); ++l) {
               ChunkAccess chunkaccess = serverlevel.getChunk(l, k, ChunkStatus.FULL, false);
               if (chunkaccess == null) {
                  throw ERROR_NOT_LOADED.create();
               }

               list.add(chunkaccess);
            }
         }

         MutableInt mutableint = new MutableInt(0);

         for(ChunkAccess chunkaccess1 : list) {
            chunkaccess1.fillBiomesFromNoise(makeResolver(mutableint, chunkaccess1, boundingbox, pBiome, pFilter), serverlevel.getChunkSource().randomState().sampler());
            chunkaccess1.setUnsaved(true);
         }

         serverlevel.getChunkSource().chunkMap.resendBiomesForChunks(list);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.fillbiome.success.count", mutableint.getValue(), boundingbox.minX(), boundingbox.minY(), boundingbox.minZ(), boundingbox.maxX(), boundingbox.maxY(), boundingbox.maxZ());
         }, true);
         return mutableint.getValue();
      }
   }
}