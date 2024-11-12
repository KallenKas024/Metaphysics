package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractTreeGrower {
   /**
    * @return the key of this tree
    */
   @Nullable
   protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource pRandom, boolean pHasFlowers);

   public boolean growTree(ServerLevel pLevel, ChunkGenerator pGenerator, BlockPos pPos, BlockState pState, RandomSource pRandom) {
      ResourceKey<ConfiguredFeature<?, ?>> resourcekey = this.getConfiguredFeature(pRandom, this.hasFlowers(pLevel, pPos));
      if (resourcekey == null) {
         return false;
      } else {
         Holder<ConfiguredFeature<?, ?>> holder = pLevel.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(resourcekey).orElse((Holder.Reference<ConfiguredFeature<?, ?>>)null);
         var event = net.minecraftforge.event.ForgeEventFactory.blockGrowFeature(pLevel, pRandom, pPos, holder);
         holder = event.getFeature();
         if (event.getResult() == net.minecraftforge.eventbus.api.Event.Result.DENY) return false;
         if (holder == null) {
            return false;
         } else {
            ConfiguredFeature<?, ?> configuredfeature = holder.value();
            BlockState blockstate = pLevel.getFluidState(pPos).createLegacyBlock();
            pLevel.setBlock(pPos, blockstate, 4);
            if (configuredfeature.place(pLevel, pGenerator, pRandom, pPos)) {
               if (pLevel.getBlockState(pPos) == blockstate) {
                  pLevel.sendBlockUpdated(pPos, pState, blockstate, 2);
               }

               return true;
            } else {
               pLevel.setBlock(pPos, pState, 4);
               return false;
            }
         }
      }
   }

   private boolean hasFlowers(LevelAccessor pLevel, BlockPos pPos) {
      for(BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(pPos.below().north(2).west(2), pPos.above().south(2).east(2))) {
         if (pLevel.getBlockState(blockpos).is(BlockTags.FLOWERS)) {
            return true;
         }
      }

      return false;
   }
}
