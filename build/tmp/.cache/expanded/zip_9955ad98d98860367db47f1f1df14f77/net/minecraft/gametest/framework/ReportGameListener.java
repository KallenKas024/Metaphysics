package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
   private final GameTestInfo originalTestInfo;
   private final GameTestTicker testTicker;
   private final BlockPos structurePos;
   int attempts;
   int successes;

   public ReportGameListener(GameTestInfo pTestInfo, GameTestTicker pTestTicker, BlockPos pPos) {
      this.originalTestInfo = pTestInfo;
      this.testTicker = pTestTicker;
      this.structurePos = pPos;
      this.attempts = 0;
      this.successes = 0;
   }

   public void testStructureLoaded(GameTestInfo pTestInfo) {
      spawnBeacon(this.originalTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
      ++this.attempts;
   }

   public void testPassed(GameTestInfo pTestInfo) {
      ++this.successes;
      if (!pTestInfo.isFlaky()) {
         reportPassed(pTestInfo, pTestInfo.getTestName() + " passed! (" + pTestInfo.getRunTime() + "ms)");
      } else {
         if (this.successes >= pTestInfo.requiredSuccesses()) {
            reportPassed(pTestInfo, pTestInfo + " passed " + this.successes + " times of " + this.attempts + " attempts.");
         } else {
            say(this.originalTestInfo.getLevel(), ChatFormatting.GREEN, "Flaky test " + this.originalTestInfo + " succeeded, attempt: " + this.attempts + " successes: " + this.successes);
            this.rerunTest();
         }

      }
   }

   public void testFailed(GameTestInfo pTestInfo) {
      if (!pTestInfo.isFlaky()) {
         reportFailure(pTestInfo, pTestInfo.getError());
      } else {
         TestFunction testfunction = this.originalTestInfo.getTestFunction();
         String s = "Flaky test " + this.originalTestInfo + " failed, attempt: " + this.attempts + "/" + testfunction.getMaxAttempts();
         if (testfunction.getRequiredSuccesses() > 1) {
            s = s + ", successes: " + this.successes + " (" + testfunction.getRequiredSuccesses() + " required)";
         }

         say(this.originalTestInfo.getLevel(), ChatFormatting.YELLOW, s);
         if (pTestInfo.maxAttempts() - this.attempts + this.successes >= pTestInfo.requiredSuccesses()) {
            this.rerunTest();
         } else {
            reportFailure(pTestInfo, new ExhaustedAttemptsException(this.attempts, this.successes, pTestInfo));
         }

      }
   }

   public static void reportPassed(GameTestInfo pTestInfo, String pMessage) {
      spawnBeacon(pTestInfo, Blocks.LIME_STAINED_GLASS);
      visualizePassedTest(pTestInfo, pMessage);
   }

   private static void visualizePassedTest(GameTestInfo pTestInfo, String pMessage) {
      say(pTestInfo.getLevel(), ChatFormatting.GREEN, pMessage);
      GlobalTestReporter.onTestSuccess(pTestInfo);
   }

   protected static void reportFailure(GameTestInfo pTestInfo, Throwable pError) {
      spawnBeacon(pTestInfo, pTestInfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
      spawnLectern(pTestInfo, Util.describeError(pError));
      visualizeFailedTest(pTestInfo, pError);
   }

   protected static void visualizeFailedTest(GameTestInfo pTestInfo, Throwable pError) {
      String s = pError.getMessage() + (pError.getCause() == null ? "" : " cause: " + Util.describeError(pError.getCause()));
      String s1 = (pTestInfo.isRequired() ? "" : "(optional) ") + pTestInfo.getTestName() + " failed! " + s;
      say(pTestInfo.getLevel(), pTestInfo.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, s1);
      Throwable throwable = MoreObjects.firstNonNull(ExceptionUtils.getRootCause(pError), pError);
      if (throwable instanceof GameTestAssertPosException gametestassertposexception) {
         showRedBox(pTestInfo.getLevel(), gametestassertposexception.getAbsolutePos(), gametestassertposexception.getMessageToShowAtBlock());
      }

      GlobalTestReporter.onTestFailed(pTestInfo);
   }

   private void rerunTest() {
      this.originalTestInfo.clearStructure();
      GameTestInfo gametestinfo = new GameTestInfo(this.originalTestInfo.getTestFunction(), this.originalTestInfo.getRotation(), this.originalTestInfo.getLevel());
      gametestinfo.startExecution();
      this.testTicker.add(gametestinfo);
      gametestinfo.addListener(this);
      gametestinfo.spawnStructure(this.structurePos, 2);
   }

   protected static void spawnBeacon(GameTestInfo pTestInfo, Block pBlock) {
      ServerLevel serverlevel = pTestInfo.getLevel();
      BlockPos blockpos = pTestInfo.getStructureBlockPos();
      BlockPos blockpos1 = new BlockPos(-1, -1, -1);
      BlockPos blockpos2 = StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, pTestInfo.getRotation(), blockpos);
      serverlevel.setBlockAndUpdate(blockpos2, Blocks.BEACON.defaultBlockState().rotate(pTestInfo.getRotation()));
      BlockPos blockpos3 = blockpos2.offset(0, 1, 0);
      serverlevel.setBlockAndUpdate(blockpos3, pBlock.defaultBlockState());

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockpos4 = blockpos2.offset(i, -1, j);
            serverlevel.setBlockAndUpdate(blockpos4, Blocks.IRON_BLOCK.defaultBlockState());
         }
      }

   }

   private static void spawnLectern(GameTestInfo pTestInfo, String pMessage) {
      ServerLevel serverlevel = pTestInfo.getLevel();
      BlockPos blockpos = pTestInfo.getStructureBlockPos();
      BlockPos blockpos1 = new BlockPos(-1, 1, -1);
      BlockPos blockpos2 = StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, pTestInfo.getRotation(), blockpos);
      serverlevel.setBlockAndUpdate(blockpos2, Blocks.LECTERN.defaultBlockState().rotate(pTestInfo.getRotation()));
      BlockState blockstate = serverlevel.getBlockState(blockpos2);
      ItemStack itemstack = createBook(pTestInfo.getTestName(), pTestInfo.isRequired(), pMessage);
      LecternBlock.tryPlaceBook((Entity)null, serverlevel, blockpos2, blockstate, itemstack);
   }

   private static ItemStack createBook(String pTestName, boolean pRequired, String pMessage) {
      ItemStack itemstack = new ItemStack(Items.WRITABLE_BOOK);
      ListTag listtag = new ListTag();
      StringBuffer stringbuffer = new StringBuffer();
      Arrays.stream(pTestName.split("\\.")).forEach((p_177716_) -> {
         stringbuffer.append(p_177716_).append('\n');
      });
      if (!pRequired) {
         stringbuffer.append("(optional)\n");
      }

      stringbuffer.append("-------------------\n");
      listtag.add(StringTag.valueOf(stringbuffer + pMessage));
      itemstack.addTagElement("pages", listtag);
      return itemstack;
   }

   protected static void say(ServerLevel pServerLevel, ChatFormatting pFormatting, String pMessage) {
      pServerLevel.getPlayers((p_177705_) -> {
         return true;
      }).forEach((p_177709_) -> {
         p_177709_.sendSystemMessage(Component.literal(pMessage).withStyle(pFormatting));
      });
   }

   private static void showRedBox(ServerLevel pServerLevel, BlockPos pPos, String pDisplayMessage) {
      DebugPackets.sendGameTestAddMarker(pServerLevel, pPos, pDisplayMessage, -2130771968, Integer.MAX_VALUE);
   }
}