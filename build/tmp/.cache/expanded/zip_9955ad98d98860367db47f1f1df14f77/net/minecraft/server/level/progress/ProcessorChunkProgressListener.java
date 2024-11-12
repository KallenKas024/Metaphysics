package net.minecraft.server.level.progress;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public class ProcessorChunkProgressListener implements ChunkProgressListener {
   private final ChunkProgressListener delegate;
   private final ProcessorMailbox<Runnable> mailbox;

   private ProcessorChunkProgressListener(ChunkProgressListener pDelegate, Executor pDispatcher) {
      this.delegate = pDelegate;
      this.mailbox = ProcessorMailbox.create(pDispatcher, "progressListener");
   }

   public static ProcessorChunkProgressListener createStarted(ChunkProgressListener pDelegate, Executor pDispatcher) {
      ProcessorChunkProgressListener processorchunkprogresslistener = new ProcessorChunkProgressListener(pDelegate, pDispatcher);
      processorchunkprogresslistener.start();
      return processorchunkprogresslistener;
   }

   public void updateSpawnPos(ChunkPos pCenter) {
      this.mailbox.tell(() -> {
         this.delegate.updateSpawnPos(pCenter);
      });
   }

   public void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
      this.mailbox.tell(() -> {
         this.delegate.onStatusChange(pChunkPosition, pNewStatus);
      });
   }

   public void start() {
      this.mailbox.tell(this.delegate::start);
   }

   public void stop() {
      this.mailbox.tell(this.delegate::stop);
   }
}