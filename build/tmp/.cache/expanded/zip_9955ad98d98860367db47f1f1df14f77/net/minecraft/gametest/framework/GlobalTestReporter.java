package net.minecraft.gametest.framework;

public class GlobalTestReporter {
   private static TestReporter DELEGATE = new LogTestReporter();

   public static void replaceWith(TestReporter pTestReporter) {
      DELEGATE = pTestReporter;
   }

   public static void onTestFailed(GameTestInfo pTestInfo) {
      DELEGATE.onTestFailed(pTestInfo);
   }

   public static void onTestSuccess(GameTestInfo pTestInfo) {
      DELEGATE.onTestSuccess(pTestInfo);
   }

   public static void finish() {
      DELEGATE.finish();
   }
}