package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import org.slf4j.Logger;

public class DebugLoggedPrintStream extends LoggedPrintStream {
   private static final Logger LOGGER = LogUtils.getLogger();

   public DebugLoggedPrintStream(String pName, OutputStream pOut) {
      super(pName, pOut);
   }

   protected void logLine(String pString) {
      StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
      StackTraceElement stacktraceelement = astacktraceelement[Math.min(3, astacktraceelement.length)];
      LOGGER.info("[{}]@.({}:{}): {}", this.name, stacktraceelement.getFileName(), stacktraceelement.getLineNumber(), pString);
   }
}