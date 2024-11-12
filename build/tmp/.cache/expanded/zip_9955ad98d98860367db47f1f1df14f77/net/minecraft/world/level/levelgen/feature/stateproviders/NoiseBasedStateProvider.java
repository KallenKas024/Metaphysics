package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseBasedStateProvider extends BlockStateProvider {
   protected final long seed;
   protected final NormalNoise.NoiseParameters parameters;
   protected final float scale;
   protected final NormalNoise noise;

   protected static <P extends NoiseBasedStateProvider> Products.P3<RecordCodecBuilder.Mu<P>, Long, NormalNoise.NoiseParameters, Float> noiseCodec(RecordCodecBuilder.Instance<P> pInstance) {
      return pInstance.group(Codec.LONG.fieldOf("seed").forGetter((p_191435_) -> {
         return p_191435_.seed;
      }), NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("noise").forGetter((p_191433_) -> {
         return p_191433_.parameters;
      }), ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter((p_191428_) -> {
         return p_191428_.scale;
      }));
   }

   protected NoiseBasedStateProvider(long pSeed, NormalNoise.NoiseParameters pParameters, float pScale) {
      this.seed = pSeed;
      this.parameters = pParameters;
      this.scale = pScale;
      this.noise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource(pSeed)), pParameters);
   }

   protected double getNoiseValue(BlockPos pPos, double pDelta) {
      return this.noise.getValue((double)pPos.getX() * pDelta, (double)pPos.getY() * pDelta, (double)pPos.getZ() * pDelta);
   }
}