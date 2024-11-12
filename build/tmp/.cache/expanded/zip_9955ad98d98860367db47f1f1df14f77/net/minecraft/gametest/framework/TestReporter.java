package net.minecraft.gametest.framework;

public interface TestReporter {
   void onTestFailed(GameTestInfo pTestInfo);

   void onTestSuccess(GameTestInfo pTestInfo);

   default void finish() {
   }
}