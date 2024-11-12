package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MossBlock extends Block implements BonemealableBlock {
   public MossBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return pLevel.getBlockState(pPos.above()).isAir();
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      pLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap((p_258973_) -> {
         return p_258973_.getHolder(CaveFeatures.MOSS_PATCH_BONEMEAL);
      }).ifPresent((p_255669_) -> {
         p_255669_.value().place(pLevel, pLevel.getChunkSource().getGenerator(), pRandom, pPos.above());
      });
   }
}