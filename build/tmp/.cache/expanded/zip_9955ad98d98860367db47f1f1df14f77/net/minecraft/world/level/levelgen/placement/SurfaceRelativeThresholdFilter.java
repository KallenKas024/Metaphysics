package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;

public class SurfaceRelativeThresholdFilter extends PlacementFilter {
   public static final Codec<SurfaceRelativeThresholdFilter> CODEC = RecordCodecBuilder.create((p_191929_) -> {
      return p_191929_.group(Heightmap.Types.CODEC.fieldOf("heightmap").forGetter((p_191944_) -> {
         return p_191944_.heightmap;
      }), Codec.INT.optionalFieldOf("min_inclusive", Integer.valueOf(Integer.MIN_VALUE)).forGetter((p_191942_) -> {
         return p_191942_.minInclusive;
      }), Codec.INT.optionalFieldOf("max_inclusive", Integer.valueOf(Integer.MAX_VALUE)).forGetter((p_191939_) -> {
         return p_191939_.maxInclusive;
      })).apply(p_191929_, SurfaceRelativeThresholdFilter::new);
   });
   private final Heightmap.Types heightmap;
   private final int minInclusive;
   private final int maxInclusive;

   private SurfaceRelativeThresholdFilter(Heightmap.Types p_191925_, int p_191926_, int p_191927_) {
      this.heightmap = p_191925_;
      this.minInclusive = p_191926_;
      this.maxInclusive = p_191927_;
   }

   public static SurfaceRelativeThresholdFilter of(Heightmap.Types pHeightmap, int pMinInclusive, int pMaxInclusive) {
      return new SurfaceRelativeThresholdFilter(pHeightmap, pMinInclusive, pMaxInclusive);
   }

   protected boolean shouldPlace(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
      long i = (long)pContext.getHeight(this.heightmap, pPos.getX(), pPos.getZ());
      long j = i + (long)this.minInclusive;
      long k = i + (long)this.maxInclusive;
      return j <= (long)pPos.getY() && (long)pPos.getY() <= k;
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
   }
}