package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface Fallable {
   default void onLand(Level pLevel, BlockPos pPos, BlockState pState, BlockState pReplaceableState, FallingBlockEntity pFallingBlock) {
   }

   default void onBrokenAfterFall(Level pLevel, BlockPos pPos, FallingBlockEntity pFallingBlock) {
   }

   default DamageSource getFallDamageSource(Entity pEntity) {
      return pEntity.damageSources().fallingBlock(pEntity);
   }
}