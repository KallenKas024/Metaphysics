package net.minecraft.client.sounds;

import java.util.concurrent.locks.LockSupport;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The SoundEngineExecutor class is responsible for executing sound-related tasks in a separate thread.
 * <p>
 * It extends the BlockableEventLoop class, providing an event loop for managing and executing tasks.
 */
@OnlyIn(Dist.CLIENT)
public class SoundEngineExecutor extends BlockableEventLoop<Runnable> {
   private Thread thread = this.createThread();
   private volatile boolean shutdown;

   public SoundEngineExecutor() {
      super("Sound executor");
   }

   /**
    * Creates and starts a new thread for executing sound-related tasks.
    * <p>
    * @return The created thread
    */
   private Thread createThread() {
      Thread thread = new Thread(this::run);
      thread.setDaemon(true);
      thread.setName("Sound engine");
      thread.start();
      return thread;
   }

   /**
    * Wraps the given runnable task. In this case, the original runnable is returned as-is.
    * <p>
    * @return The wrapped runnable task
    * @param pRunnable The original runnable task
    */
   protected Runnable wrapRunnable(Runnable pRunnable) {
      return pRunnable;
   }

   /**
    * Determines whether the given runnable task should be run or not.
    * It depends on the shutdown state of the SoundEngineExecutor.
    * <p>
    * @return true if the task should run, false otherwise
    * @param pRunnable The runnable task
    */
   protected boolean shouldRun(Runnable pRunnable) {
      return !this.shutdown;
   }

   /**
    * {@return The currently running thread}
    */
   protected Thread getRunningThread() {
      return this.thread;
   }

   /**
    * The main run loop of the SoundEngineExecutor.
    * It continuously blocks until the shutdown state is true.
    */
   private void run() {
      while(!this.shutdown) {
         this.managedBlock(() -> {
            return this.shutdown;
         });
      }

   }

   /**
    * Waits for all tasks to complete.
    * This method uses LockSupport.park() to wait indefinitely.
    */
   protected void waitForTasks() {
      LockSupport.park("waiting for tasks");
   }

   /**
    * Flushes the SoundEngineExecutor by interrupting the thread, joining the thread, dropping all pending tasks, and
    * recreating the thread.
    * It sets the shutdown state to false to allow new tasks to be scheduled.
    */
   public void flush() {
      this.shutdown = true;
      this.thread.interrupt();

      try {
         this.thread.join();
      } catch (InterruptedException interruptedexception) {
         Thread.currentThread().interrupt();
      }

      this.dropAllTasks();
      this.shutdown = false;
      this.thread = this.createThread();
   }
}