package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockStateProvider {
   public static final Codec<BlockStateProvider> CODEC = BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE.byNameCodec().dispatch(BlockStateProvider::type, BlockStateProviderType::codec);

   public static SimpleStateProvider simple(BlockState pState) {
      return new SimpleStateProvider(pState);
   }

   public static SimpleStateProvider simple(Block pBlock) {
      return new SimpleStateProvider(pBlock.defaultBlockState());
   }

   protected abstract BlockStateProviderType<?> type();

   public abstract BlockState getState(RandomSource pRandom, BlockPos pPos);
}