package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAtTargetSink extends Behavior<Mob> {
   public LookAtTargetSink(int pMinDuration, int pMaxDuration) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), pMinDuration, pMaxDuration);
   }

   protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      return pEntity.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter((p_23497_) -> {
         return p_23497_.isVisibleBy(pEntity);
      }).isPresent();
   }

   protected void stop(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel pLevel, Mob pOwner, long pGameTime) {
      pOwner.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent((p_23486_) -> {
         pOwner.getLookControl().setLookAt(p_23486_.currentPosition());
      });
   }
}