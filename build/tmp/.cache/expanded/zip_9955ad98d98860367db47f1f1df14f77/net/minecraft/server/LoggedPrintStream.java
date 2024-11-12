package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class LoggedPrintStream extends PrintStream {
   private static final Logger LOGGER = LogUtils.getLogger();
   protected final String name;

   public LoggedPrintStream(String pName, OutputStream pOut) {
      super(pOut);
      this.name = pName;
   }

   public void println(@Nullable String pMessage) {
      this.logLine(pMessage);
   }

   public void println(Object pObject) {
      this.logLine(String.valueOf(pObject));
   }

   protected void logLine(@Nullable String pString) {
      LOGGER.info("[{}]: {}", this.name, pString);
   }
}