package net.minecraft.server;

public class TickTask implements Runnable {
   private final int tick;
   private final Runnable runnable;

   public TickTask(int pTick, Runnable pRunnable) {
      this.tick = pTick;
      this.runnable = pRunnable;
   }

   /**
    * Get the server time when this task was scheduled
    */
   public int getTick() {
      return this.tick;
   }

   public void run() {
      this.runnable.run();
   }
}