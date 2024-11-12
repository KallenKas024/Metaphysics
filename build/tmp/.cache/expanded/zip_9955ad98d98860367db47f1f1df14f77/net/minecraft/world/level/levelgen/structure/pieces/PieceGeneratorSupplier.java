package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

@FunctionalInterface
public interface PieceGeneratorSupplier<C extends FeatureConfiguration> {
   Optional<PieceGenerator<C>> createGenerator(PieceGeneratorSupplier.Context<C> pContext);

   static <C extends FeatureConfiguration> PieceGeneratorSupplier<C> simple(Predicate<PieceGeneratorSupplier.Context<C>> pPredicate, PieceGenerator<C> pPieceGenerator) {
      Optional<PieceGenerator<C>> optional = Optional.of(pPieceGenerator);
      return (p_197344_) -> {
         return pPredicate.test(p_197344_) ? optional : Optional.empty();
      };
   }

   static <C extends FeatureConfiguration> Predicate<PieceGeneratorSupplier.Context<C>> checkForBiomeOnTop(Heightmap.Types pHeightmapType) {
      return (p_197340_) -> {
         return p_197340_.validBiomeOnTop(pHeightmapType);
      };
   }

   public static record Context<C extends FeatureConfiguration>(ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, long seed, ChunkPos chunkPos, C config, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome, StructureTemplateManager structureTemplateManager, RegistryAccess registryAccess) {
      public boolean validBiomeOnTop(Heightmap.Types pHeightmapType) {
         int i = this.chunkPos.getMiddleBlockX();
         int j = this.chunkPos.getMiddleBlockZ();
         int k = this.chunkGenerator.getFirstOccupiedHeight(i, j, pHeightmapType, this.heightAccessor, this.randomState);
         Holder<Biome> holder = this.chunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j), this.randomState.sampler());
         return this.validBiome.test(holder);
      }
   }
}