package net.minecraft.gametest.framework;

import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatch {
   public static final String DEFAULT_BATCH_NAME = "defaultBatch";
   private final String name;
   private final Collection<TestFunction> testFunctions;
   @Nullable
   private final Consumer<ServerLevel> beforeBatchFunction;
   @Nullable
   private final Consumer<ServerLevel> afterBatchFunction;

   public GameTestBatch(String pName, Collection<TestFunction> pTestFunctions, @Nullable Consumer<ServerLevel> pBeforeBatchFunction, @Nullable Consumer<ServerLevel> pAfterBatchFunction) {
      if (pTestFunctions.isEmpty()) {
         throw new IllegalArgumentException("A GameTestBatch must include at least one TestFunction!");
      } else {
         this.name = pName;
         this.testFunctions = pTestFunctions;
         this.beforeBatchFunction = pBeforeBatchFunction;
         this.afterBatchFunction = pAfterBatchFunction;
      }
   }

   public String getName() {
      return this.name;
   }

   public Collection<TestFunction> getTestFunctions() {
      return this.testFunctions;
   }

   public void runBeforeBatchFunction(ServerLevel pServerLevel) {
      if (this.beforeBatchFunction != null) {
         this.beforeBatchFunction.accept(pServerLevel);
      }

   }

   public void runAfterBatchFunction(ServerLevel pServerLevel) {
      if (this.afterBatchFunction != null) {
         this.afterBatchFunction.accept(pServerLevel);
      }

   }
}