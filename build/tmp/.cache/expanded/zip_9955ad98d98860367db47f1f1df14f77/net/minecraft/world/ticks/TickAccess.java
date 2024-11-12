package net.minecraft.world.ticks;

import net.minecraft.core.BlockPos;

public interface TickAccess<T> {
   void schedule(ScheduledTick<T> pTick);

   boolean hasScheduledTick(BlockPos pPos, T pType);

   int count();
}