package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaJungleFoliagePlacer extends FoliagePlacer {
   public static final Codec<MegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.create((p_68630_) -> {
      return foliagePlacerParts(p_68630_).and(Codec.intRange(0, 16).fieldOf("height").forGetter((p_161468_) -> {
         return p_161468_.height;
      })).apply(p_68630_, MegaJungleFoliagePlacer::new);
   });
   protected final int height;

   public MegaJungleFoliagePlacer(IntProvider p_161454_, IntProvider p_161455_, int p_161456_) {
      super(p_161454_, p_161455_);
      this.height = p_161456_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.MEGA_JUNGLE_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset) {
      int i = pAttachment.doubleTrunk() ? pFoliageHeight : 1 + pRandom.nextInt(2);

      for(int j = pOffset; j >= pOffset - i; --j) {
         int k = pFoliageRadius + pAttachment.radiusOffset() + 1 - j;
         this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, pAttachment.pos(), k, j, pAttachment.doubleTrunk());
      }

   }

   public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
      return this.height;
   }

   /**
    * Skips certain positions based on the provided shape, such as rounding corners randomly.
    * The coordinates are passed in as absolute value, and should be within [0, {@code range}].
    */
   protected boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
      if (pLocalX + pLocalZ >= 7) {
         return true;
      } else {
         return pLocalX * pLocalX + pLocalZ * pLocalZ > pRange * pRange;
      }
   }
}