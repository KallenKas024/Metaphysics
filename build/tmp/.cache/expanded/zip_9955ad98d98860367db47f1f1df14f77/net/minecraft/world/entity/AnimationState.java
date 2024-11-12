package net.minecraft.world.entity;

import java.util.function.Consumer;
import net.minecraft.util.Mth;

public class AnimationState {
   private static final long STOPPED = Long.MAX_VALUE;
   private long lastTime = Long.MAX_VALUE;
   private long accumulatedTime;

   public void start(int pTickCount) {
      this.lastTime = (long)pTickCount * 1000L / 20L;
      this.accumulatedTime = 0L;
   }

   public void startIfStopped(int pTickCount) {
      if (!this.isStarted()) {
         this.start(pTickCount);
      }

   }

   public void animateWhen(boolean pCondition, int pTickCount) {
      if (pCondition) {
         this.startIfStopped(pTickCount);
      } else {
         this.stop();
      }

   }

   public void stop() {
      this.lastTime = Long.MAX_VALUE;
   }

   public void ifStarted(Consumer<AnimationState> pAction) {
      if (this.isStarted()) {
         pAction.accept(this);
      }

   }

   public void updateTime(float pAgeInTicks, float pSpeed) {
      if (this.isStarted()) {
         long i = Mth.lfloor((double)(pAgeInTicks * 1000.0F / 20.0F));
         this.accumulatedTime += (long)((float)(i - this.lastTime) * pSpeed);
         this.lastTime = i;
      }
   }

   public long getAccumulatedTime() {
      return this.accumulatedTime;
   }

   public boolean isStarted() {
      return this.lastTime != Long.MAX_VALUE;
   }
}