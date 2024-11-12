package net.minecraft.world.level.chunk;

import com.google.common.base.Stopwatch;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.slf4j.Logger;

public class ChunkGeneratorStructureState {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RandomState randomState;
   private final BiomeSource biomeSource;
   private final long levelSeed;
   private final long concentricRingsSeed;
   private final Map<Structure, List<StructurePlacement>> placementsForStructure = new Object2ObjectOpenHashMap<>();
   private final Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> ringPositions = new Object2ObjectArrayMap<>();
   private boolean hasGeneratedPositions;
   private final List<Holder<StructureSet>> possibleStructureSets;

   public static ChunkGeneratorStructureState createForFlat(RandomState pRandomState, long pLevelSeed, BiomeSource pBiomeSource, Stream<Holder<StructureSet>> pStructureSets) {
      List<Holder<StructureSet>> list = pStructureSets.filter((p_255616_) -> {
         return hasBiomesForStructureSet(p_255616_.value(), pBiomeSource);
      }).toList();
      return new ChunkGeneratorStructureState(pRandomState, pBiomeSource, pLevelSeed, 0L, list);
   }

   public static ChunkGeneratorStructureState createForNormal(RandomState pRandomState, long pSeed, BiomeSource pBiomeSource, HolderLookup<StructureSet> pStructureSetLookup) {
      List<Holder<StructureSet>> list = pStructureSetLookup.listElements().filter((p_256144_) -> {
         return hasBiomesForStructureSet(p_256144_.value(), pBiomeSource);
      }).collect(Collectors.toUnmodifiableList());
      return new ChunkGeneratorStructureState(pRandomState, pBiomeSource, pSeed, pSeed, list);
   }

   private static boolean hasBiomesForStructureSet(StructureSet pStructureSet, BiomeSource pBiomeSource) {
      Stream<Holder<Biome>> stream = pStructureSet.structures().stream().flatMap((p_255738_) -> {
         Structure structure = p_255738_.structure().value();
         return structure.biomes().stream();
      });
      return stream.anyMatch(pBiomeSource.possibleBiomes()::contains);
   }

   private ChunkGeneratorStructureState(RandomState pRandomState, BiomeSource pBiomeSource, long pLevelSeed, long pCocentricRingsSeed, List<Holder<StructureSet>> pPossibleStructureSets) {
      this.randomState = pRandomState;
      this.levelSeed = pLevelSeed;
      this.biomeSource = pBiomeSource;
      this.concentricRingsSeed = pCocentricRingsSeed;
      this.possibleStructureSets = pPossibleStructureSets;
   }

   public List<Holder<StructureSet>> possibleStructureSets() {
      return this.possibleStructureSets;
   }

   private void generatePositions() {
      Set<Holder<Biome>> set = this.biomeSource.possibleBiomes();
      this.possibleStructureSets().forEach((p_255638_) -> {
         StructureSet structureset = p_255638_.value();
         boolean flag = false;

         for(StructureSet.StructureSelectionEntry structureset$structureselectionentry : structureset.structures()) {
            Structure structure = structureset$structureselectionentry.structure().value();
            if (structure.biomes().stream().anyMatch(set::contains)) {
               this.placementsForStructure.computeIfAbsent(structure, (p_256235_) -> {
                  return new ArrayList();
               }).add(structureset.placement());
               flag = true;
            }
         }

         if (flag) {
            StructurePlacement structureplacement = structureset.placement();
            if (structureplacement instanceof ConcentricRingsStructurePlacement) {
               ConcentricRingsStructurePlacement concentricringsstructureplacement = (ConcentricRingsStructurePlacement)structureplacement;
               this.ringPositions.put(concentricringsstructureplacement, this.generateRingPositions(p_255638_, concentricringsstructureplacement));
            }
         }

      });
   }

   private CompletableFuture<List<ChunkPos>> generateRingPositions(Holder<StructureSet> pStructureSet, ConcentricRingsStructurePlacement pPlacement) {
      if (pPlacement.count() == 0) {
         return CompletableFuture.completedFuture(List.of());
      } else {
         Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
         int i = pPlacement.distance();
         int j = pPlacement.count();
         List<CompletableFuture<ChunkPos>> list = new ArrayList<>(j);
         int k = pPlacement.spread();
         HolderSet<Biome> holderset = pPlacement.preferredBiomes();
         RandomSource randomsource = RandomSource.create();
         randomsource.setSeed(this.concentricRingsSeed);
         double d0 = randomsource.nextDouble() * Math.PI * 2.0D;
         int l = 0;
         int i1 = 0;

         for(int j1 = 0; j1 < j; ++j1) {
            double d1 = (double)(4 * i + i * i1 * 6) + (randomsource.nextDouble() - 0.5D) * (double)i * 2.5D;
            int k1 = (int)Math.round(Math.cos(d0) * d1);
            int l1 = (int)Math.round(Math.sin(d0) * d1);
            RandomSource randomsource1 = randomsource.fork();
            list.add(CompletableFuture.supplyAsync(() -> {
               Pair<BlockPos, Holder<Biome>> pair = this.biomeSource.findBiomeHorizontal(SectionPos.sectionToBlockCoord(k1, 8), 0, SectionPos.sectionToBlockCoord(l1, 8), 112, holderset::contains, randomsource1, this.randomState.sampler());
               if (pair != null) {
                  BlockPos blockpos = pair.getFirst();
                  return new ChunkPos(SectionPos.blockToSectionCoord(blockpos.getX()), SectionPos.blockToSectionCoord(blockpos.getZ()));
               } else {
                  return new ChunkPos(k1, l1);
               }
            }, Util.backgroundExecutor()));
            d0 += (Math.PI * 2D) / (double)k;
            ++l;
            if (l == k) {
               ++i1;
               l = 0;
               k += 2 * k / (i1 + 1);
               k = Math.min(k, j - j1);
               d0 += randomsource.nextDouble() * Math.PI * 2.0D;
            }
         }

         return Util.sequence(list).thenApply((p_256372_) -> {
            double d2 = (double)stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0D;
            LOGGER.debug("Calculation for {} took {}s", pStructureSet, d2);
            return p_256372_;
         });
      }
   }

   public void ensureStructuresGenerated() {
      if (!this.hasGeneratedPositions) {
         this.generatePositions();
         this.hasGeneratedPositions = true;
      }

   }

   @Nullable
   public List<ChunkPos> getRingPositionsFor(ConcentricRingsStructurePlacement pPlacement) {
      this.ensureStructuresGenerated();
      CompletableFuture<List<ChunkPos>> completablefuture = this.ringPositions.get(pPlacement);
      return completablefuture != null ? completablefuture.join() : null;
   }

   public List<StructurePlacement> getPlacementsForStructure(Holder<Structure> pStructure) {
      this.ensureStructuresGenerated();
      return this.placementsForStructure.getOrDefault(pStructure.value(), List.of());
   }

   public RandomState randomState() {
      return this.randomState;
   }

   public boolean hasStructureChunkInRange(Holder<StructureSet> pStructureSet, int pX, int pZ, int pRange) {
      StructurePlacement structureplacement = pStructureSet.value().placement();

      for(int i = pX - pRange; i <= pX + pRange; ++i) {
         for(int j = pZ - pRange; j <= pZ + pRange; ++j) {
            if (structureplacement.isStructureChunk(this, i, j)) {
               return true;
            }
         }
      }

      return false;
   }

   public long getLevelSeed() {
      return this.levelSeed;
   }
}