package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class WaterloggedVegetationPatchFeature extends VegetationPatchFeature {
   public WaterloggedVegetationPatchFeature(Codec<VegetationPatchConfiguration> pCodec) {
      super(pCodec);
   }

   protected Set<BlockPos> placeGroundPatch(WorldGenLevel pLevel, VegetationPatchConfiguration pConfig, RandomSource pRandom, BlockPos pPos, Predicate<BlockState> pState, int pXRadius, int pZRadius) {
      Set<BlockPos> set = super.placeGroundPatch(pLevel, pConfig, pRandom, pPos, pState, pXRadius, pZRadius);
      Set<BlockPos> set1 = new HashSet<>();
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(BlockPos blockpos : set) {
         if (!isExposed(pLevel, set, blockpos, blockpos$mutableblockpos)) {
            set1.add(blockpos);
         }
      }

      for(BlockPos blockpos1 : set1) {
         pLevel.setBlock(blockpos1, Blocks.WATER.defaultBlockState(), 2);
      }

      return set1;
   }

   private static boolean isExposed(WorldGenLevel pLevel, Set<BlockPos> pPositions, BlockPos pPos, BlockPos.MutableBlockPos pMutablePos) {
      return isExposedDirection(pLevel, pPos, pMutablePos, Direction.NORTH) || isExposedDirection(pLevel, pPos, pMutablePos, Direction.EAST) || isExposedDirection(pLevel, pPos, pMutablePos, Direction.SOUTH) || isExposedDirection(pLevel, pPos, pMutablePos, Direction.WEST) || isExposedDirection(pLevel, pPos, pMutablePos, Direction.DOWN);
   }

   private static boolean isExposedDirection(WorldGenLevel pLevel, BlockPos pPos, BlockPos.MutableBlockPos pMutablePos, Direction pDirection) {
      pMutablePos.setWithOffset(pPos, pDirection);
      return !pLevel.getBlockState(pMutablePos).isFaceSturdy(pLevel, pMutablePos, pDirection.getOpposite());
   }

   protected boolean placeVegetation(WorldGenLevel pLevel, VegetationPatchConfiguration pConfig, ChunkGenerator pChunkGenerator, RandomSource pRandom, BlockPos pPos) {
      if (super.placeVegetation(pLevel, pConfig, pChunkGenerator, pRandom, pPos.below())) {
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && !blockstate.getValue(BlockStateProperties.WATERLOGGED)) {
            pLevel.setBlock(pPos, blockstate.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 2);
         }

         return true;
      } else {
         return false;
      }
   }
}