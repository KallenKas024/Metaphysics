package net.minecraft.gametest.framework;

class ExhaustedAttemptsException extends Throwable {
   public ExhaustedAttemptsException(int pMadeAttempts, int pSuccessfulAttempts, GameTestInfo pTestInfo) {
      super("Not enough successes: " + pSuccessfulAttempts + " out of " + pMadeAttempts + " attempts. Required successes: " + pTestInfo.requiredSuccesses() + ". max attempts: " + pTestInfo.maxAttempts() + ".", pTestInfo.getError());
   }
}