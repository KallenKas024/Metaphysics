package net.minecraft.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface AbortableIterationConsumer<T> {
   AbortableIterationConsumer.Continuation accept(T pValue);

   static <T> AbortableIterationConsumer<T> forConsumer(Consumer<T> pConsumer) {
      return (p_261916_) -> {
         pConsumer.accept(p_261916_);
         return AbortableIterationConsumer.Continuation.CONTINUE;
      };
   }

   public static enum Continuation {
      CONTINUE,
      ABORT;

      public boolean shouldAbort() {
         return this == ABORT;
      }
   }
}