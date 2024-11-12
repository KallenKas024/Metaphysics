package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class UpwardsBranchingTrunkPlacer extends TrunkPlacer {
   public static final Codec<UpwardsBranchingTrunkPlacer> CODEC = RecordCodecBuilder.create((p_259008_) -> {
      return trunkPlacerParts(p_259008_).and(p_259008_.group(IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter((p_226242_) -> {
         return p_226242_.extraBranchSteps;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("place_branch_per_log_probability").forGetter((p_226240_) -> {
         return p_226240_.placeBranchPerLogProbability;
      }), IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter((p_226238_) -> {
         return p_226238_.extraBranchLength;
      }), RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter((p_226234_) -> {
         return p_226234_.canGrowThrough;
      }))).apply(p_259008_, UpwardsBranchingTrunkPlacer::new);
   });
   private final IntProvider extraBranchSteps;
   private final float placeBranchPerLogProbability;
   private final IntProvider extraBranchLength;
   private final HolderSet<Block> canGrowThrough;

   public UpwardsBranchingTrunkPlacer(int p_226201_, int p_226202_, int p_226203_, IntProvider p_226204_, float p_226205_, IntProvider p_226206_, HolderSet<Block> p_226207_) {
      super(p_226201_, p_226202_, p_226203_);
      this.extraBranchSteps = p_226204_;
      this.placeBranchPerLogProbability = p_226205_;
      this.extraBranchLength = p_226206_;
      this.canGrowThrough = p_226207_;
   }

   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.UPWARDS_BRANCHING_TRUNK_PLACER;
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, int pFreeTreeHeight, BlockPos pPos, TreeConfiguration pConfig) {
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < pFreeTreeHeight; ++i) {
         int j = pPos.getY() + i;
         if (this.placeLog(pLevel, pBlockSetter, pRandom, blockpos$mutableblockpos.set(pPos.getX(), j, pPos.getZ()), pConfig) && i < pFreeTreeHeight - 1 && pRandom.nextFloat() < this.placeBranchPerLogProbability) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
            int k = this.extraBranchLength.sample(pRandom);
            int l = Math.max(0, k - this.extraBranchLength.sample(pRandom) - 1);
            int i1 = this.extraBranchSteps.sample(pRandom);
            this.placeBranch(pLevel, pBlockSetter, pRandom, pFreeTreeHeight, pConfig, list, blockpos$mutableblockpos, j, direction, l, i1);
         }

         if (i == pFreeTreeHeight - 1) {
            list.add(new FoliagePlacer.FoliageAttachment(blockpos$mutableblockpos.set(pPos.getX(), j + 1, pPos.getZ()), 0, false));
         }
      }

      return list;
   }

   private void placeBranch(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, int pFreeTreeHeight, TreeConfiguration pTreeConfig, List<FoliagePlacer.FoliageAttachment> pFoliageAttachments, BlockPos.MutableBlockPos pPos, int pY, Direction pDirection, int pExtraBranchLength, int pExtraBranchSteps) {
      int i = pY + pExtraBranchLength;
      int j = pPos.getX();
      int k = pPos.getZ();

      for(int l = pExtraBranchLength; l < pFreeTreeHeight && pExtraBranchSteps > 0; --pExtraBranchSteps) {
         if (l >= 1) {
            int i1 = pY + l;
            j += pDirection.getStepX();
            k += pDirection.getStepZ();
            i = i1;
            if (this.placeLog(pLevel, pBlockSetter, pRandom, pPos.set(j, i1, k), pTreeConfig)) {
               i = i1 + 1;
            }

            pFoliageAttachments.add(new FoliagePlacer.FoliageAttachment(pPos.immutable(), 0, false));
         }

         ++l;
      }

      if (i - pY > 1) {
         BlockPos blockpos = new BlockPos(j, i, k);
         pFoliageAttachments.add(new FoliagePlacer.FoliageAttachment(blockpos, 0, false));
         pFoliageAttachments.add(new FoliagePlacer.FoliageAttachment(blockpos.below(2), 0, false));
      }

   }

   protected boolean validTreePos(LevelSimulatedReader pLevel, BlockPos pPos) {
      return super.validTreePos(pLevel, pPos) || pLevel.isStateAtPosition(pPos, (p_226232_) -> {
         return p_226232_.is(this.canGrowThrough);
      });
   }
}