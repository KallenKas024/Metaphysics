package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class SmokerBlock extends AbstractFurnaceBlock {
   public SmokerBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
      return new SmokerBlockEntity(pPos, pState);
   }

   @Nullable
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
      return createFurnaceTicker(pLevel, pBlockEntityType, BlockEntityType.SMOKER);
   }

   /**
    * Called to open this furnace's container.
    * 
    * @see #use
    */
   protected void openContainer(Level pLevel, BlockPos pPos, Player pPlayer) {
      BlockEntity blockentity = pLevel.getBlockEntity(pPos);
      if (blockentity instanceof SmokerBlockEntity) {
         pPlayer.openMenu((MenuProvider)blockentity);
         pPlayer.awardStat(Stats.INTERACT_WITH_SMOKER);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      if (pState.getValue(LIT)) {
         double d0 = (double)pPos.getX() + 0.5D;
         double d1 = (double)pPos.getY();
         double d2 = (double)pPos.getZ() + 0.5D;
         if (pRandom.nextDouble() < 0.1D) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         pLevel.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);
      }
   }
}