package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public abstract class CoralFeature extends Feature<NoneFeatureConfiguration> {
   public CoralFeature(Codec<NoneFeatureConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
      RandomSource randomsource = pContext.random();
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      Optional<Block> optional = BuiltInRegistries.BLOCK.getTag(BlockTags.CORAL_BLOCKS).flatMap((p_224980_) -> {
         return p_224980_.getRandomElement(randomsource);
      }).map(Holder::value);
      return optional.isEmpty() ? false : this.placeFeature(worldgenlevel, randomsource, blockpos, optional.get().defaultBlockState());
   }

   protected abstract boolean placeFeature(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState);

   protected boolean placeCoralBlock(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      BlockPos blockpos = pPos.above();
      BlockState blockstate = pLevel.getBlockState(pPos);
      if ((blockstate.is(Blocks.WATER) || blockstate.is(BlockTags.CORALS)) && pLevel.getBlockState(blockpos).is(Blocks.WATER)) {
         pLevel.setBlock(pPos, pState, 3);
         if (pRandom.nextFloat() < 0.25F) {
            BuiltInRegistries.BLOCK.getTag(BlockTags.CORALS).flatMap((p_224972_) -> {
               return p_224972_.getRandomElement(pRandom);
            }).map(Holder::value).ifPresent((p_204720_) -> {
               pLevel.setBlock(blockpos, p_204720_.defaultBlockState(), 2);
            });
         } else if (pRandom.nextFloat() < 0.05F) {
            pLevel.setBlock(blockpos, Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, Integer.valueOf(pRandom.nextInt(4) + 1)), 2);
         }

         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (pRandom.nextFloat() < 0.2F) {
               BlockPos blockpos1 = pPos.relative(direction);
               if (pLevel.getBlockState(blockpos1).is(Blocks.WATER)) {
                  BuiltInRegistries.BLOCK.getTag(BlockTags.WALL_CORALS).flatMap((p_224965_) -> {
                     return p_224965_.getRandomElement(pRandom);
                  }).map(Holder::value).ifPresent((p_204725_) -> {
                     BlockState blockstate1 = p_204725_.defaultBlockState();
                     if (blockstate1.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        blockstate1 = blockstate1.setValue(BaseCoralWallFanBlock.FACING, direction);
                     }

                     pLevel.setBlock(blockpos1, blockstate1, 2);
                  });
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}