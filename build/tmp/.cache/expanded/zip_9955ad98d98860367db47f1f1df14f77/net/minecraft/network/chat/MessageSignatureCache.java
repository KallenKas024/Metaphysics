package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

public class MessageSignatureCache {
   public static final int NOT_FOUND = -1;
   private static final int DEFAULT_CAPACITY = 128;
   private final MessageSignature[] entries;

   public MessageSignatureCache(int pSize) {
      this.entries = new MessageSignature[pSize];
   }

   public static MessageSignatureCache createDefault() {
      return new MessageSignatureCache(128);
   }

   public int pack(MessageSignature pSignature) {
      for(int i = 0; i < this.entries.length; ++i) {
         if (pSignature.equals(this.entries[i])) {
            return i;
         }
      }

      return -1;
   }

   @Nullable
   public MessageSignature unpack(int pIndex) {
      return this.entries[pIndex];
   }

   public void push(PlayerChatMessage pChatMessage) {
      List<MessageSignature> list = pChatMessage.signedBody().lastSeen().entries();
      ArrayDeque<MessageSignature> arraydeque = new ArrayDeque<>(list.size() + 1);
      arraydeque.addAll(list);
      MessageSignature messagesignature = pChatMessage.signature();
      if (messagesignature != null) {
         arraydeque.add(messagesignature);
      }

      this.push(arraydeque);
   }

   @VisibleForTesting
   void push(List<MessageSignature> pChatMessages) {
      this.push(new ArrayDeque<>(pChatMessages));
   }

   private void push(ArrayDeque<MessageSignature> pDeque) {
      Set<MessageSignature> set = new ObjectOpenHashSet<>(pDeque);

      for(int i = 0; !pDeque.isEmpty() && i < this.entries.length; ++i) {
         MessageSignature messagesignature = this.entries[i];
         this.entries[i] = pDeque.removeLast();
         if (messagesignature != null && !set.contains(messagesignature)) {
            pDeque.addFirst(messagesignature);
         }
      }

   }
}