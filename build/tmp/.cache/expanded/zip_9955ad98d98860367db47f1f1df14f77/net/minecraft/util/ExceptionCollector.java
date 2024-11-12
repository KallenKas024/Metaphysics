package net.minecraft.util;

import javax.annotation.Nullable;

public class ExceptionCollector<T extends Throwable> {
   @Nullable
   private T result;

   public void add(T pException) {
      if (this.result == null) {
         this.result = pException;
      } else {
         this.result.addSuppressed(pException);
      }

   }

   public void throwIfPresent() throws T {
      if (this.result != null) {
         throw this.result;
      }
   }
}