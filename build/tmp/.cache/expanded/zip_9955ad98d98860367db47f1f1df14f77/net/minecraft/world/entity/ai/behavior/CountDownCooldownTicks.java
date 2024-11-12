package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownCooldownTicks extends Behavior<LivingEntity> {
   private final MemoryModuleType<Integer> cooldownTicks;

   public CountDownCooldownTicks(MemoryModuleType<Integer> pCooldownTicks) {
      super(ImmutableMap.of(pCooldownTicks, MemoryStatus.VALUE_PRESENT));
      this.cooldownTicks = pCooldownTicks;
   }

   private Optional<Integer> getCooldownTickMemory(LivingEntity pEntity) {
      return pEntity.getBrain().getMemory(this.cooldownTicks);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   protected boolean canStillUse(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      Optional<Integer> optional = this.getCooldownTickMemory(pEntity);
      return optional.isPresent() && optional.get() > 0;
   }

   protected void tick(ServerLevel pLevel, LivingEntity pOwner, long pGameTime) {
      Optional<Integer> optional = this.getCooldownTickMemory(pOwner);
      pOwner.getBrain().setMemory(this.cooldownTicks, optional.get() - 1);
   }

   protected void stop(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(this.cooldownTicks);
   }
}