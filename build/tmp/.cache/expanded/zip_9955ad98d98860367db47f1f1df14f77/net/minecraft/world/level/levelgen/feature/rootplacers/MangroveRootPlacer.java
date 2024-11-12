package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class MangroveRootPlacer extends RootPlacer {
   public static final int ROOT_WIDTH_LIMIT = 8;
   public static final int ROOT_LENGTH_LIMIT = 15;
   public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create((p_225856_) -> {
      return rootPlacerParts(p_225856_).and(MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter((p_225849_) -> {
         return p_225849_.mangroveRootPlacement;
      })).apply(p_225856_, MangroveRootPlacer::new);
   });
   private final MangroveRootPlacement mangroveRootPlacement;

   public MangroveRootPlacer(IntProvider p_225817_, BlockStateProvider p_225818_, Optional<AboveRootPlacement> p_225819_, MangroveRootPlacement p_225820_) {
      super(p_225817_, p_225818_, p_225819_);
      this.mangroveRootPlacement = p_225820_;
   }

   public boolean placeRoots(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, BlockPos pPos, BlockPos pTrunkOrigin, TreeConfiguration pTreeConfig) {
      List<BlockPos> list = Lists.newArrayList();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = pPos.mutable();

      while(blockpos$mutableblockpos.getY() < pTrunkOrigin.getY()) {
         if (!this.canPlaceRoot(pLevel, blockpos$mutableblockpos)) {
            return false;
         }

         blockpos$mutableblockpos.move(Direction.UP);
      }

      list.add(pTrunkOrigin.below());

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pTrunkOrigin.relative(direction);
         List<BlockPos> list1 = Lists.newArrayList();
         if (!this.simulateRoots(pLevel, pRandom, blockpos, direction, pTrunkOrigin, list1, 0)) {
            return false;
         }

         list.addAll(list1);
         list.add(pTrunkOrigin.relative(direction));
      }

      for(BlockPos blockpos1 : list) {
         this.placeRoot(pLevel, pBlockSetter, pRandom, blockpos1, pTreeConfig);
      }

      return true;
   }

   private boolean simulateRoots(LevelSimulatedReader pLevel, RandomSource pRandom, BlockPos pPos, Direction pDirection, BlockPos pTrunkOrigin, List<BlockPos> pRoots, int pLength) {
      int i = this.mangroveRootPlacement.maxRootLength();
      if (pLength != i && pRoots.size() <= i) {
         for(BlockPos blockpos : this.potentialRootPositions(pPos, pDirection, pRandom, pTrunkOrigin)) {
            if (this.canPlaceRoot(pLevel, blockpos)) {
               pRoots.add(blockpos);
               if (!this.simulateRoots(pLevel, pRandom, blockpos, pDirection, pTrunkOrigin, pRoots, pLength + 1)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected List<BlockPos> potentialRootPositions(BlockPos pPos, Direction pDirection, RandomSource pRandom, BlockPos pTrunkOrigin) {
      BlockPos blockpos = pPos.below();
      BlockPos blockpos1 = pPos.relative(pDirection);
      int i = pPos.distManhattan(pTrunkOrigin);
      int j = this.mangroveRootPlacement.maxRootWidth();
      float f = this.mangroveRootPlacement.randomSkewChance();
      if (i > j - 3 && i <= j) {
         return pRandom.nextFloat() < f ? List.of(blockpos, blockpos1.below()) : List.of(blockpos);
      } else if (i > j) {
         return List.of(blockpos);
      } else if (pRandom.nextFloat() < f) {
         return List.of(blockpos);
      } else {
         return pRandom.nextBoolean() ? List.of(blockpos1) : List.of(blockpos);
      }
   }

   protected boolean canPlaceRoot(LevelSimulatedReader pLevel, BlockPos pPos) {
      return super.canPlaceRoot(pLevel, pPos) || pLevel.isStateAtPosition(pPos, (p_225858_) -> {
         return p_225858_.is(this.mangroveRootPlacement.canGrowThrough());
      });
   }

   protected void placeRoot(LevelSimulatedReader pLevel, BiConsumer<BlockPos, BlockState> pBlockSetter, RandomSource pRandom, BlockPos pPos, TreeConfiguration pTreeConfig) {
      if (pLevel.isStateAtPosition(pPos, (p_225847_) -> {
         return p_225847_.is(this.mangroveRootPlacement.muddyRootsIn());
      })) {
         BlockState blockstate = this.mangroveRootPlacement.muddyRootsProvider().getState(pRandom, pPos);
         pBlockSetter.accept(pPos, this.getPotentiallyWaterloggedState(pLevel, pPos, blockstate));
      } else {
         super.placeRoot(pLevel, pBlockSetter, pRandom, pPos, pTreeConfig);
      }

   }

   protected RootPlacerType<?> type() {
      return RootPlacerType.MANGROVE_ROOT_PLACER;
   }
}