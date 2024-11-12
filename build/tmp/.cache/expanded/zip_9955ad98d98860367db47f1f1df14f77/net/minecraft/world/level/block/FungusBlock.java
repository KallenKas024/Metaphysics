package net.minecraft.world.level.block;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungusBlock extends BushBlock implements BonemealableBlock {
   protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D);
   private static final double BONEMEAL_SUCCESS_PROBABILITY = 0.4D;
   private final Block requiredBlock;
   private final ResourceKey<ConfiguredFeature<?, ?>> feature;

   public FungusBlock(BlockBehaviour.Properties pProperties, ResourceKey<ConfiguredFeature<?, ?>> pFeature, Block pRequiredBlock) {
      super(pProperties);
      this.feature = pFeature;
      this.requiredBlock = pRequiredBlock;
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
      return pState.is(BlockTags.NYLIUM) || pState.is(Blocks.MYCELIUM) || pState.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(pState, pLevel, pPos);
   }

   private Optional<? extends Holder<ConfiguredFeature<?, ?>>> getFeature(LevelReader pLevel) {
      return pLevel.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolder(this.feature);
   }

   /**
    * @return whether bonemeal can be used on this block
    */
   public boolean isValidBonemealTarget(LevelReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      return blockstate.is(this.requiredBlock);
   }

   public boolean isBonemealSuccess(Level pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      return (double)pRandom.nextFloat() < 0.4D;
   }

   public void performBonemeal(ServerLevel pLevel, RandomSource pRandom, BlockPos pPos, BlockState pState) {
      this.getFeature(pLevel).ifPresent((p_256352_) -> {
         var event = net.minecraftforge.event.ForgeEventFactory.blockGrowFeature(pLevel, pRandom, pPos, p_256352_);
         if (event.getResult().equals(net.minecraftforge.eventbus.api.Event.Result.DENY)) return;
         event.getFeature().value().place(pLevel, pLevel.getChunkSource().getGenerator(), pRandom, pPos);
      });
   }
}
