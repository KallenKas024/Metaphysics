package net.minecraft;

import org.slf4j.Logger;

public class DefaultUncaughtExceptionHandlerWithName implements Thread.UncaughtExceptionHandler {
   private final Logger logger;

   public DefaultUncaughtExceptionHandlerWithName(Logger pLogger) {
      this.logger = pLogger;
   }

   public void uncaughtException(Thread pThread, Throwable pException) {
      this.logger.error("Caught previously unhandled exception :");
      this.logger.error(pThread.getName(), pException);
   }
}