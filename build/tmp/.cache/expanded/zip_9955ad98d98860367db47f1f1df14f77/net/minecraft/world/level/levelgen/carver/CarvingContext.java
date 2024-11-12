package net.minecraft.world.level.levelgen.carver;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.WorldGenerationContext;

public class CarvingContext extends WorldGenerationContext {
   private final RegistryAccess registryAccess;
   private final NoiseChunk noiseChunk;
   private final RandomState randomState;
   private final SurfaceRules.RuleSource surfaceRule;

   public CarvingContext(NoiseBasedChunkGenerator pGenerator, RegistryAccess pRegistryAccess, LevelHeightAccessor pLevel, NoiseChunk pNoiseChunk, RandomState pRandomState, SurfaceRules.RuleSource pSurfaceRule) {
      super(pGenerator, pLevel);
      this.registryAccess = pRegistryAccess;
      this.noiseChunk = pNoiseChunk;
      this.randomState = pRandomState;
      this.surfaceRule = pSurfaceRule;
   }

   /** @deprecated */
   @Deprecated
   public Optional<BlockState> topMaterial(Function<BlockPos, Holder<Biome>> pBiomeMapper, ChunkAccess pAccess, BlockPos pPos, boolean pHasFluid) {
      return this.randomState.surfaceSystem().topMaterial(this.surfaceRule, this, pBiomeMapper, pAccess, this.noiseChunk, pPos, pHasFluid);
   }

   /** @deprecated */
   @Deprecated
   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public RandomState randomState() {
      return this.randomState;
   }
}