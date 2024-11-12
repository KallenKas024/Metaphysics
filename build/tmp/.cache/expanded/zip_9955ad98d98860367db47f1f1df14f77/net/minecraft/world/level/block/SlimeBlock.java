package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlimeBlock extends HalfTransparentBlock {
   public SlimeBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
   }

   public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
      if (pEntity.isSuppressingBounce()) {
         super.fallOn(pLevel, pState, pPos, pEntity, pFallDistance);
      } else {
         pEntity.causeFallDamage(pFallDistance, 0.0F, pLevel.damageSources().fall());
      }

   }

   /**
    * Called when an Entity lands on this Block.
    * This method is responsible for doing any modification on the motion of the entity that should result from the
    * landing.
    */
   public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
      if (pEntity.isSuppressingBounce()) {
         super.updateEntityAfterFallOn(pLevel, pEntity);
      } else {
         this.bounceUp(pEntity);
      }

   }

   private void bounceUp(Entity pEntity) {
      Vec3 vec3 = pEntity.getDeltaMovement();
      if (vec3.y < 0.0D) {
         double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
         pEntity.setDeltaMovement(vec3.x, -vec3.y * d0, vec3.z);
      }

   }

   public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
      double d0 = Math.abs(pEntity.getDeltaMovement().y);
      if (d0 < 0.1D && !pEntity.isSteppingCarefully()) {
         double d1 = 0.4D + d0 * 0.2D;
         pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(d1, 1.0D, d1));
      }

      super.stepOn(pLevel, pPos, pState, pEntity);
   }
}