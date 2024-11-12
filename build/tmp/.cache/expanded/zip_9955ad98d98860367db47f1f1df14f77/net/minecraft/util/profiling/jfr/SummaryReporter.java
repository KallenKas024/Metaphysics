package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class SummaryReporter {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Runnable onDeregistration;

   protected SummaryReporter(Runnable pOnDeregistration) {
      this.onDeregistration = pOnDeregistration;
   }

   public void recordingStopped(@Nullable Path pOutputPath) {
      if (pOutputPath != null) {
         this.onDeregistration.run();
         infoWithFallback(() -> {
            return "Dumped flight recorder profiling to " + pOutputPath;
         });

         JfrStatsResult jfrstatsresult;
         try {
            jfrstatsresult = JfrStatsParser.parse(pOutputPath);
         } catch (Throwable throwable1) {
            warnWithFallback(() -> {
               return "Failed to parse JFR recording";
            }, throwable1);
            return;
         }

         try {
            infoWithFallback(jfrstatsresult::asJson);
            Path path = pOutputPath.resolveSibling("jfr-report-" + StringUtils.substringBefore(pOutputPath.getFileName().toString(), ".jfr") + ".json");
            Files.writeString(path, jfrstatsresult.asJson(), StandardOpenOption.CREATE);
            infoWithFallback(() -> {
               return "Dumped recording summary to " + path;
            });
         } catch (Throwable throwable) {
            warnWithFallback(() -> {
               return "Failed to output JFR report";
            }, throwable);
         }

      }
   }

   private static void infoWithFallback(Supplier<String> pMessage) {
      if (LogUtils.isLoggerActive()) {
         LOGGER.info(pMessage.get());
      } else {
         Bootstrap.realStdoutPrintln(pMessage.get());
      }

   }

   private static void warnWithFallback(Supplier<String> pMessage, Throwable pThrowable) {
      if (LogUtils.isLoggerActive()) {
         LOGGER.warn(pMessage.get(), pThrowable);
      } else {
         Bootstrap.realStdoutPrintln(pMessage.get());
         pThrowable.printStackTrace(Bootstrap.STDOUT);
      }

   }
}