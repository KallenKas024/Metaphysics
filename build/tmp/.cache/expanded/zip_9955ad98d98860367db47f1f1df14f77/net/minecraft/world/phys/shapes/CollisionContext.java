package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FluidState;

public interface CollisionContext {
   static CollisionContext empty() {
      return EntityCollisionContext.EMPTY;
   }

   static CollisionContext of(Entity pEntity) {
      return new EntityCollisionContext(pEntity);
   }

   boolean isDescending();

   boolean isAbove(VoxelShape pShape, BlockPos pPos, boolean pCanAscend);

   boolean isHoldingItem(Item pItem);

   boolean canStandOnFluid(FluidState p_205110_, FluidState p_205111_);
}