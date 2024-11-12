package net.minecraft.world.entity.ai.behavior;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class DoNothing implements BehaviorControl<LivingEntity> {
   private final int minDuration;
   private final int maxDuration;
   private Behavior.Status status = Behavior.Status.STOPPED;
   private long endTimestamp;

   public DoNothing(int pMinDuration, int pMaxDuration) {
      this.minDuration = pMinDuration;
      this.maxDuration = pMaxDuration;
   }

   public Behavior.Status getStatus() {
      return this.status;
   }

   public final boolean tryStart(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      this.status = Behavior.Status.RUNNING;
      int i = this.minDuration + pLevel.getRandom().nextInt(this.maxDuration + 1 - this.minDuration);
      this.endTimestamp = pGameTime + (long)i;
      return true;
   }

   public final void tickOrStop(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      if (pGameTime > this.endTimestamp) {
         this.doStop(pLevel, pEntity, pGameTime);
      }

   }

   public final void doStop(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      this.status = Behavior.Status.STOPPED;
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }
}