package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class MultiNoiseBiomeSource extends BiomeSource {
   private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
   public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes");
   private static final MapCodec<Holder<MultiNoiseBiomeSourceParameterList>> PRESET_CODEC = MultiNoiseBiomeSourceParameterList.CODEC.fieldOf("preset").withLifecycle(Lifecycle.stable());
   public static final Codec<MultiNoiseBiomeSource> CODEC = Codec.mapEither(DIRECT_CODEC, PRESET_CODEC).xmap(MultiNoiseBiomeSource::new, (p_275170_) -> {
      return p_275170_.parameters;
   }).codec();
   private final Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> parameters;

   private MultiNoiseBiomeSource(Either<Climate.ParameterList<Holder<Biome>>, Holder<MultiNoiseBiomeSourceParameterList>> p_275370_) {
      this.parameters = p_275370_;
   }

   public static MultiNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> pParameters) {
      return new MultiNoiseBiomeSource(Either.left(pParameters));
   }

   public static MultiNoiseBiomeSource createFromPreset(Holder<MultiNoiseBiomeSourceParameterList> pParameters) {
      return new MultiNoiseBiomeSource(Either.right(pParameters));
   }

   private Climate.ParameterList<Holder<Biome>> parameters() {
      return this.parameters.map((p_275171_) -> {
         return p_275171_;
      }, (p_275172_) -> {
         return p_275172_.value().parameters();
      });
   }

   protected Stream<Holder<Biome>> collectPossibleBiomes() {
      return this.parameters().values().stream().map(Pair::getSecond);
   }

   protected Codec<? extends BiomeSource> codec() {
      return CODEC;
   }

   public boolean stable(ResourceKey<MultiNoiseBiomeSourceParameterList> pResourceKey) {
      Optional<Holder<MultiNoiseBiomeSourceParameterList>> optional = this.parameters.right();
      return optional.isPresent() && optional.get().is(pResourceKey);
   }

   public Holder<Biome> getNoiseBiome(int pX, int pY, int pZ, Climate.Sampler pSampler) {
      return this.getNoiseBiome(pSampler.sample(pX, pY, pZ));
   }

   @VisibleForDebug
   public Holder<Biome> getNoiseBiome(Climate.TargetPoint pTargetPoint) {
      return this.parameters().findValue(pTargetPoint);
   }

   public void addDebugInfo(List<String> pInfo, BlockPos pPos, Climate.Sampler pSampler) {
      int i = QuartPos.fromBlock(pPos.getX());
      int j = QuartPos.fromBlock(pPos.getY());
      int k = QuartPos.fromBlock(pPos.getZ());
      Climate.TargetPoint climate$targetpoint = pSampler.sample(i, j, k);
      float f = Climate.unquantizeCoord(climate$targetpoint.continentalness());
      float f1 = Climate.unquantizeCoord(climate$targetpoint.erosion());
      float f2 = Climate.unquantizeCoord(climate$targetpoint.temperature());
      float f3 = Climate.unquantizeCoord(climate$targetpoint.humidity());
      float f4 = Climate.unquantizeCoord(climate$targetpoint.weirdness());
      double d0 = (double)NoiseRouterData.peaksAndValleys(f4);
      OverworldBiomeBuilder overworldbiomebuilder = new OverworldBiomeBuilder();
      pInfo.add("Biome builder PV: " + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d0) + " C: " + overworldbiomebuilder.getDebugStringForContinentalness((double)f) + " E: " + overworldbiomebuilder.getDebugStringForErosion((double)f1) + " T: " + overworldbiomebuilder.getDebugStringForTemperature((double)f2) + " H: " + overworldbiomebuilder.getDebugStringForHumidity((double)f3));
   }
}