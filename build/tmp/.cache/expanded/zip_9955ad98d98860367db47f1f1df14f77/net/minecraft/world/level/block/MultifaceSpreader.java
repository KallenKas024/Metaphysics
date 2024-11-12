package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class MultifaceSpreader {
   public static final MultifaceSpreader.SpreadType[] DEFAULT_SPREAD_ORDER = new MultifaceSpreader.SpreadType[]{MultifaceSpreader.SpreadType.SAME_POSITION, MultifaceSpreader.SpreadType.SAME_PLANE, MultifaceSpreader.SpreadType.WRAP_AROUND};
   private final MultifaceSpreader.SpreadConfig config;

   public MultifaceSpreader(MultifaceBlock pBlock) {
      this(new MultifaceSpreader.DefaultSpreaderConfig(pBlock));
   }

   public MultifaceSpreader(MultifaceSpreader.SpreadConfig pConfig) {
      this.config = pConfig;
   }

   public boolean canSpreadInAnyDirection(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pSpreadDirection) {
      return Direction.stream().anyMatch((p_221611_) -> {
         return this.getSpreadFromFaceTowardDirection(pState, pLevel, pPos, pSpreadDirection, p_221611_, this.config::canSpreadInto).isPresent();
      });
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadFromRandomFaceTowardRandomDirection(BlockState pState, LevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
      return Direction.allShuffled(pRandom).stream().filter((p_221680_) -> {
         return this.config.canSpreadFrom(pState, p_221680_);
      }).map((p_221629_) -> {
         return this.spreadFromFaceTowardRandomDirection(pState, pLevel, pPos, p_221629_, pRandom, false);
      }).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
   }

   public long spreadAll(BlockState pState, LevelAccessor pLevel, BlockPos pPos, boolean pMarkForPostprocessing) {
      return Direction.stream().filter((p_221670_) -> {
         return this.config.canSpreadFrom(pState, p_221670_);
      }).map((p_221667_) -> {
         return this.spreadFromFaceTowardAllDirections(pState, pLevel, pPos, p_221667_, pMarkForPostprocessing);
      }).reduce(0L, Long::sum);
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardRandomDirection(BlockState pState, LevelAccessor pLevel, BlockPos pPos, Direction pSpreadDirection, RandomSource pRandom, boolean pMarkForPostprocessing) {
      return Direction.allShuffled(pRandom).stream().map((p_221677_) -> {
         return this.spreadFromFaceTowardDirection(pState, pLevel, pPos, pSpreadDirection, p_221677_, pMarkForPostprocessing);
      }).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
   }

   private long spreadFromFaceTowardAllDirections(BlockState pState, LevelAccessor pLevel, BlockPos pPos, Direction pSpreadDirection, boolean pMarkForPostprocessing) {
      return Direction.stream().map((p_221656_) -> {
         return this.spreadFromFaceTowardDirection(pState, pLevel, pPos, pSpreadDirection, p_221656_, pMarkForPostprocessing);
      }).filter(Optional::isPresent).count();
   }

   @VisibleForTesting
   public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardDirection(BlockState pState, LevelAccessor pLevel, BlockPos pPos, Direction pSpreadDirection, Direction pFace, boolean pMarkForPostprocessing) {
      return this.getSpreadFromFaceTowardDirection(pState, pLevel, pPos, pSpreadDirection, pFace, this.config::canSpreadInto).flatMap((p_221600_) -> {
         return this.spreadToFace(pLevel, p_221600_, pMarkForPostprocessing);
      });
   }

   public Optional<MultifaceSpreader.SpreadPos> getSpreadFromFaceTowardDirection(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pSpreadDirection, Direction pFace, MultifaceSpreader.SpreadPredicate pPredicate) {
      if (pFace.getAxis() == pSpreadDirection.getAxis()) {
         return Optional.empty();
      } else if (this.config.isOtherBlockValidAsSource(pState) || this.config.hasFace(pState, pSpreadDirection) && !this.config.hasFace(pState, pFace)) {
         for(MultifaceSpreader.SpreadType multifacespreader$spreadtype : this.config.getSpreadTypes()) {
            MultifaceSpreader.SpreadPos multifacespreader$spreadpos = multifacespreader$spreadtype.getSpreadPos(pPos, pFace, pSpreadDirection);
            if (pPredicate.test(pLevel, pPos, multifacespreader$spreadpos)) {
               return Optional.of(multifacespreader$spreadpos);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadToFace(LevelAccessor pLevel, MultifaceSpreader.SpreadPos pPos, boolean pMarkForPostprocessing) {
      BlockState blockstate = pLevel.getBlockState(pPos.pos());
      return this.config.placeBlock(pLevel, pPos, blockstate, pMarkForPostprocessing) ? Optional.of(pPos) : Optional.empty();
   }

   public static class DefaultSpreaderConfig implements MultifaceSpreader.SpreadConfig {
      protected MultifaceBlock block;

      public DefaultSpreaderConfig(MultifaceBlock pBlock) {
         this.block = pBlock;
      }

      @Nullable
      public BlockState getStateForPlacement(BlockState pCurrentState, BlockGetter pLevel, BlockPos pPos, Direction pLookingDirection) {
         return this.block.getStateForPlacement(pCurrentState, pLevel, pPos, pLookingDirection);
      }

      protected boolean stateCanBeReplaced(BlockGetter pLevel, BlockPos pPos, BlockPos pSpreadPos, Direction pDirection, BlockState pState) {
         return pState.isAir() || pState.is(this.block) || pState.is(Blocks.WATER) && pState.getFluidState().isSource();
      }

      public boolean canSpreadInto(BlockGetter pLevel, BlockPos pPos, MultifaceSpreader.SpreadPos pSpreadPos) {
         BlockState blockstate = pLevel.getBlockState(pSpreadPos.pos());
         return this.stateCanBeReplaced(pLevel, pPos, pSpreadPos.pos(), pSpreadPos.face(), blockstate) && this.block.isValidStateForPlacement(pLevel, blockstate, pSpreadPos.pos(), pSpreadPos.face());
      }
   }

   public interface SpreadConfig {
      @Nullable
      BlockState getStateForPlacement(BlockState pCurrentState, BlockGetter pLevel, BlockPos pPos, Direction pLookingDirection);

      boolean canSpreadInto(BlockGetter pLevel, BlockPos pPos, MultifaceSpreader.SpreadPos pSpreadPos);

      default MultifaceSpreader.SpreadType[] getSpreadTypes() {
         return MultifaceSpreader.DEFAULT_SPREAD_ORDER;
      }

      default boolean hasFace(BlockState pState, Direction pDirection) {
         return MultifaceBlock.hasFace(pState, pDirection);
      }

      default boolean isOtherBlockValidAsSource(BlockState pOtherBlock) {
         return false;
      }

      default boolean canSpreadFrom(BlockState pState, Direction pDirection) {
         return this.isOtherBlockValidAsSource(pState) || this.hasFace(pState, pDirection);
      }

      default boolean placeBlock(LevelAccessor pLevel, MultifaceSpreader.SpreadPos pPos, BlockState pState, boolean pMarkForPostprocessing) {
         BlockState blockstate = this.getStateForPlacement(pState, pLevel, pPos.pos(), pPos.face());
         if (blockstate != null) {
            if (pMarkForPostprocessing) {
               pLevel.getChunk(pPos.pos()).markPosForPostprocessing(pPos.pos());
            }

            return pLevel.setBlock(pPos.pos(), blockstate, 2);
         } else {
            return false;
         }
      }
   }

   public static record SpreadPos(BlockPos pos, Direction face) {
   }

   @FunctionalInterface
   public interface SpreadPredicate {
      boolean test(BlockGetter pLevel, BlockPos pPos, MultifaceSpreader.SpreadPos pSpreadPos);
   }

   public static enum SpreadType {
      SAME_POSITION {
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos p_221751_, Direction p_221752_, Direction p_221753_) {
            return new MultifaceSpreader.SpreadPos(p_221751_, p_221752_);
         }
      },
      SAME_PLANE {
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos p_221758_, Direction p_221759_, Direction p_221760_) {
            return new MultifaceSpreader.SpreadPos(p_221758_.relative(p_221759_), p_221760_);
         }
      },
      WRAP_AROUND {
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos p_221765_, Direction p_221766_, Direction p_221767_) {
            return new MultifaceSpreader.SpreadPos(p_221765_.relative(p_221766_).relative(p_221767_), p_221766_.getOpposite());
         }
      };

      public abstract MultifaceSpreader.SpreadPos getSpreadPos(BlockPos pPos, Direction pFace, Direction pSpreadDirection);
   }
}