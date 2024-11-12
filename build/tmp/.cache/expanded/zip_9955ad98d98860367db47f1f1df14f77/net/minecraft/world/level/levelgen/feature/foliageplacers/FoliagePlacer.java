package net.minecraft.world.level.levelgen.feature.foliageplacers;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.material.Fluids;

public abstract class FoliagePlacer {
   public static final Codec<FoliagePlacer> CODEC = BuiltInRegistries.FOLIAGE_PLACER_TYPE.byNameCodec().dispatch(FoliagePlacer::type, FoliagePlacerType::codec);
   protected final IntProvider radius;
   protected final IntProvider offset;

   protected static <P extends FoliagePlacer> Products.P2<RecordCodecBuilder.Mu<P>, IntProvider, IntProvider> foliagePlacerParts(RecordCodecBuilder.Instance<P> pInstance) {
      return pInstance.group(IntProvider.codec(0, 16).fieldOf("radius").forGetter((p_161449_) -> {
         return p_161449_.radius;
      }), IntProvider.codec(0, 16).fieldOf("offset").forGetter((p_161447_) -> {
         return p_161447_.offset;
      }));
   }

   public FoliagePlacer(IntProvider pRadius, IntProvider pOffset) {
      this.radius = pRadius;
      this.offset = pOffset;
   }

   protected abstract FoliagePlacerType<?> type();

   public void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius) {
      this.createFoliage(pLevel, pBlockSetter, pRandom, pConfig, pMaxFreeTreeHeight, pAttachment, pFoliageHeight, pFoliageRadius, this.offset(pRandom));
   }

   protected abstract void createFoliage(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pBlockSetter, RandomSource pRandom, TreeConfiguration pConfig, int pMaxFreeTreeHeight, FoliagePlacer.FoliageAttachment pAttachment, int pFoliageHeight, int pFoliageRadius, int pOffset);

   public abstract int foliageHeight(RandomSource pRandom, int pHeight, TreeConfiguration pConfig);

   public int foliageRadius(RandomSource pRandom, int pRadius) {
      return this.radius.sample(pRandom);
   }

   private int offset(RandomSource pRandom) {
      return this.offset.sample(pRandom);
   }

   /**
    * Skips certain positions based on the provided shape, such as rounding corners randomly.
    * The coordinates are passed in as absolute value, and should be within [0, {@code range}].
    */
   protected abstract boolean shouldSkipLocation(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge);

   protected boolean shouldSkipLocationSigned(RandomSource pRandom, int pLocalX, int pLocalY, int pLocalZ, int pRange, boolean pLarge) {
      int i;
      int j;
      if (pLarge) {
         i = Math.min(Math.abs(pLocalX), Math.abs(pLocalX - 1));
         j = Math.min(Math.abs(pLocalZ), Math.abs(pLocalZ - 1));
      } else {
         i = Math.abs(pLocalX);
         j = Math.abs(pLocalZ);
      }

      return this.shouldSkipLocation(pRandom, i, pLocalY, j, pRange, pLarge);
   }

   protected void placeLeavesRow(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pFoliageSetter, RandomSource pRandom, TreeConfiguration pTreeConfiguration, BlockPos pPos, int pRange, int pLocalY, boolean pLarge) {
      int i = pLarge ? 1 : 0;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int j = -pRange; j <= pRange + i; ++j) {
         for(int k = -pRange; k <= pRange + i; ++k) {
            if (!this.shouldSkipLocationSigned(pRandom, j, pLocalY, k, pRange, pLarge)) {
               blockpos$mutableblockpos.setWithOffset(pPos, j, pLocalY, k);
               tryPlaceLeaf(pLevel, pFoliageSetter, pRandom, pTreeConfiguration, blockpos$mutableblockpos);
            }
         }
      }

   }

   protected final void placeLeavesRowWithHangingLeavesBelow(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pFoliageSetter, RandomSource pRandom, TreeConfiguration pTreeConfiguration, BlockPos pPos, int pRange, int pLocalY, boolean pLarge, float pHangingLeavesChance, float pHangingLeavesExtensionChance) {
      this.placeLeavesRow(pLevel, pFoliageSetter, pRandom, pTreeConfiguration, pPos, pRange, pLocalY, pLarge);
      int i = pLarge ? 1 : 0;
      BlockPos blockpos = pPos.below();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         Direction direction1 = direction.getClockWise();
         int j = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? pRange + i : pRange;
         blockpos$mutableblockpos.setWithOffset(pPos, 0, pLocalY - 1, 0).move(direction1, j).move(direction, -pRange);
         int k = -pRange;

         while(k < pRange + i) {
            boolean flag = pFoliageSetter.isSet(blockpos$mutableblockpos.move(Direction.UP));
            blockpos$mutableblockpos.move(Direction.DOWN);
            if (flag && tryPlaceExtension(pLevel, pFoliageSetter, pRandom, pTreeConfiguration, pHangingLeavesChance, blockpos, blockpos$mutableblockpos)) {
               blockpos$mutableblockpos.move(Direction.DOWN);
               tryPlaceExtension(pLevel, pFoliageSetter, pRandom, pTreeConfiguration, pHangingLeavesExtensionChance, blockpos, blockpos$mutableblockpos);
               blockpos$mutableblockpos.move(Direction.UP);
            }

            ++k;
            blockpos$mutableblockpos.move(direction);
         }
      }

   }

   private static boolean tryPlaceExtension(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pFoliageSetter, RandomSource pRandom, TreeConfiguration pTreeConfiguration, float pExtensionChance, BlockPos pLogPos, BlockPos.MutableBlockPos pPos) {
      if (pPos.distManhattan(pLogPos) >= 7) {
         return false;
      } else {
         return pRandom.nextFloat() > pExtensionChance ? false : tryPlaceLeaf(pLevel, pFoliageSetter, pRandom, pTreeConfiguration, pPos);
      }
   }

   protected static boolean tryPlaceLeaf(LevelSimulatedReader pLevel, FoliagePlacer.FoliageSetter pFoliageSetter, RandomSource pRandom, TreeConfiguration pTreeConfiguration, BlockPos pPos) {
      if (!TreeFeature.validTreePos(pLevel, pPos)) {
         return false;
      } else {
         BlockState blockstate = pTreeConfiguration.foliageProvider.getState(pRandom, pPos);
         if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockstate = blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(pLevel.isFluidAtPosition(pPos, (p_225638_) -> {
               return p_225638_.isSourceOfType(Fluids.WATER);
            })));
         }

         pFoliageSetter.set(pPos, blockstate);
         return true;
      }
   }

   public static final class FoliageAttachment {
      private final BlockPos pos;
      private final int radiusOffset;
      private final boolean doubleTrunk;

      public FoliageAttachment(BlockPos pPos, int pRadiusOffset, boolean pDoubleTrunk) {
         this.pos = pPos;
         this.radiusOffset = pRadiusOffset;
         this.doubleTrunk = pDoubleTrunk;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public int radiusOffset() {
         return this.radiusOffset;
      }

      public boolean doubleTrunk() {
         return this.doubleTrunk;
      }
   }

   public interface FoliageSetter {
      void set(BlockPos pPos, BlockState pState);

      boolean isSet(BlockPos pPos);
   }
}