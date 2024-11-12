package net.minecraft;

import org.slf4j.Logger;

public class DefaultUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
   private final Logger logger;

   public DefaultUncaughtExceptionHandler(Logger pLogger) {
      this.logger = pLogger;
   }

   public void uncaughtException(Thread pThread, Throwable pException) {
      this.logger.error("Caught previously unhandled exception :", pException);
   }
}