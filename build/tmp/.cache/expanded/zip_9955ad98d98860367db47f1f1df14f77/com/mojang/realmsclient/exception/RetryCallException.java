package com.mojang.realmsclient.exception;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RetryCallException extends RealmsServiceException {
   public static final int DEFAULT_DELAY = 5;
   public final int delaySeconds;

   public RetryCallException(int pRetryAfter, int pHttpResultCode) {
      super(pHttpResultCode, "Retry operation");
      if (pRetryAfter >= 0 && pRetryAfter <= 120) {
         this.delaySeconds = pRetryAfter;
      } else {
         this.delaySeconds = 5;
      }

   }
}