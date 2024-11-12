package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WetSpongeBlock extends Block {
   public WetSpongeBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (pLevel.dimensionType().ultraWarm()) {
         pLevel.setBlock(pPos, Blocks.SPONGE.defaultBlockState(), 3);
         pLevel.levelEvent(2009, pPos, 0);
         pLevel.playSound((Player)null, pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (1.0F + pLevel.getRandom().nextFloat() * 0.2F) * 0.7F);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles).
    */
   public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
      Direction direction = Direction.getRandom(pRandom);
      if (direction != Direction.UP) {
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (!pState.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, direction.getOpposite())) {
            double d0 = (double)pPos.getX();
            double d1 = (double)pPos.getY();
            double d2 = (double)pPos.getZ();
            if (direction == Direction.DOWN) {
               d1 -= 0.05D;
               d0 += pRandom.nextDouble();
               d2 += pRandom.nextDouble();
            } else {
               d1 += pRandom.nextDouble() * 0.8D;
               if (direction.getAxis() == Direction.Axis.X) {
                  d2 += pRandom.nextDouble();
                  if (direction == Direction.EAST) {
                     ++d0;
                  } else {
                     d0 += 0.05D;
                  }
               } else {
                  d0 += pRandom.nextDouble();
                  if (direction == Direction.SOUTH) {
                     ++d2;
                  } else {
                     d2 += 0.05D;
                  }
               }
            }

            pLevel.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }
   }
}