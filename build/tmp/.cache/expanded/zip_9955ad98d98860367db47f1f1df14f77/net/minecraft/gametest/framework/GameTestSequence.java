package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class GameTestSequence {
   final GameTestInfo parent;
   private final List<GameTestEvent> events = Lists.newArrayList();
   private long lastTick;

   GameTestSequence(GameTestInfo pTestInfo) {
      this.parent = pTestInfo;
      this.lastTick = pTestInfo.getTick();
   }

   public GameTestSequence thenWaitUntil(Runnable pTask) {
      this.events.add(GameTestEvent.create(pTask));
      return this;
   }

   public GameTestSequence thenWaitUntil(long pExpectedDelay, Runnable pTask) {
      this.events.add(GameTestEvent.create(pExpectedDelay, pTask));
      return this;
   }

   public GameTestSequence thenIdle(int pTick) {
      return this.thenExecuteAfter(pTick, () -> {
      });
   }

   public GameTestSequence thenExecute(Runnable pTask) {
      this.events.add(GameTestEvent.create(() -> {
         this.executeWithoutFail(pTask);
      }));
      return this;
   }

   public GameTestSequence thenExecuteAfter(int pTick, Runnable pTask) {
      this.events.add(GameTestEvent.create(() -> {
         if (this.parent.getTick() < this.lastTick + (long)pTick) {
            throw new GameTestAssertException("Waiting");
         } else {
            this.executeWithoutFail(pTask);
         }
      }));
      return this;
   }

   public GameTestSequence thenExecuteFor(int pTick, Runnable pTask) {
      this.events.add(GameTestEvent.create(() -> {
         if (this.parent.getTick() < this.lastTick + (long)pTick) {
            this.executeWithoutFail(pTask);
            throw new GameTestAssertException("Waiting");
         }
      }));
      return this;
   }

   public void thenSucceed() {
      this.events.add(GameTestEvent.create(this.parent::succeed));
   }

   public void thenFail(Supplier<Exception> pException) {
      this.events.add(GameTestEvent.create(() -> {
         this.parent.fail(pException.get());
      }));
   }

   public GameTestSequence.Condition thenTrigger() {
      GameTestSequence.Condition gametestsequence$condition = new GameTestSequence.Condition();
      this.events.add(GameTestEvent.create(() -> {
         gametestsequence$condition.trigger(this.parent.getTick());
      }));
      return gametestsequence$condition;
   }

   public void tickAndContinue(long pTick) {
      try {
         this.tick(pTick);
      } catch (GameTestAssertException gametestassertexception) {
      }

   }

   public void tickAndFailIfNotComplete(long pTicks) {
      try {
         this.tick(pTicks);
      } catch (GameTestAssertException gametestassertexception) {
         this.parent.fail(gametestassertexception);
      }

   }

   private void executeWithoutFail(Runnable pTask) {
      try {
         pTask.run();
      } catch (GameTestAssertException gametestassertexception) {
         this.parent.fail(gametestassertexception);
      }

   }

   private void tick(long pTick) {
      Iterator<GameTestEvent> iterator = this.events.iterator();

      while(iterator.hasNext()) {
         GameTestEvent gametestevent = iterator.next();
         gametestevent.assertion.run();
         iterator.remove();
         long i = pTick - this.lastTick;
         long j = this.lastTick;
         this.lastTick = pTick;
         if (gametestevent.expectedDelay != null && gametestevent.expectedDelay != i) {
            this.parent.fail(new GameTestAssertException("Succeeded in invalid tick: expected " + (j + gametestevent.expectedDelay) + ", but current tick is " + pTick));
            break;
         }
      }

   }

   public class Condition {
      private static final long NOT_TRIGGERED = -1L;
      private long triggerTime = -1L;

      void trigger(long pTriggerTime) {
         if (this.triggerTime != -1L) {
            throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
         } else {
            this.triggerTime = pTriggerTime;
         }
      }

      public void assertTriggeredThisTick() {
         long i = GameTestSequence.this.parent.getTick();
         if (this.triggerTime != i) {
            if (this.triggerTime == -1L) {
               throw new GameTestAssertException("Condition not triggered (t=" + i + ")");
            } else {
               throw new GameTestAssertException("Condition triggered at " + this.triggerTime + ", (t=" + i + ")");
            }
         }
      }
   }
}