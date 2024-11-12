package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityBasedExplosionDamageCalculator extends ExplosionDamageCalculator {
   private final Entity source;

   public EntityBasedExplosionDamageCalculator(Entity pSource) {
      this.source = pSource;
   }

   public Optional<Float> getBlockExplosionResistance(Explosion pExplosion, BlockGetter pReader, BlockPos pPos, BlockState pState, FluidState pFluid) {
      return super.getBlockExplosionResistance(pExplosion, pReader, pPos, pState, pFluid).map((p_45913_) -> {
         return this.source.getBlockExplosionResistance(pExplosion, pReader, pPos, pState, pFluid, p_45913_);
      });
   }

   public boolean shouldBlockExplode(Explosion pExplosion, BlockGetter pReader, BlockPos pPos, BlockState pState, float pPower) {
      return this.source.shouldBlockExplode(pExplosion, pReader, pPos, pState, pPower);
   }
}