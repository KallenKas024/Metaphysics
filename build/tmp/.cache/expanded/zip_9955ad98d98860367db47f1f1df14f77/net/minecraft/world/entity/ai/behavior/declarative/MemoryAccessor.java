package net.minecraft.world.entity.ai.behavior.declarative;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;
import java.util.Optional;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public final class MemoryAccessor<F extends K1, Value> {
   private final Brain<?> brain;
   private final MemoryModuleType<Value> memoryType;
   private final App<F, Value> value;

   public MemoryAccessor(Brain<?> pBrain, MemoryModuleType<Value> pMemoryType, App<F, Value> pValue) {
      this.brain = pBrain;
      this.memoryType = pMemoryType;
      this.value = pValue;
   }

   public App<F, Value> value() {
      return this.value;
   }

   public void set(Value pValue) {
      this.brain.setMemory(this.memoryType, Optional.of(pValue));
   }

   public void setOrErase(Optional<Value> pMemory) {
      this.brain.setMemory(this.memoryType, pMemory);
   }

   public void setWithExpiry(Value pMemory, long pExpiry) {
      this.brain.setMemoryWithExpiry(this.memoryType, pMemory, pExpiry);
   }

   public void erase() {
      this.brain.eraseMemory(this.memoryType);
   }
}