package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class RandomSpreadFoliagePlacer extends FoliagePlacer {
   public static final Codec<RandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.create((p_161522_) -> {
      return foliagePlacerParts(p_161522_).and(p_161522_.group(IntProvider.codec(1, 512).fieldOf("foliage_height").forGetter((p_161537_) -> {
         return p_161537_.foliageHeight;
      }), Codec.intRange(0, 256).fieldOf("leaf_placement_attempts").forGetter((p_161524_) -> {
         return p_161524_.leafPlacementAttempts;
      }))).apply(p_161522_, RandomSpreadFoliagePlacer::new);
   });
   private final IntProvider foliageHeight;
   private final int leafPlacementAttempts;

   public RandomSpreadFoliagePlacer(IntProvider p_161506_, IntProvider p_161507_, IntProvider p_161508_, int p_161509_) {
      super(p_161506_, p_161507_);
      this.foliageHeight = p_161508_;
      this.leafPlacementAttempts = p_161509_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.RANDOM_SPREAD_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset) {
      BlockPos blockpos = pAttachment.pos();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos.mutable();

      for(int i = 0; i < this.leafPlacementAttempts; ++i) {
         blockpos$mutableblockpos.setWithOffset(blockpos, pRandom.nextInt(pFoliageRadius) - pRandom.nextInt(pFoliageRadius), pRandom.nextInt(pFoliageHeight) - pRandom.nextInt(pFoliageHeight), pRandom.nextInt(pFoliageRadius) - pRandom.nextInt(pFoliageRadius));
         tryPlaceLeaf(pLevel, pBlockSetter, pRandom, pConfig, blockpos$mutableblockpos);
      }

   }

   public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
      return this.foliageHeight.sample(pRandom);
   }

   /**
    * Skips certain positions based on the provided shape, such as rounding corners randomly.
    * The coordinates are passed in as absolute value, and should be within [0, {@code range}].
    */
   protected boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
      return false;
   }
}