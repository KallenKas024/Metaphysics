package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.phys.AABB;

public class UnderwaterMagmaFeature extends Feature<UnderwaterMagmaConfiguration> {
   public UnderwaterMagmaFeature(Codec<UnderwaterMagmaConfiguration> pCodec) {
      super(pCodec);
   }

   /**
    * Places the given feature at the given location.
    * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated,
    * that they can safely generate into.
    * @param pContext A context object with a reference to the level and the position the feature is being placed at
    */
   public boolean place(FeaturePlaceContext<UnderwaterMagmaConfiguration> pContext) {
      WorldGenLevel worldgenlevel = pContext.level();
      BlockPos blockpos = pContext.origin();
      UnderwaterMagmaConfiguration underwatermagmaconfiguration = pContext.config();
      RandomSource randomsource = pContext.random();
      OptionalInt optionalint = getFloorY(worldgenlevel, blockpos, underwatermagmaconfiguration);
      if (!optionalint.isPresent()) {
         return false;
      } else {
         BlockPos blockpos1 = blockpos.atY(optionalint.getAsInt());
         Vec3i vec3i = new Vec3i(underwatermagmaconfiguration.placementRadiusAroundFloor, underwatermagmaconfiguration.placementRadiusAroundFloor, underwatermagmaconfiguration.placementRadiusAroundFloor);
         AABB aabb = new AABB(blockpos1.subtract(vec3i), blockpos1.offset(vec3i));
         return BlockPos.betweenClosedStream(aabb).filter((p_225310_) -> {
            return randomsource.nextFloat() < underwatermagmaconfiguration.placementProbabilityPerValidPosition;
         }).filter((p_160584_) -> {
            return this.isValidPlacement(worldgenlevel, p_160584_);
         }).mapToInt((p_160579_) -> {
            worldgenlevel.setBlock(p_160579_, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
            return 1;
         }).sum() > 0;
      }
   }

   private static OptionalInt getFloorY(WorldGenLevel pLevel, BlockPos pPos, UnderwaterMagmaConfiguration pConfig) {
      Predicate<BlockState> predicate = (p_160586_) -> {
         return p_160586_.is(Blocks.WATER);
      };
      Predicate<BlockState> predicate1 = (p_160581_) -> {
         return !p_160581_.is(Blocks.WATER);
      };
      Optional<Column> optional = Column.scan(pLevel, pPos, pConfig.floorSearchRange, predicate, predicate1);
      return optional.map(Column::getFloor).orElseGet(OptionalInt::empty);
   }

   private boolean isValidPlacement(WorldGenLevel pLevel, BlockPos pPos) {
      if (!this.isWaterOrAir(pLevel, pPos) && !this.isWaterOrAir(pLevel, pPos.below())) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.isWaterOrAir(pLevel, pPos.relative(direction))) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean isWaterOrAir(LevelAccessor pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return blockstate.is(Blocks.WATER) || blockstate.isAir();
   }
}