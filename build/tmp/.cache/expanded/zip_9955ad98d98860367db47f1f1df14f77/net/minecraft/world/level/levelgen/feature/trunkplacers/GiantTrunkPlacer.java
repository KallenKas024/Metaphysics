package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class GiantTrunkPlacer extends TrunkPlacer {
   public static final Codec<GiantTrunkPlacer> CODEC = RecordCodecBuilder.create((p_70189_) -> {
      return trunkPlacerParts(p_70189_).apply(p_70189_, GiantTrunkPlacer::new);
   });

   public GiantTrunkPlacer(int p_70165_, int p_70166_, int p_70167_) {
      super(p_70165_, p_70166_, p_70167_);
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.GIANT_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, int pFreeTreeHeight, BlockPos pPos, TreeConfiguration pConfig) {
      BlockPos blockpos = pPos.below();
      setDirtAt(pLevel, pBlockSetter, pRandom, blockpos, pConfig);
      setDirtAt(pLevel, pBlockSetter, pRandom, blockpos.east(), pConfig);
      setDirtAt(pLevel, pBlockSetter, pRandom, blockpos.south(), pConfig);
      setDirtAt(pLevel, pBlockSetter, pRandom, blockpos.south().east(), pConfig);
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < pFreeTreeHeight; ++i) {
         this.placeLogIfFreeWithOffset(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos, pConfig, pPos, 0, i, 0);
         if (i < pFreeTreeHeight - 1) {
            this.placeLogIfFreeWithOffset(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos, pConfig, pPos, 1, i, 0);
            this.placeLogIfFreeWithOffset(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos, pConfig, pPos, 1, i, 1);
            this.placeLogIfFreeWithOffset(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos, pConfig, pPos, 0, i, 1);
         }
      }

      return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pPos.above(pFreeTreeHeight), 0, true));
   }

   private void placeLogIfFreeWithOffset(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, BlockPos.MutableBlockPos pPos, TreeConfiguration pConfig, BlockPos pOffsetPos, int pOffsetX, int pOffsetY, int pOffsetZ) {
      pPos.setWithOffset(pOffsetPos, pOffsetX, pOffsetY, pOffsetZ);
      this.placeLogIfFree(pLevel, pBlockSetter, pRandom, pPos, pConfig);
   }
}