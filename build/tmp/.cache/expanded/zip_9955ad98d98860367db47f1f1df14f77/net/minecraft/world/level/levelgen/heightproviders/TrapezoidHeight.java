package net.minecraft.world.level.levelgen.heightproviders;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.slf4j.Logger;

public class TrapezoidHeight extends HeightProvider {
   public static final Codec<TrapezoidHeight> CODEC = RecordCodecBuilder.create((p_162005_) -> {
      return p_162005_.group(VerticalAnchor.CODEC.fieldOf("min_inclusive").forGetter((p_162021_) -> {
         return p_162021_.minInclusive;
      }), VerticalAnchor.CODEC.fieldOf("max_inclusive").forGetter((p_162019_) -> {
         return p_162019_.maxInclusive;
      }), Codec.INT.optionalFieldOf("plateau", Integer.valueOf(0)).forGetter((p_162014_) -> {
         return p_162014_.plateau;
      })).apply(p_162005_, TrapezoidHeight::new);
   });
   private static final Logger LOGGER = LogUtils.getLogger();
   private final VerticalAnchor minInclusive;
   private final VerticalAnchor maxInclusive;
   private final int plateau;

   private TrapezoidHeight(VerticalAnchor p_162000_, VerticalAnchor p_162001_, int p_162002_) {
      this.minInclusive = p_162000_;
      this.maxInclusive = p_162001_;
      this.plateau = p_162002_;
   }

   public static TrapezoidHeight of(VerticalAnchor pMinInclusive, VerticalAnchor pMaxInclusive, int pPlateau) {
      return new TrapezoidHeight(pMinInclusive, pMaxInclusive, pPlateau);
   }

   public static TrapezoidHeight of(VerticalAnchor pMinInclusive, VerticalAnchor pMaxInclusive) {
      return of(pMinInclusive, pMaxInclusive, 0);
   }

   public int sample(RandomSource pRandom, WorldGenerationContext pContext) {
      int i = this.minInclusive.resolveY(pContext);
      int j = this.maxInclusive.resolveY(pContext);
      if (i > j) {
         LOGGER.warn("Empty height range: {}", (Object)this);
         return i;
      } else {
         int k = j - i;
         if (this.plateau >= k) {
            return Mth.randomBetweenInclusive(pRandom, i, j);
         } else {
            int l = (k - this.plateau) / 2;
            int i1 = k - l;
            return i + Mth.randomBetweenInclusive(pRandom, 0, i1) + Mth.randomBetweenInclusive(pRandom, 0, l);
         }
      }
   }

   public HeightProviderType<?> getType() {
      return HeightProviderType.TRAPEZOID;
   }

   public String toString() {
      return this.plateau == 0 ? "triangle (" + this.minInclusive + "-" + this.maxInclusive + ")" : "trapezoid(" + this.plateau + ") in [" + this.minInclusive + "-" + this.maxInclusive + "]";
   }
}