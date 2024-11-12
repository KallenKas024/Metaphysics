package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * The statuses that chunks go through during different phases of generation and loading.
 * Each status has an asynchronous task that is completed to generate a chunk, and one to load a chunk up to that
 * status.
 * Chunks are generated in sequential stages, some of which rely on nearby chunks from the previous stage. To this
 * respect, tasks define a "range" that they require chunks to be generated up to the previous stage. This is
 * responsible for the concentric squares seen in the chunk loading screen.
 */
public class ChunkStatus {
   public static final int MAX_STRUCTURE_DISTANCE = 8;
   private static final EnumSet<Heightmap.Types> PRE_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG);
   public static final EnumSet<Heightmap.Types> POST_FEATURES = EnumSet.of(Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE, Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);
   private static final ChunkStatus.LoadingTask PASSTHROUGH_LOAD_TASK = (p_281194_, p_281195_, p_281196_, p_281197_, p_281198_, p_281199_) -> {
      return CompletableFuture.completedFuture(Either.left(p_281199_));
   };
   public static final ChunkStatus EMPTY = registerSimple("empty", (ChunkStatus)null, -1, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156307_, p_156308_, p_156309_, p_156310_, p_156311_) -> {
   });
   public static final ChunkStatus STRUCTURE_STARTS = register("structure_starts", EMPTY, 0, false, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_289514_, p_289515_, p_289516_, p_289517_, p_289518_, p_289519_, p_289520_, p_289521_, p_289522_) -> {
      if (p_289516_.getServer().getWorldData().worldGenOptions().generateStructures()) {
         p_289517_.createStructures(p_289516_.registryAccess(), p_289516_.getChunkSource().getGeneratorState(), p_289516_.structureManager(), p_289522_, p_289518_);
      }

      p_289516_.onStructureStartsAvailable(p_289522_);
      return CompletableFuture.completedFuture(Either.left(p_289522_));
   }, (p_281209_, p_281210_, p_281211_, p_281212_, p_281213_, p_281214_) -> {
      p_281210_.onStructureStartsAvailable(p_281214_);
      return CompletableFuture.completedFuture(Either.left(p_281214_));
   });
   public static final ChunkStatus STRUCTURE_REFERENCES = registerSimple("structure_references", STRUCTURE_STARTS, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_196843_, p_196844_, p_196845_, p_196846_, p_196847_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_196844_, p_196846_, p_196843_, -1);
      p_196845_.createReferences(worldgenregion, p_196844_.structureManager().forWorldGenRegion(worldgenregion), p_196847_);
   });
   public static final ChunkStatus BIOMES = register("biomes", STRUCTURE_REFERENCES, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_281200_, p_281201_, p_281202_, p_281203_, p_281204_, p_281205_, p_281206_, p_281207_, p_281208_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_281202_, p_281207_, p_281200_, -1);
      return p_281203_.createBiomes(p_281201_, p_281202_.getChunkSource().randomState(), Blender.of(worldgenregion), p_281202_.structureManager().forWorldGenRegion(worldgenregion), p_281208_).thenApply((p_281193_) -> {
         return Either.left(p_281193_);
      });
   });
   public static final ChunkStatus NOISE = register("noise", BIOMES, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_281161_, p_281162_, p_281163_, p_281164_, p_281165_, p_281166_, p_281167_, p_281168_, p_281169_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_281163_, p_281168_, p_281161_, 0);
      return p_281164_.fillFromNoise(p_281162_, Blender.of(worldgenregion), p_281163_.getChunkSource().randomState(), p_281163_.structureManager().forWorldGenRegion(worldgenregion), p_281169_).thenApply((p_281218_) -> {
         if (p_281218_ instanceof ProtoChunk protochunk) {
            BelowZeroRetrogen belowzeroretrogen = protochunk.getBelowZeroRetrogen();
            if (belowzeroretrogen != null) {
               BelowZeroRetrogen.replaceOldBedrock(protochunk);
               if (belowzeroretrogen.hasBedrockHoles()) {
                  belowzeroretrogen.applyBedrockMask(protochunk);
               }
            }
         }

         return Either.left(p_281218_);
      });
   });
   public static final ChunkStatus SURFACE = registerSimple("surface", NOISE, 8, PRE_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_156247_, p_156248_, p_156249_, p_156250_, p_156251_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_156248_, p_156250_, p_156247_, 0);
      p_156249_.buildSurface(worldgenregion, p_156248_.structureManager().forWorldGenRegion(worldgenregion), p_156248_.getChunkSource().randomState(), p_156251_);
   });
   public static final ChunkStatus CARVERS = registerSimple("carvers", SURFACE, 8, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_289523_, p_289524_, p_289525_, p_289526_, p_289527_) -> {
      WorldGenRegion worldgenregion = new WorldGenRegion(p_289524_, p_289526_, p_289523_, 0);
      if (p_289527_ instanceof ProtoChunk protochunk) {
         Blender.addAroundOldChunksCarvingMaskFilter(worldgenregion, protochunk);
      }

      p_289525_.applyCarvers(worldgenregion, p_289524_.getSeed(), p_289524_.getChunkSource().randomState(), p_289524_.getBiomeManager(), p_289524_.structureManager().forWorldGenRegion(worldgenregion), p_289527_, GenerationStep.Carving.AIR);
   });
   public static final ChunkStatus FEATURES = registerSimple("features", CARVERS, 8, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_281188_, p_281189_, p_281190_, p_281191_, p_281192_) -> {
      Heightmap.primeHeightmaps(p_281192_, EnumSet.of(Heightmap.Types.MOTION_BLOCKING, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Heightmap.Types.OCEAN_FLOOR, Heightmap.Types.WORLD_SURFACE));
      WorldGenRegion worldgenregion = new WorldGenRegion(p_281189_, p_281191_, p_281188_, 1);
      p_281190_.applyBiomeDecoration(worldgenregion, p_281192_, p_281189_.structureManager().forWorldGenRegion(worldgenregion));
      Blender.generateBorderTicks(worldgenregion, p_281192_);
   });
   public static final ChunkStatus INITIALIZE_LIGHT = register("initialize_light", FEATURES, 0, false, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_281179_, p_281180_, p_281181_, p_281182_, p_281183_, p_281184_, p_281185_, p_281186_, p_281187_) -> {
      return initializeLight(p_281184_, p_281187_);
   }, (p_281155_, p_281156_, p_281157_, p_281158_, p_281159_, p_281160_) -> {
      return initializeLight(p_281158_, p_281160_);
   });
   public static final ChunkStatus LIGHT = register("light", INITIALIZE_LIGHT, 1, true, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_284904_, p_284905_, p_284906_, p_284907_, p_284908_, p_284909_, p_284910_, p_284911_, p_284912_) -> {
      return lightChunk(p_284909_, p_284912_);
   }, (p_284898_, p_284899_, p_284900_, p_284901_, p_284902_, p_284903_) -> {
      return lightChunk(p_284901_, p_284903_);
   });
   public static final ChunkStatus SPAWN = registerSimple("spawn", LIGHT, 0, POST_FEATURES, ChunkStatus.ChunkType.PROTOCHUNK, (p_196758_, p_196759_, p_196760_, p_196761_, p_196762_) -> {
      if (!p_196762_.isUpgrading()) {
         p_196760_.spawnOriginalMobs(new WorldGenRegion(p_196759_, p_196761_, p_196758_, -1));
      }

   });
   public static final ChunkStatus FULL = register("full", SPAWN, 0, false, POST_FEATURES, ChunkStatus.ChunkType.LEVELCHUNK, (p_223267_, p_223268_, p_223269_, p_223270_, p_223271_, p_223272_, p_223273_, p_223274_, p_223275_) -> {
      return p_223273_.apply(p_223275_);
   }, (p_223260_, p_223261_, p_223262_, p_223263_, p_223264_, p_223265_) -> {
      return p_223264_.apply(p_223265_);
   });
   private static final List<ChunkStatus> STATUS_BY_RANGE = ImmutableList.of(FULL, INITIALIZE_LIGHT, CARVERS, BIOMES, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS, STRUCTURE_STARTS);
   private static final IntList RANGE_BY_STATUS = Util.make(new IntArrayList(getStatusList().size()), (p_283066_) -> {
      int i = 0;

      for(int j = getStatusList().size() - 1; j >= 0; --j) {
         while(i + 1 < STATUS_BY_RANGE.size() && j <= STATUS_BY_RANGE.get(i + 1).getIndex()) {
            ++i;
         }

         p_283066_.add(0, i);
      }

   });
   private final int index;
   private final ChunkStatus parent;
   private final ChunkStatus.GenerationTask generationTask;
   private final ChunkStatus.LoadingTask loadingTask;
   private final int range;
   private final boolean hasLoadDependencies;
   private final ChunkStatus.ChunkType chunkType;
   private final EnumSet<Heightmap.Types> heightmapsAfter;

   private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> initializeLight(ThreadedLevelLightEngine pLightEngine, ChunkAccess pChunk) {
      pChunk.initializeLightSources();
      ((ProtoChunk)pChunk).setLightEngine(pLightEngine);
      boolean flag = isLighted(pChunk);
      return pLightEngine.initializeLight(pChunk, flag).thenApply(Either::left);
   }

   private static CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> lightChunk(ThreadedLevelLightEngine pLightEngine, ChunkAccess pChunk) {
      boolean flag = isLighted(pChunk);
      return pLightEngine.lightChunk(pChunk, flag).thenApply(Either::left);
   }

   private static ChunkStatus registerSimple(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Types> pHeightmaps, ChunkStatus.ChunkType pType, ChunkStatus.SimpleGenerationTask pGenerationTask) {
      return register(pKey, pParent, pTaskRange, pHeightmaps, pType, pGenerationTask);
   }

   private static ChunkStatus register(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, EnumSet<Heightmap.Types> pHeightmaps, ChunkStatus.ChunkType pType, ChunkStatus.GenerationTask pGenerationTask) {
      return register(pKey, pParent, pTaskRange, false, pHeightmaps, pType, pGenerationTask, PASSTHROUGH_LOAD_TASK);
   }

   private static ChunkStatus register(String pKey, @Nullable ChunkStatus pParent, int pTaskRange, boolean pHasLoadDependencies, EnumSet<Heightmap.Types> pHeightmaps, ChunkStatus.ChunkType pType, ChunkStatus.GenerationTask pGenerationTask, ChunkStatus.LoadingTask pLoadingTask) {
      return Registry.register(BuiltInRegistries.CHUNK_STATUS, pKey, new ChunkStatus(pParent, pTaskRange, pHasLoadDependencies, pHeightmaps, pType, pGenerationTask, pLoadingTask));
   }

   public static List<ChunkStatus> getStatusList() {
      List<ChunkStatus> list = Lists.newArrayList();

      ChunkStatus chunkstatus;
      for(chunkstatus = FULL; chunkstatus.getParent() != chunkstatus; chunkstatus = chunkstatus.getParent()) {
         list.add(chunkstatus);
      }

      list.add(chunkstatus);
      Collections.reverse(list);
      return list;
   }

   private static boolean isLighted(ChunkAccess pChunk) {
      return pChunk.getStatus().isOrAfter(LIGHT) && pChunk.isLightCorrect();
   }

   public static ChunkStatus getStatusAroundFullChunk(int pRadius) {
      if (pRadius >= STATUS_BY_RANGE.size()) {
         return EMPTY;
      } else {
         return pRadius < 0 ? FULL : STATUS_BY_RANGE.get(pRadius);
      }
   }

   public static int maxDistance() {
      return STATUS_BY_RANGE.size();
   }

   public static int getDistance(ChunkStatus pStatus) {
      return RANGE_BY_STATUS.getInt(pStatus.getIndex());
   }

   public ChunkStatus(@Nullable ChunkStatus pParent, int pRange, boolean pHasLoadDependencies, EnumSet<Heightmap.Types> pHeightmapsAfter, ChunkStatus.ChunkType pChunkType, ChunkStatus.GenerationTask pGenerationTask, ChunkStatus.LoadingTask pLoadingTask) {
      this.parent = pParent == null ? this : pParent;
      this.generationTask = pGenerationTask;
      this.loadingTask = pLoadingTask;
      this.range = pRange;
      this.hasLoadDependencies = pHasLoadDependencies;
      this.chunkType = pChunkType;
      this.heightmapsAfter = pHeightmapsAfter;
      this.index = pParent == null ? 0 : pParent.getIndex() + 1;
   }

   public int getIndex() {
      return this.index;
   }

   public ChunkStatus getParent() {
      return this.parent;
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> generate(Executor pExectutor, ServerLevel pLevel, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, List<ChunkAccess> pCache) {
      ChunkAccess chunkaccess = pCache.get(pCache.size() / 2);
      ProfiledDuration profiledduration = JvmProfiler.INSTANCE.onChunkGenerate(chunkaccess.getPos(), pLevel.dimension(), this.toString());
      return this.generationTask.doWork(this, pExectutor, pLevel, pChunkGenerator, pStructureTemplateManager, pLightEngine, pTask, pCache, chunkaccess).thenApply((p_281217_) -> {
         p_281217_.ifLeft((p_290029_) -> {
            if (p_290029_ instanceof ProtoChunk protochunk) {
               if (!protochunk.getStatus().isOrAfter(this)) {
                  protochunk.setStatus(this);
               }
            }

         });
         if (profiledduration != null) {
            profiledduration.finish();
         }

         return p_281217_;
      });
   }

   public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> load(ServerLevel pLevel, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, ChunkAccess pLoadingChunk) {
      return this.loadingTask.doWork(this, pLevel, pStructureTemplateManager, pLightEngine, pTask, pLoadingChunk);
   }

   /**
    * Distance in chunks between the edge of the center chunk and the edge of the chunk region needed for the task. The
    * task will only affect the center chunk, only reading from the chunks in the margin.
    */
   public int getRange() {
      return this.range;
   }

   public boolean hasLoadDependencies() {
      return this.hasLoadDependencies;
   }

   public ChunkStatus.ChunkType getChunkType() {
      return this.chunkType;
   }

   public static ChunkStatus byName(String pKey) {
      return BuiltInRegistries.CHUNK_STATUS.get(ResourceLocation.tryParse(pKey));
   }

   public EnumSet<Heightmap.Types> heightmapsAfter() {
      return this.heightmapsAfter;
   }

   public boolean isOrAfter(ChunkStatus pStatus) {
      return this.getIndex() >= pStatus.getIndex();
   }

   public String toString() {
      return BuiltInRegistries.CHUNK_STATUS.getKey(this).toString();
   }

   public static enum ChunkType {
      PROTOCHUNK,
      LEVELCHUNK;
   }

   interface GenerationTask {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus pStatus, Executor pExecutor, ServerLevel pLevel, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, List<ChunkAccess> pCache, ChunkAccess pLoadingChunk);
   }

   interface LoadingTask {
      CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus pStatus, ServerLevel pLevel, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, ChunkAccess pChunk);
   }

   /**
    * A {@link GenerationTask} which completes all work synchronously.
    */
   interface SimpleGenerationTask extends ChunkStatus.GenerationTask {
      default CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> doWork(ChunkStatus pStatus, Executor pExecutor, ServerLevel pLevel, ChunkGenerator pChunkGenerator, StructureTemplateManager pStructureTemplateManager, ThreadedLevelLightEngine pLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> pTask, List<ChunkAccess> pCache, ChunkAccess pLoadingChunk) {
         this.doWork(pStatus, pLevel, pChunkGenerator, pCache, pLoadingChunk);
         return CompletableFuture.completedFuture(Either.left(pLoadingChunk));
      }

      void doWork(ChunkStatus pStatus, ServerLevel pLevel, ChunkGenerator pGenerator, List<ChunkAccess> pCache, ChunkAccess pLoadingChunk);
   }
}