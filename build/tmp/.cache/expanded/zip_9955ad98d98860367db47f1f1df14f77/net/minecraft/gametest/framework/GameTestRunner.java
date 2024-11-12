package net.minecraft.gametest.framework;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class GameTestRunner {
   private static final int MAX_TESTS_PER_BATCH = 100;
   public static final int PADDING_AROUND_EACH_STRUCTURE = 2;
   public static final int SPACE_BETWEEN_COLUMNS = 5;
   public static final int SPACE_BETWEEN_ROWS = 6;
   public static final int DEFAULT_TESTS_PER_ROW = 8;

   public static void runTest(GameTestInfo pTestInfo, BlockPos pPos, GameTestTicker pTestTicker) {
      pTestInfo.startExecution();
      pTestTicker.add(pTestInfo);
      pTestInfo.addListener(new ReportGameListener(pTestInfo, pTestTicker, pPos));
      pTestInfo.spawnStructure(pPos, 2);
   }

   public static Collection<GameTestInfo> runTestBatches(Collection<GameTestBatch> pTestBatches, BlockPos pPos, Rotation pRotation, ServerLevel pServerLevel, GameTestTicker pTestTicker, int pTestsPerRow) {
      GameTestBatchRunner gametestbatchrunner = new GameTestBatchRunner(pTestBatches, pPos, pRotation, pServerLevel, pTestTicker, pTestsPerRow);
      gametestbatchrunner.start();
      return gametestbatchrunner.getTestInfos();
   }

   public static Collection<GameTestInfo> runTests(Collection<TestFunction> pTestFunctions, BlockPos pPos, Rotation pRotation, ServerLevel pServerLevel, GameTestTicker pTestTicker, int pTestsPerRow) {
      return runTestBatches(groupTestsIntoBatches(pTestFunctions), pPos, pRotation, pServerLevel, pTestTicker, pTestsPerRow);
   }

   public static Collection<GameTestBatch> groupTestsIntoBatches(Collection<TestFunction> pTestFunctions) {
      Map<String, List<TestFunction>> map = pTestFunctions.stream().collect(Collectors.groupingBy(TestFunction::getBatchName));
      return map.entrySet().stream().flatMap((p_177537_) -> {
         String s = p_177537_.getKey();
         Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(s);
         Consumer<ServerLevel> consumer1 = GameTestRegistry.getAfterBatchFunction(s);
         MutableInt mutableint = new MutableInt();
         Collection<TestFunction> collection = p_177537_.getValue();
         return Streams.stream(Iterables.partition(collection, 100)).map((p_177535_) -> {
            return new GameTestBatch(s + ":" + mutableint.incrementAndGet(), ImmutableList.copyOf(p_177535_), consumer, consumer1);
         });
      }).collect(ImmutableList.toImmutableList());
   }

   public static void clearAllTests(ServerLevel pServerLevel, BlockPos pPos, GameTestTicker pTestTicker, int pRadius) {
      pTestTicker.clear();
      BlockPos blockpos = pPos.offset(-pRadius, 0, -pRadius);
      BlockPos blockpos1 = pPos.offset(pRadius, 0, pRadius);
      BlockPos.betweenClosedStream(blockpos, blockpos1).filter((p_177540_) -> {
         return pServerLevel.getBlockState(p_177540_).is(Blocks.STRUCTURE_BLOCK);
      }).forEach((p_177529_) -> {
         StructureBlockEntity structureblockentity = (StructureBlockEntity)pServerLevel.getBlockEntity(p_177529_);
         BlockPos blockpos2 = structureblockentity.getBlockPos();
         BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(structureblockentity);
         StructureUtils.clearSpaceForStructure(boundingbox, blockpos2.getY(), pServerLevel);
      });
   }

   public static void clearMarkers(ServerLevel pServerLevel) {
      DebugPackets.sendGameTestClearPacket(pServerLevel);
   }
}