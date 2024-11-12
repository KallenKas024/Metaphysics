package net.minecraft.gametest.framework;

public class GameTestTimeoutException extends RuntimeException {
   public GameTestTimeoutException(String pExceptionMessage) {
      super(pExceptionMessage);
   }
}