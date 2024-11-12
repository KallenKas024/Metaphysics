package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FileZipper;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class PerfCommand {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final SimpleCommandExceptionType ERROR_NOT_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.notRunning"));
   private static final SimpleCommandExceptionType ERROR_ALREADY_RUNNING = new SimpleCommandExceptionType(Component.translatable("commands.perf.alreadyRunning"));

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("perf").requires((p_180462_) -> {
         return p_180462_.hasPermission(4);
      }).then(Commands.literal("start").executes((p_180455_) -> {
         return startProfilingDedicatedServer(p_180455_.getSource());
      })).then(Commands.literal("stop").executes((p_180440_) -> {
         return stopProfilingDedicatedServer(p_180440_.getSource());
      })));
   }

   private static int startProfilingDedicatedServer(CommandSourceStack pSource) throws CommandSyntaxException {
      MinecraftServer minecraftserver = pSource.getServer();
      if (minecraftserver.isRecordingMetrics()) {
         throw ERROR_ALREADY_RUNNING.create();
      } else {
         Consumer<ProfileResults> consumer = (p_180460_) -> {
            whenStopped(pSource, p_180460_);
         };
         Consumer<Path> consumer1 = (p_180453_) -> {
            saveResults(pSource, p_180453_, minecraftserver);
         };
         minecraftserver.startRecordingMetrics(consumer, consumer1);
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.perf.started");
         }, false);
         return 0;
      }
   }

   private static int stopProfilingDedicatedServer(CommandSourceStack pSource) throws CommandSyntaxException {
      MinecraftServer minecraftserver = pSource.getServer();
      if (!minecraftserver.isRecordingMetrics()) {
         throw ERROR_NOT_RUNNING.create();
      } else {
         minecraftserver.finishRecordingMetrics();
         return 0;
      }
   }

   private static void saveResults(CommandSourceStack pSource, Path pPath, MinecraftServer pServer) {
      String s = String.format(Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), pServer.getWorldData().getLevelName(), SharedConstants.getCurrentVersion().getId());

      String s1;
      try {
         s1 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, s, ".zip");
      } catch (IOException ioexception1) {
         pSource.sendFailure(Component.translatable("commands.perf.reportFailed"));
         LOGGER.error("Failed to create report name", (Throwable)ioexception1);
         return;
      }

      try (FileZipper filezipper = new FileZipper(MetricsPersister.PROFILING_RESULTS_DIR.resolve(s1))) {
         filezipper.add(Paths.get("system.txt"), pServer.fillSystemReport(new SystemReport()).toLineSeparatedString());
         filezipper.add(pPath);
      }

      try {
         FileUtils.forceDelete(pPath.toFile());
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to delete temporary profiling file {}", pPath, ioexception);
      }

      pSource.sendSuccess(() -> {
         return Component.translatable("commands.perf.reportSaved", s1);
      }, false);
   }

   private static void whenStopped(CommandSourceStack pSource, ProfileResults pResults) {
      if (pResults != EmptyProfileResults.EMPTY) {
         int i = pResults.getTickDuration();
         double d0 = (double)pResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
         pSource.sendSuccess(() -> {
            return Component.translatable("commands.perf.stopped", String.format(Locale.ROOT, "%.2f", d0), i, String.format(Locale.ROOT, "%.2f", (double)i / d0));
         }, false);
      }
   }
}