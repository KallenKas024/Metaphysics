package net.minecraft.server.commands;

import com.google.common.base.Stopwatch;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

public class LocateCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_NOT_FOUND = new DynamicCommandExceptionType((p_201831_) -> {
      return Component.translatable("commands.locate.structure.not_found", p_201831_);
   });
   private static final DynamicCommandExceptionType ERROR_STRUCTURE_INVALID = new DynamicCommandExceptionType((p_207534_) -> {
      return Component.translatable("commands.locate.structure.invalid", p_207534_);
   });
   private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType((p_214514_) -> {
      return Component.translatable("commands.locate.biome.not_found", p_214514_);
   });
   private static final DynamicCommandExceptionType ERROR_POI_NOT_FOUND = new DynamicCommandExceptionType((p_214512_) -> {
      return Component.translatable("commands.locate.poi.not_found", p_214512_);
   });
   private static final int MAX_STRUCTURE_SEARCH_RADIUS = 100;
   private static final int MAX_BIOME_SEARCH_RADIUS = 6400;
   private static final int BIOME_SAMPLE_RESOLUTION_HORIZONTAL = 32;
   private static final int BIOME_SAMPLE_RESOLUTION_VERTICAL = 64;
   private static final int POI_SEARCH_RADIUS = 256;

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher, CommandBuildContext pContext) {
      pDispatcher.register(Commands.literal("locate").requires((p_214470_) -> {
         return p_214470_.hasPermission(2);
      }).then(Commands.literal("structure").then(Commands.argument("structure", ResourceOrTagKeyArgument.resourceOrTagKey(Registries.STRUCTURE)).executes((p_258233_) -> {
         return locateStructure(p_258233_.getSource(), ResourceOrTagKeyArgument.getResourceOrTagKey(p_258233_, "structure", Registries.STRUCTURE, ERROR_STRUCTURE_INVALID));
      }))).then(Commands.literal("biome").then(Commands.argument("biome", ResourceOrTagArgument.resourceOrTag(pContext, Registries.BIOME)).executes((p_258232_) -> {
         return locateBiome(p_258232_.getSource(), ResourceOrTagArgument.getResourceOrTag(p_258232_, "biome", Registries.BIOME));
      }))).then(Commands.literal("poi").then(Commands.argument("poi", ResourceOrTagArgument.resourceOrTag(pContext, Registries.POINT_OF_INTEREST_TYPE)).executes((p_258234_) -> {
         return locatePoi(p_258234_.getSource(), ResourceOrTagArgument.getResourceOrTag(p_258234_, "poi", Registries.POINT_OF_INTEREST_TYPE));
      }))));
   }

   private static Optional<? extends HolderSet.ListBacked<Structure>> getHolders(ResourceOrTagKeyArgument.Result<Structure> pStructure, Registry<Structure> pStructureRegistry) {
      return pStructure.unwrap().map((p_258231_) -> {
         return pStructureRegistry.getHolder(p_258231_).map((p_214491_) -> {
            return HolderSet.direct(p_214491_);
         });
      }, pStructureRegistry::getTag);
   }

   private static int locateStructure(CommandSourceStack pSource, ResourceOrTagKeyArgument.Result<Structure> pStructure) throws CommandSyntaxException {
      Registry<Structure> registry = pSource.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
      HolderSet<Structure> holderset = getHolders(pStructure, registry).orElseThrow(() -> {
         return ERROR_STRUCTURE_INVALID.create(pStructure.asPrintable());
      });
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      ServerLevel serverlevel = pSource.getLevel();
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Pair<BlockPos, Holder<Structure>> pair = serverlevel.getChunkSource().getGenerator().findNearestMapStructure(serverlevel, holderset, blockpos, 100, false);
      stopwatch.stop();
      if (pair == null) {
         throw ERROR_STRUCTURE_NOT_FOUND.create(pStructure.asPrintable());
      } else {
         return showLocateResult(pSource, pStructure, blockpos, pair, "commands.locate.structure.success", false, stopwatch.elapsed());
      }
   }

   private static int locateBiome(CommandSourceStack pSource, ResourceOrTagArgument.Result<Biome> pBiome) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Pair<BlockPos, Holder<Biome>> pair = pSource.getLevel().findClosestBiome3d(pBiome, blockpos, 6400, 32, 64);
      stopwatch.stop();
      if (pair == null) {
         throw ERROR_BIOME_NOT_FOUND.create(pBiome.asPrintable());
      } else {
         return showLocateResult(pSource, pBiome, blockpos, pair, "commands.locate.biome.success", true, stopwatch.elapsed());
      }
   }

   private static int locatePoi(CommandSourceStack pSource, ResourceOrTagArgument.Result<PoiType> pPoiType) throws CommandSyntaxException {
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      ServerLevel serverlevel = pSource.getLevel();
      Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
      Optional<Pair<Holder<PoiType>, BlockPos>> optional = serverlevel.getPoiManager().findClosestWithType(pPoiType, blockpos, 256, PoiManager.Occupancy.ANY);
      stopwatch.stop();
      if (optional.isEmpty()) {
         throw ERROR_POI_NOT_FOUND.create(pPoiType.asPrintable());
      } else {
         return showLocateResult(pSource, pPoiType, blockpos, optional.get().swap(), "commands.locate.poi.success", false, stopwatch.elapsed());
      }
   }

   private static String getElementName(Pair<BlockPos, ? extends Holder<?>> pResultWithPosition) {
      return pResultWithPosition.getSecond().unwrapKey().map((p_214498_) -> {
         return p_214498_.location().toString();
      }).orElse("[unregistered]");
   }

   public static int showLocateResult(CommandSourceStack pSource, ResourceOrTagArgument.Result<?> pResult, BlockPos pSourcePosition, Pair<BlockPos, ? extends Holder<?>> pResultWithPosition, String pTranslationKey, boolean pAbsoluteY, Duration pDuration) {
      String s = pResult.unwrap().map((p_248147_) -> {
         return pResult.asPrintable();
      }, (p_248143_) -> {
         return pResult.asPrintable() + " (" + getElementName(pResultWithPosition) + ")";
      });
      return showLocateResult(pSource, pSourcePosition, pResultWithPosition, pTranslationKey, pAbsoluteY, s, pDuration);
   }

   public static int showLocateResult(CommandSourceStack pSource, ResourceOrTagKeyArgument.Result<?> pResult, BlockPos pSourcePosition, Pair<BlockPos, ? extends Holder<?>> pResultWithPosition, String pTranslationKey, boolean pAbsoluteY, Duration pDuration) {
      String s = pResult.unwrap().map((p_214463_) -> {
         return p_214463_.location().toString();
      }, (p_248145_) -> {
         return "#" + p_248145_.location() + " (" + getElementName(pResultWithPosition) + ")";
      });
      return showLocateResult(pSource, pSourcePosition, pResultWithPosition, pTranslationKey, pAbsoluteY, s, pDuration);
   }

   private static int showLocateResult(CommandSourceStack pSource, BlockPos pSourcePosition, Pair<BlockPos, ? extends Holder<?>> pResultWithoutPosition, String pTranslationKey, boolean pAbsoluteY, String pElementName, Duration pDuration) {
      BlockPos blockpos = pResultWithoutPosition.getFirst();
      int i = pAbsoluteY ? Mth.floor(Mth.sqrt((float)pSourcePosition.distSqr(blockpos))) : Mth.floor(dist(pSourcePosition.getX(), pSourcePosition.getZ(), blockpos.getX(), blockpos.getZ()));
      String s = pAbsoluteY ? String.valueOf(blockpos.getY()) : "~";
      Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockpos.getX(), s, blockpos.getZ())).withStyle((p_214489_) -> {
         return p_214489_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockpos.getX() + " " + s + " " + blockpos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")));
      });
      pSource.sendSuccess(() -> {
         return Component.translatable(pTranslationKey, pElementName, component, i);
      }, false);
      LOGGER.info("Locating element " + pElementName + " took " + pDuration.toMillis() + " ms");
      return i;
   }

   private static float dist(int pX1, int pZ1, int pX2, int pZ2) {
      int i = pX2 - pX1;
      int j = pZ2 - pZ1;
      return Mth.sqrt((float)(i * i + j * j));
   }
}