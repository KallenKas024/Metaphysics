package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public class CherryFoliagePlacer extends FoliagePlacer {
   public static final Codec<CherryFoliagePlacer> CODEC = RecordCodecBuilder.create((p_273246_) -> {
      return foliagePlacerParts(p_273246_).and(p_273246_.group(IntProvider.codec(4, 16).fieldOf("height").forGetter((p_273527_) -> {
         return p_273527_.height;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("wide_bottom_layer_hole_chance").forGetter((p_273760_) -> {
         return p_273760_.wideBottomLayerHoleChance;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("corner_hole_chance").forGetter((p_273020_) -> {
         return p_273020_.wideBottomLayerHoleChance;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_chance").forGetter((p_273148_) -> {
         return p_273148_.hangingLeavesChance;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("hanging_leaves_extension_chance").forGetter((p_273098_) -> {
         return p_273098_.hangingLeavesExtensionChance;
      }))).apply(p_273246_, CherryFoliagePlacer::new);
   });
   private final IntProvider height;
   private final float wideBottomLayerHoleChance;
   private final float cornerHoleChance;
   private final float hangingLeavesChance;
   private final float hangingLeavesExtensionChance;

   public CherryFoliagePlacer(IntProvider p_272646_, IntProvider p_272802_, IntProvider p_273604_, float p_272737_, float p_273720_, float p_273152_, float p_273529_) {
      super(p_272646_, p_272802_);
      this.height = p_273604_;
      this.wideBottomLayerHoleChance = p_272737_;
      this.cornerHoleChance = p_273720_;
      this.hangingLeavesChance = p_273152_;
      this.hangingLeavesExtensionChance = p_273529_;
   }

   protected FoliagePlacerType<?> type() {
      return FoliagePlacerType.CHERRY_FOLIAGE_PLACER;
   }

   protected void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset) {
      boolean flag = pAttachment.doubleTrunk();
      BlockPos blockpos = pAttachment.pos().above(pOffset);
      int i = pFoliageRadius + pAttachment.radiusOffset() - 1;
      this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, blockpos, i - 2, pFoliageHeight - 3, flag);
      this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, blockpos, i - 1, pFoliageHeight - 4, flag);

      for(int j = pFoliageHeight - 5; j >= 0; --j) {
         this.placeLeavesRow(pLevel, pBlockSetter, pRandom, pConfig, blockpos, i, j, flag);
      }

      this.placeLeavesRowWithHangingLeavesBelow(pLevel, pBlockSetter, pRandom, pConfig, blockpos, i, -1, flag, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
      this.placeLeavesRowWithHangingLeavesBelow(pLevel, pBlockSetter, pRandom, pConfig, blockpos, i - 1, -2, flag, this.hangingLeavesChance, this.hangingLeavesExtensionChance);
   }

   public int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig) {
      return this.height.sample(pRandom);
   }

   /**
    * Skips certain positions based on the provided shape, such as rounding corners randomly.
    * The coordinates are passed in as absolute value, and should be within [0, {@code range}].
    */
   protected boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
      if (pLocalY == -1 && (pLocalX == pRange || pLocalZ == pRange) && pRandom.nextFloat() < this.wideBottomLayerHoleChance) {
         return true;
      } else {
         boolean flag = pLocalX == pRange && pLocalZ == pRange;
         boolean flag1 = pRange > 2;
         if (flag1) {
            return flag || pLocalX + pLocalZ > pRange * 2 - 2 && pRandom.nextFloat() < this.cornerHoleChance;
         } else {
            return flag && pRandom.nextFloat() < this.cornerHoleChance;
         }
      }
   }
}