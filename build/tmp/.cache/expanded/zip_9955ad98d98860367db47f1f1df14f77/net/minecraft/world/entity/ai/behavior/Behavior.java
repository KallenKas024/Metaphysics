package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public abstract class Behavior<E extends LivingEntity> implements BehaviorControl<E> {
   public static final int DEFAULT_DURATION = 60;
   protected final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
   private Behavior.Status status = Behavior.Status.STOPPED;
   private long endTimestamp;
   private final int minDuration;
   private final int maxDuration;

   public Behavior(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition) {
      this(pEntryCondition, 60);
   }

   public Behavior(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int pDuration) {
      this(pEntryCondition, pDuration, pDuration);
   }

   public Behavior(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, int pMinDuration, int pMaxDuration) {
      this.minDuration = pMinDuration;
      this.maxDuration = pMaxDuration;
      this.entryCondition = pEntryCondition;
   }

   public Behavior.Status getStatus() {
      return this.status;
   }

   public final boolean tryStart(ServerLevel pLevel, E pOwner, long pGameTime) {
      if (this.hasRequiredMemories(pOwner) && this.checkExtraStartConditions(pLevel, pOwner)) {
         this.status = Behavior.Status.RUNNING;
         int i = this.minDuration + pLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
         this.endTimestamp = pGameTime + (long)i;
         this.start(pLevel, pOwner, pGameTime);
         return true;
      } else {
         return false;
      }
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
   }

   public final void tickOrStop(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (!this.timedOut(pGameTime) && this.canStillUse(pLevel, pEntity, pGameTime)) {
         this.tick(pLevel, pEntity, pGameTime);
      } else {
         this.doStop(pLevel, pEntity, pGameTime);
      }

   }

   protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
   }

   public final void doStop(ServerLevel pLevel, E pEntity, long pGameTime) {
      this.status = Behavior.Status.STOPPED;
      this.stop(pLevel, pEntity, pGameTime);
   }

   protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
   }

   protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
      return false;
   }

   protected boolean timedOut(long pGameTime) {
      return pGameTime > this.endTimestamp;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      return true;
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }

   protected boolean hasRequiredMemories(E pOwner) {
      for(Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
         MemoryModuleType<?> memorymoduletype = entry.getKey();
         MemoryStatus memorystatus = entry.getValue();
         if (!pOwner.getBrain().checkMemory(memorymoduletype, memorystatus)) {
            return false;
         }
      }

      return true;
   }

   public static enum Status {
      STOPPED,
      RUNNING;
   }
}