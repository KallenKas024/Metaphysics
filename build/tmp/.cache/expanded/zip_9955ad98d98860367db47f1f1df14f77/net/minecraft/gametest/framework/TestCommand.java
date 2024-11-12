package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;

public class TestCommand {
   private static final int DEFAULT_CLEAR_RADIUS = 200;
   private static final int MAX_CLEAR_RADIUS = 1024;
   private static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
   private static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
   private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
   private static final int SHOW_POS_DURATION_MS = 10000;
   private static final int DEFAULT_X_SIZE = 5;
   private static final int DEFAULT_Y_SIZE = 5;
   private static final int DEFAULT_Z_SIZE = 5;

   public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
      pDispatcher.register(Commands.literal("test").then(Commands.literal("runthis").executes((p_128057_) -> {
         return runNearbyTest(p_128057_.getSource());
      })).then(Commands.literal("runthese").executes((p_128055_) -> {
         return runAllNearbyTests(p_128055_.getSource());
      })).then(Commands.literal("runfailed").executes((p_128053_) -> {
         return runLastFailedTests(p_128053_.getSource(), false, 0, 8);
      }).then(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()).executes((p_128051_) -> {
         return runLastFailedTests(p_128051_.getSource(), BoolArgumentType.getBool(p_128051_, "onlyRequiredTests"), 0, 8);
      }).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_128049_) -> {
         return runLastFailedTests(p_128049_.getSource(), BoolArgumentType.getBool(p_128049_, "onlyRequiredTests"), IntegerArgumentType.getInteger(p_128049_, "rotationSteps"), 8);
      }).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((p_128047_) -> {
         return runLastFailedTests(p_128047_.getSource(), BoolArgumentType.getBool(p_128047_, "onlyRequiredTests"), IntegerArgumentType.getInteger(p_128047_, "rotationSteps"), IntegerArgumentType.getInteger(p_128047_, "testsPerRow"));
      }))))).then(Commands.literal("run").then(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()).executes((p_128045_) -> {
         return runTest(p_128045_.getSource(), TestFunctionArgument.getTestFunction(p_128045_, "testName"), 0);
      }).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_128043_) -> {
         return runTest(p_128043_.getSource(), TestFunctionArgument.getTestFunction(p_128043_, "testName"), IntegerArgumentType.getInteger(p_128043_, "rotationSteps"));
      })))).then(Commands.literal("runall").executes((p_128041_) -> {
         return runAllTests(p_128041_.getSource(), 0, 8);
      }).then(Commands.argument("testClassName", TestClassNameArgument.testClassName()).executes((p_128039_) -> {
         return runAllTestsInClass(p_128039_.getSource(), TestClassNameArgument.getTestClassName(p_128039_, "testClassName"), 0, 8);
      }).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_128037_) -> {
         return runAllTestsInClass(p_128037_.getSource(), TestClassNameArgument.getTestClassName(p_128037_, "testClassName"), IntegerArgumentType.getInteger(p_128037_, "rotationSteps"), 8);
      }).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((p_128035_) -> {
         return runAllTestsInClass(p_128035_.getSource(), TestClassNameArgument.getTestClassName(p_128035_, "testClassName"), IntegerArgumentType.getInteger(p_128035_, "rotationSteps"), IntegerArgumentType.getInteger(p_128035_, "testsPerRow"));
      })))).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_128033_) -> {
         return runAllTests(p_128033_.getSource(), IntegerArgumentType.getInteger(p_128033_, "rotationSteps"), 8);
      }).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((p_128031_) -> {
         return runAllTests(p_128031_.getSource(), IntegerArgumentType.getInteger(p_128031_, "rotationSteps"), IntegerArgumentType.getInteger(p_128031_, "testsPerRow"));
      })))).then(Commands.literal("export").then(Commands.argument("testName", StringArgumentType.word()).executes((p_128029_) -> {
         return exportTestStructure(p_128029_.getSource(), StringArgumentType.getString(p_128029_, "testName"));
      }))).then(Commands.literal("exportthis").executes((p_128027_) -> {
         return exportNearestTestStructure(p_128027_.getSource());
      })).then(Commands.literal("import").then(Commands.argument("testName", StringArgumentType.word()).executes((p_128025_) -> {
         return importTestStructure(p_128025_.getSource(), StringArgumentType.getString(p_128025_, "testName"));
      }))).then(Commands.literal("pos").executes((p_128023_) -> {
         return showPos(p_128023_.getSource(), "pos");
      }).then(Commands.argument("var", StringArgumentType.word()).executes((p_128021_) -> {
         return showPos(p_128021_.getSource(), StringArgumentType.getString(p_128021_, "var"));
      }))).then(Commands.literal("create").then(Commands.argument("testName", StringArgumentType.word()).executes((p_128019_) -> {
         return createNewStructure(p_128019_.getSource(), StringArgumentType.getString(p_128019_, "testName"), 5, 5, 5);
      }).then(Commands.argument("width", IntegerArgumentType.integer()).executes((p_128014_) -> {
         return createNewStructure(p_128014_.getSource(), StringArgumentType.getString(p_128014_, "testName"), IntegerArgumentType.getInteger(p_128014_, "width"), IntegerArgumentType.getInteger(p_128014_, "width"), IntegerArgumentType.getInteger(p_128014_, "width"));
      }).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes((p_128007_) -> {
         return createNewStructure(p_128007_.getSource(), StringArgumentType.getString(p_128007_, "testName"), IntegerArgumentType.getInteger(p_128007_, "width"), IntegerArgumentType.getInteger(p_128007_, "height"), IntegerArgumentType.getInteger(p_128007_, "depth"));
      })))))).then(Commands.literal("clearall").executes((p_128000_) -> {
         return clearAllTests(p_128000_.getSource(), 200);
      }).then(Commands.argument("radius", IntegerArgumentType.integer()).executes((p_127949_) -> {
         return clearAllTests(p_127949_.getSource(), IntegerArgumentType.getInteger(p_127949_, "radius"));
      }))));
   }

   private static int createNewStructure(CommandSourceStack pSource, String pStructureName, int pX, int pY, int pZ) {
      if (pX <= 48 && pY <= 48 && pZ <= 48) {
         ServerLevel serverlevel = pSource.getLevel();
         BlockPos blockpos = BlockPos.containing(pSource.getPosition());
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), pSource.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY(), blockpos.getZ() + 3);
         StructureUtils.createNewEmptyStructureBlock(pStructureName.toLowerCase(), blockpos1, new Vec3i(pX, pY, pZ), Rotation.NONE, serverlevel);

         for(int i = 0; i < pX; ++i) {
            for(int j = 0; j < pZ; ++j) {
               BlockPos blockpos2 = new BlockPos(blockpos1.getX() + i, blockpos1.getY() + 1, blockpos1.getZ() + j);
               Block block = Blocks.POLISHED_ANDESITE;
               BlockInput blockinput = new BlockInput(block.defaultBlockState(), Collections.emptySet(), (CompoundTag)null);
               blockinput.place(serverlevel, blockpos2, 2);
            }
         }

         StructureUtils.addCommandBlockAndButtonToStartTest(blockpos1, new BlockPos(1, 0, -1), Rotation.NONE, serverlevel);
         return 0;
      } else {
         throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
      }
   }

   private static int showPos(CommandSourceStack pSource, String pVariableName) throws CommandSyntaxException {
      BlockHitResult blockhitresult = (BlockHitResult)pSource.getPlayerOrException().pick(10.0D, 1.0F, false);
      BlockPos blockpos = blockhitresult.getBlockPos();
      ServerLevel serverlevel = pSource.getLevel();
      Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockpos, 15, serverlevel);
      if (!optional.isPresent()) {
         optional = StructureUtils.findStructureBlockContainingPos(blockpos, 200, serverlevel);
      }

      if (!optional.isPresent()) {
         pSource.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + blockpos));
         return 0;
      } else {
         StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(optional.get());
         BlockPos blockpos1 = blockpos.subtract(optional.get());
         String s = blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ();
         String s1 = structureblockentity.getStructurePath();
         Component component = Component.literal(s).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + pVariableName + " = new BlockPos(" + s + ");")));
         pSource.sendSuccess(() -> {
            return Component.literal("Position relative to " + s1 + ": ").append(component);
         }, false);
         DebugPackets.sendGameTestAddMarker(serverlevel, new BlockPos(blockpos), s, -2147418368, 10000);
         return 1;
      }
   }

   private static int runNearbyTest(CommandSourceStack pSource) {
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      ServerLevel serverlevel = pSource.getLevel();
      BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
      if (blockpos1 == null) {
         say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
         return 0;
      } else {
         GameTestRunner.clearMarkers(serverlevel);
         runTest(serverlevel, blockpos1, (MultipleTestTracker)null);
         return 1;
      }
   }

   private static int runAllNearbyTests(CommandSourceStack pSource) {
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      ServerLevel serverlevel = pSource.getLevel();
      Collection<BlockPos> collection = StructureUtils.findStructureBlocks(blockpos, 200, serverlevel);
      if (collection.isEmpty()) {
         say(serverlevel, "Couldn't find any structure blocks within 200 block radius", ChatFormatting.RED);
         return 1;
      } else {
         GameTestRunner.clearMarkers(serverlevel);
         say(pSource, "Running " + collection.size() + " tests...");
         MultipleTestTracker multipletesttracker = new MultipleTestTracker();
         collection.forEach((p_127943_) -> {
            runTest(serverlevel, p_127943_, multipletesttracker);
         });
         return 1;
      }
   }

   private static void runTest(ServerLevel pServerLevel, BlockPos pPos, @Nullable MultipleTestTracker pTracker) {
      StructureBlockEntity structureblockentity = (StructureBlockEntity)pServerLevel.getBlockEntity(pPos);
      String s = structureblockentity.getStructurePath();
      TestFunction testfunction = GameTestRegistry.getTestFunction(s);
      GameTestInfo gametestinfo = new GameTestInfo(testfunction, structureblockentity.getRotation(), pServerLevel);
      if (pTracker != null) {
         pTracker.addTestToTrack(gametestinfo);
         gametestinfo.addListener(new TestCommand.TestSummaryDisplayer(pServerLevel, pTracker));
      }

      runTestPreparation(testfunction, pServerLevel);
      AABB aabb = StructureUtils.getStructureBounds(structureblockentity);
      BlockPos blockpos = BlockPos.containing(aabb.minX, aabb.minY, aabb.minZ);
      GameTestRunner.runTest(gametestinfo, blockpos, GameTestTicker.SINGLETON);
   }

   static void showTestSummaryIfAllDone(ServerLevel pServerLevel, MultipleTestTracker pTracker) {
      if (pTracker.isDone()) {
         say(pServerLevel, "GameTest done! " + pTracker.getTotalCount() + " tests were run", ChatFormatting.WHITE);
         if (pTracker.hasFailedRequired()) {
            say(pServerLevel, pTracker.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
         } else {
            say(pServerLevel, "All required tests passed :)", ChatFormatting.GREEN);
         }

         if (pTracker.hasFailedOptional()) {
            say(pServerLevel, pTracker.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
         }
      }

   }

   private static int clearAllTests(CommandSourceStack pSource, int pRadius) {
      ServerLevel serverlevel = pSource.getLevel();
      GameTestRunner.clearMarkers(serverlevel);
      BlockPos blockpos = BlockPos.containing(pSource.getPosition().x, (double)pSource.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.containing(pSource.getPosition())).getY(), pSource.getPosition().z);
      GameTestRunner.clearAllTests(serverlevel, blockpos, GameTestTicker.SINGLETON, Mth.clamp(pRadius, 0, 1024));
      return 1;
   }

   private static int runTest(CommandSourceStack pSource, TestFunction pFunction, int pRotationSteps) {
      ServerLevel serverlevel = pSource.getLevel();
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      int i = pSource.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY();
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), i, blockpos.getZ() + 3);
      GameTestRunner.clearMarkers(serverlevel);
      runTestPreparation(pFunction, serverlevel);
      Rotation rotation = StructureUtils.getRotationForRotationSteps(pRotationSteps);
      GameTestInfo gametestinfo = new GameTestInfo(pFunction, rotation, serverlevel);
      GameTestRunner.runTest(gametestinfo, blockpos1, GameTestTicker.SINGLETON);
      return 1;
   }

   private static void runTestPreparation(TestFunction pFunction, ServerLevel pServerLevel) {
      Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(pFunction.getBatchName());
      if (consumer != null) {
         consumer.accept(pServerLevel);
      }

   }

   private static int runAllTests(CommandSourceStack pSource, int pRotationSteps, int pTestsPerRow) {
      GameTestRunner.clearMarkers(pSource.getLevel());
      Collection<TestFunction> collection = GameTestRegistry.getAllTestFunctions();
      say(pSource, "Running all " + collection.size() + " tests...");
      GameTestRegistry.forgetFailedTests();
      runTests(pSource, collection, pRotationSteps, pTestsPerRow);
      return 1;
   }

   private static int runAllTestsInClass(CommandSourceStack pSource, String pTestClassName, int pRotationSteps, int pTestsPerRow) {
      Collection<TestFunction> collection = GameTestRegistry.getTestFunctionsForClassName(pTestClassName);
      GameTestRunner.clearMarkers(pSource.getLevel());
      say(pSource, "Running " + collection.size() + " tests from " + pTestClassName + "...");
      GameTestRegistry.forgetFailedTests();
      runTests(pSource, collection, pRotationSteps, pTestsPerRow);
      return 1;
   }

   private static int runLastFailedTests(CommandSourceStack pSource, boolean pRunOnlyRequired, int pRotationSteps, int pTestsPerRow) {
      Collection<TestFunction> collection;
      if (pRunOnlyRequired) {
         collection = GameTestRegistry.getLastFailedTests().stream().filter(TestFunction::isRequired).collect(Collectors.toList());
      } else {
         collection = GameTestRegistry.getLastFailedTests();
      }

      if (collection.isEmpty()) {
         say(pSource, "No failed tests to rerun");
         return 0;
      } else {
         GameTestRunner.clearMarkers(pSource.getLevel());
         say(pSource, "Rerunning " + collection.size() + " failed tests (" + (pRunOnlyRequired ? "only required tests" : "including optional tests") + ")");
         runTests(pSource, collection, pRotationSteps, pTestsPerRow);
         return 1;
      }
   }

   private static void runTests(CommandSourceStack pSource, Collection<TestFunction> pTests, int pRotationSteps, int pTestsPerRow) {
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), pSource.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY(), blockpos.getZ() + 3);
      ServerLevel serverlevel = pSource.getLevel();
      Rotation rotation = StructureUtils.getRotationForRotationSteps(pRotationSteps);
      Collection<GameTestInfo> collection = GameTestRunner.runTests(pTests, blockpos1, rotation, serverlevel, GameTestTicker.SINGLETON, pTestsPerRow);
      MultipleTestTracker multipletesttracker = new MultipleTestTracker(collection);
      multipletesttracker.addListener(new TestCommand.TestSummaryDisplayer(serverlevel, multipletesttracker));
      multipletesttracker.addFailureListener((p_127992_) -> {
         GameTestRegistry.rememberFailedTest(p_127992_.getTestFunction());
      });
   }

   private static void say(CommandSourceStack pSource, String pMessage) {
      pSource.sendSuccess(() -> {
         return Component.literal(pMessage);
      }, false);
   }

   private static int exportNearestTestStructure(CommandSourceStack pSource) {
      BlockPos blockpos = BlockPos.containing(pSource.getPosition());
      ServerLevel serverlevel = pSource.getLevel();
      BlockPos blockpos1 = StructureUtils.findNearestStructureBlock(blockpos, 15, serverlevel);
      if (blockpos1 == null) {
         say(serverlevel, "Couldn't find any structure block within 15 radius", ChatFormatting.RED);
         return 0;
      } else {
         StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(blockpos1);
         String s = structureblockentity.getStructurePath();
         return exportTestStructure(pSource, s);
      }
   }

   private static int exportTestStructure(CommandSourceStack pSource, String pStructurePath) {
      Path path = Paths.get(StructureUtils.testStructuresDir);
      ResourceLocation resourcelocation = new ResourceLocation("minecraft", pStructurePath);
      Path path1 = pSource.getLevel().getStructureManager().getPathToGeneratedStructure(resourcelocation, ".nbt");
      Path path2 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path1, pStructurePath, path);
      if (path2 == null) {
         say(pSource, "Failed to export " + path1);
         return 1;
      } else {
         try {
            Files.createDirectories(path2.getParent());
         } catch (IOException ioexception) {
            say(pSource, "Could not create folder " + path2.getParent());
            ioexception.printStackTrace();
            return 1;
         }

         say(pSource, "Exported " + pStructurePath + " to " + path2.toAbsolutePath());
         return 0;
      }
   }

   private static int importTestStructure(CommandSourceStack pSource, String pStructurePath) {
      Path path = Paths.get(StructureUtils.testStructuresDir, pStructurePath + ".snbt");
      ResourceLocation resourcelocation = new ResourceLocation("minecraft", pStructurePath);
      Path path1 = pSource.getLevel().getStructureManager().getPathToGeneratedStructure(resourcelocation, ".nbt");

      try {
         BufferedReader bufferedreader = Files.newBufferedReader(path);
         String s = IOUtils.toString((Reader)bufferedreader);
         Files.createDirectories(path1.getParent());

         try (OutputStream outputstream = Files.newOutputStream(path1)) {
            NbtIo.writeCompressed(NbtUtils.snbtToStructure(s), outputstream);
         }

         say(pSource, "Imported to " + path1.toAbsolutePath());
         return 0;
      } catch (CommandSyntaxException | IOException ioexception) {
         System.err.println("Failed to load structure " + pStructurePath);
         ioexception.printStackTrace();
         return 1;
      }
   }

   private static void say(ServerLevel pServerLevel, String pMessage, ChatFormatting pFormatting) {
      pServerLevel.getPlayers((p_127945_) -> {
         return true;
      }).forEach((p_127990_) -> {
         p_127990_.sendSystemMessage(Component.literal(pFormatting + pMessage));
      });
   }

   static class TestSummaryDisplayer implements GameTestListener {
      private final ServerLevel level;
      private final MultipleTestTracker tracker;

      public TestSummaryDisplayer(ServerLevel pServerLevel, MultipleTestTracker pTracker) {
         this.level = pServerLevel;
         this.tracker = pTracker;
      }

      public void testStructureLoaded(GameTestInfo pTestInfo) {
      }

      public void testPassed(GameTestInfo pTestInfo) {
         TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
      }

      public void testFailed(GameTestInfo pTestInfo) {
         TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
      }
   }
}