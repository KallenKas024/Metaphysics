package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BecomePassiveIfMemoryPresent {
   public static BehaviorControl<LivingEntity> create(MemoryModuleType<?> pPacifyingMemory, int pPacifyDuration) {
      return BehaviorBuilder.create((p_259944_) -> {
         return p_259944_.group(p_259944_.registered(MemoryModuleType.ATTACK_TARGET), p_259944_.absent(MemoryModuleType.PACIFIED), p_259944_.present(pPacifyingMemory)).apply(p_259944_, p_259944_.point(() -> {
            return "[BecomePassive if " + pPacifyingMemory + " present]";
         }, (p_260120_, p_259674_, p_259822_) -> {
            return (p_260328_, p_259412_, p_259725_) -> {
               p_259674_.setWithExpiry(true, (long)pPacifyDuration);
               p_260120_.erase();
               return true;
            };
         }));
      });
   }
}