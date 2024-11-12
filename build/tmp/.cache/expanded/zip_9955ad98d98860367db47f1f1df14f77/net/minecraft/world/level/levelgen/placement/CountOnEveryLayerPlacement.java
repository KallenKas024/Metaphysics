package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** @deprecated */
@Deprecated
public class CountOnEveryLayerPlacement extends PlacementModifier {
   public static final Codec<CountOnEveryLayerPlacement> CODEC = IntProvider.codec(0, 256).fieldOf("count").xmap(CountOnEveryLayerPlacement::new, (p_191611_) -> {
      return p_191611_.count;
   }).codec();
   private final IntProvider count;

   private CountOnEveryLayerPlacement(IntProvider p_191603_) {
      this.count = p_191603_;
   }

   public static CountOnEveryLayerPlacement of(IntProvider pCount) {
      return new CountOnEveryLayerPlacement(pCount);
   }

   public static CountOnEveryLayerPlacement of(int pCount) {
      return of(ConstantInt.of(pCount));
   }

   public Stream<BlockPos> getPositions(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
      Stream.Builder<BlockPos> builder = Stream.builder();
      int i = 0;

      boolean flag;
      do {
         flag = false;

         for(int j = 0; j < this.count.sample(pRandom); ++j) {
            int k = pRandom.nextInt(16) + pPos.getX();
            int l = pRandom.nextInt(16) + pPos.getZ();
            int i1 = pContext.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
            int j1 = findOnGroundYPosition(pContext, k, i1, l, i);
            if (j1 != Integer.MAX_VALUE) {
               builder.add(new BlockPos(k, j1, l));
               flag = true;
            }
         }

         ++i;
      } while(flag);

      return builder.build();
   }

   public PlacementModifierType<?> type() {
      return PlacementModifierType.COUNT_ON_EVERY_LAYER;
   }

   private static int findOnGroundYPosition(PlacementContext pContext, int pX, int pY, int pZ, int p_191617_) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(pX, pY, pZ);
      int i = 0;
      BlockState blockstate = pContext.getBlockState(blockpos$mutableblockpos);

      for(int j = pY; j >= pContext.getMinBuildHeight() + 1; --j) {
         blockpos$mutableblockpos.setY(j - 1);
         BlockState blockstate1 = pContext.getBlockState(blockpos$mutableblockpos);
         if (!isEmpty(blockstate1) && isEmpty(blockstate) && !blockstate1.is(Blocks.BEDROCK)) {
            if (i == p_191617_) {
               return blockpos$mutableblockpos.getY() + 1;
            }

            ++i;
         }

         blockstate = blockstate1;
      }

      return Integer.MAX_VALUE;
   }

   private static boolean isEmpty(BlockState pState) {
      return pState.isAir() || pState.is(Blocks.WATER) || pState.is(Blocks.LAVA);
   }
}