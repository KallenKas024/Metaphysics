package net.minecraft.util.thread;

public abstract class ReentrantBlockableEventLoop<R extends Runnable> extends BlockableEventLoop<R> {
   private int reentrantCount;

   public ReentrantBlockableEventLoop(String pName) {
      super(pName);
   }

   public boolean scheduleExecutables() {
      return this.runningTask() || super.scheduleExecutables();
   }

   protected boolean runningTask() {
      return this.reentrantCount != 0;
   }

   public void doRunTask(R pTask) {
      ++this.reentrantCount;

      try {
         super.doRunTask(pTask);
      } finally {
         --this.reentrantCount;
      }

   }
}