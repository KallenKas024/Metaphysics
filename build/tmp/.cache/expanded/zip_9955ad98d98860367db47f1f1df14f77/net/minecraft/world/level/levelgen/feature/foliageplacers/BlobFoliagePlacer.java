package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class BlobFoliagePlacer extends FoliagePlacer {
   public static final Codec<BlobFoliagePlacer> CODEC = RecordCodecBuilder.create((p_68427_) -> {
      return blobParts(p_68427_).apply(p_68427_, BlobFoliagePlacer::new);
   });
   protected final int height;

   protected static <P extends BlobFoliagePlacer> Products.P3<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider, Integer> blobParts(RecordCodecBuilder.Instance<P> pInstance) {
      return foliagePlacerParts(pInstance).and(Codec.intRange(0, 16).fieldOf("height").forGetter((p_68412_) -> {
         return p_68412_.height;
      }));
   }

   public BlobFoliagePlacer(IntProvider p_161356_, IntProvider p_161357_, int p_161358_) {
      super(p_161356_, p_161357_);
      this.height = p_161358_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset) {
      for(int i = pOffset; i >= pOffset - pFoliageHeight; --i) {
         int j = Math.max(pFoliageRadius + pAttachment.radiusOffset() - 1 - i / 2, 0);
         this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, pAttachment.pos(), j, i, pAttachment.doubleTrunk());
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
      return pLocalX == pRange && pLocalZ == pRange && (pRandom.nextInt(2) == 0 || pLocalY == 0);
   }
}