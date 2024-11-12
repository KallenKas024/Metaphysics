package net.minecraft.gametest.framework;

public interface GameTestListener {
   void testStructureLoaded(GameTestInfo pTestInfo);

   void testPassed(GameTestInfo pTestInfo);

   void testFailed(GameTestInfo pTestInfo);
}