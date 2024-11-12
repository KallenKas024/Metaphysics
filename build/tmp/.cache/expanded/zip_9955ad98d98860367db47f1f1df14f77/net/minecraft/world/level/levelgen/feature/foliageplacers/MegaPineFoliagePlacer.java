package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class MegaPineFoliagePlacer extends FoliagePlacer {
   public static final Codec<MegaPineFoliagePlacer> CODEC = RecordCodecBuilder.create((p_68664_) -> {
      return foliagePlacerParts(p_68664_).and(IntProvider.codec(0, 24).fieldOf("crown_height").forGetter((p_161484_) -> {
         return p_161484_.crownHeight;
      })).apply(p_68664_, MegaPineFoliagePlacer::new);
   });
   private final IntProvider crownHeight;

   public MegaPineFoliagePlacer(IntProvider p_161470_, IntProvider p_161471_, IntProvider p_161472_) {
      super(p_161470_, p_161471_);
      this.crownHeight = p_161472_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.MEGA_PINE_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset) {
      BlockPos blockpos = pAttachment.pos();
      int i = 0;

      for(int j = blockpos.getY() - pFoliageHeight + pOffset; j <= blockpos.getY() + pOffset; ++j) {
         int k = blockpos.getY() - j;
         int l = pFoliageRadius + pAttachment.radiusOffset() + Mth.floor((float)k / (float)pFoliageHeight * 3.5F);
         int i1;
         if (k > 0 && l == i && (j & 1) == 0) {
            i1 = l + 1;
         } else {
            i1 = l;
         }

         this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, new BlockPos(blockpos.getX(), j, blockpos.getZ()), i1, 0, pAttachment.doubleTrunk());
         i = l;
      }

   }

   public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
      return this.crownHeight.sample(pRandom);
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