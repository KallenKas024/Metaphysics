package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.BitSet;
import java.util.Objects;
import javax.annotation.Nullable;

public class LastSeenMessagesTracker {
   private final LastSeenTrackedEntry[] trackedMessages;
   private int tail;
   private int offset;
   @Nullable
   private MessageSignature lastTrackedMessage;

   public LastSeenMessagesTracker(int pSize) {
      this.trackedMessages = new LastSeenTrackedEntry[pSize];
   }

   public boolean addPending(MessageSignature pSignature, boolean pAcknowledged) {
      if (Objects.equals(pSignature, this.lastTrackedMessage)) {
         return false;
      } else {
         this.lastTrackedMessage = pSignature;
         this.addEntry(pAcknowledged ? new LastSeenTrackedEntry(pSignature, true) : null);
         return true;
      }
   }

   private void addEntry(@Nullable LastSeenTrackedEntry pEntry) {
      int i = this.tail;
      this.tail = (i + 1) % this.trackedMessages.length;
      ++this.offset;
      this.trackedMessages[i] = pEntry;
   }

   public void ignorePending(MessageSignature pSignature) {
      for(int i = 0; i < this.trackedMessages.length; ++i) {
         LastSeenTrackedEntry lastseentrackedentry = this.trackedMessages[i];
         if (lastseentrackedentry != null && lastseentrackedentry.pending() && pSignature.equals(lastseentrackedentry.signature())) {
            this.trackedMessages[i] = null;
            break;
         }
      }

   }

   public int getAndClearOffset() {
      int i = this.offset;
      this.offset = 0;
      return i;
   }

   public LastSeenMessagesTracker.Update generateAndApplyUpdate() {
      int i = this.getAndClearOffset();
      BitSet bitset = new BitSet(this.trackedMessages.length);
      ObjectList<MessageSignature> objectlist = new ObjectArrayList<>(this.trackedMessages.length);

      for(int j = 0; j < this.trackedMessages.length; ++j) {
         int k = (this.tail + j) % this.trackedMessages.length;
         LastSeenTrackedEntry lastseentrackedentry = this.trackedMessages[k];
         if (lastseentrackedentry != null) {
            bitset.set(j, true);
            objectlist.add(lastseentrackedentry.signature());
            this.trackedMessages[k] = lastseentrackedentry.acknowledge();
         }
      }

      LastSeenMessages lastseenmessages = new LastSeenMessages(objectlist);
      LastSeenMessages.Update lastseenmessages$update = new LastSeenMessages.Update(i, bitset);
      return new LastSeenMessagesTracker.Update(lastseenmessages, lastseenmessages$update);
   }

   public int offset() {
      return this.offset;
   }

   public static record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
   }
}