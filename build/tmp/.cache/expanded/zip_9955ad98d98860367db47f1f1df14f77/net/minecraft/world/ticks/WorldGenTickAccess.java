package net.minecraft.world.ticks;

import java.util.function.Function;
import net.minecraft.core.BlockPos;

public class WorldGenTickAccess<T> implements LevelTickAccess<T> {
   private final Function<BlockPos, TickContainerAccess<T>> containerGetter;

   public WorldGenTickAccess(Function<BlockPos, TickContainerAccess<T>> pContainerGetter) {
      this.containerGetter = pContainerGetter;
   }

   public boolean hasScheduledTick(BlockPos pPos, T pType) {
      return this.containerGetter.apply(pPos).hasScheduledTick(pPos, pType);
   }

   public void schedule(ScheduledTick<T> pTick) {
      this.containerGetter.apply(pTick.pos()).schedule(pTick);
   }

   public boolean willTickThisTick(BlockPos pPos, T pType) {
      return false;
   }

   public int count() {
      return 0;
   }
}