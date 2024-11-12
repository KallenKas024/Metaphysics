package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class ExplosionDamageCalculator {
   public Optional<Float> getBlockExplosionResistance(Explosion pExplosion, BlockGetter pReader, BlockPos pPos, BlockState pState, FluidState pFluid) {
      return pState.isAir() && pFluid.isEmpty() ? Optional.empty() : Optional.of(Math.max(pState.getExplosionResistance(pReader, pPos, pExplosion), pFluid.getExplosionResistance(pReader, pPos, pExplosion)));
   }

   public boolean shouldBlockExplode(Explosion pExplosion, BlockGetter pReader, BlockPos pPos, BlockState pState, float pPower) {
      return true;
   }
}
