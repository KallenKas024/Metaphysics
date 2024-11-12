package net.minecraft.gametest.framework;

import javax.annotation.Nullable;

class GameTestEvent {
   @Nullable
   public final Long expectedDelay;
   public final Runnable assertion;

   private GameTestEvent(@Nullable Long pExpectedDelay, Runnable pAssertion) {
      this.expectedDelay = pExpectedDelay;
      this.assertion = pAssertion;
   }

   static GameTestEvent create(Runnable pAssertion) {
      return new GameTestEvent((Long)null, pAssertion);
   }

   static GameTestEvent create(long pExpectedDelay, Runnable pAssertion) {
      return new GameTestEvent(pExpectedDelay, pAssertion);
   }
}