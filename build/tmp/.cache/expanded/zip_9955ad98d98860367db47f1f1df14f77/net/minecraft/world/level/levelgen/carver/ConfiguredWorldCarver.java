package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public record ConfiguredWorldCarver<WC extends CarverConfiguration>(WorldCarver<WC> worldCarver, WC config) {
   public static final Codec<ConfiguredWorldCarver<?>> DIRECT_CODEC = BuiltInRegistries.CARVER.byNameCodec().dispatch((p_64867_) -> {
      return p_64867_.worldCarver;
   }, WorldCarver::configuredCodec);
   public static final Codec<Holder<ConfiguredWorldCarver<?>>> CODEC = RegistryFileCodec.create(Registries.CONFIGURED_CARVER, DIRECT_CODEC);
   public static final Codec<HolderSet<ConfiguredWorldCarver<?>>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.CONFIGURED_CARVER, DIRECT_CODEC);

   public boolean isStartChunk(RandomSource pRandom) {
      return this.worldCarver.isStartChunk(this.config, pRandom);
   }

   public boolean carve(CarvingContext pContext, ChunkAccess pChunk, Function<BlockPos, Holder<Biome>> pBiomeAccessor, RandomSource pRandom, Aquifer pAquifer, ChunkPos pChunkPos, CarvingMask pCarvingMask) {
      return SharedConstants.debugVoidTerrain(pChunk.getPos()) ? false : this.worldCarver.carve(pContext, this.config, pChunk, pBiomeAccessor, pRandom, pAquifer, pChunkPos, pCarvingMask);
   }
}