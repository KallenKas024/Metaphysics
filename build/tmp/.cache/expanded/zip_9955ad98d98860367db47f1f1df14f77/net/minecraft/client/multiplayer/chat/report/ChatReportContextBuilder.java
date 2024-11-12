package net.minecraft.client.multiplayer.chat.report;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatReportContextBuilder {
   final int leadingCount;
   private final List<ChatReportContextBuilder.Collector> activeCollectors = new ArrayList<>();

   public ChatReportContextBuilder(int pLeadingCount) {
      this.leadingCount = pLeadingCount;
   }

   public void collectAllContext(ChatLog pChatLog, IntCollection pReportedMessages, ChatReportContextBuilder.Handler pHandler) {
      IntSortedSet intsortedset = new IntRBTreeSet(pReportedMessages);

      for(int i = intsortedset.lastInt(); i >= pChatLog.start() && (this.isActive() || !intsortedset.isEmpty()); --i) {
         LoggedChatEvent $$6 = pChatLog.lookup(i);
         if ($$6 instanceof LoggedChatMessage.Player loggedchatmessage$player) {
            boolean flag = this.acceptContext(loggedchatmessage$player.message());
            if (intsortedset.remove(i)) {
               this.trackContext(loggedchatmessage$player.message());
               pHandler.accept(i, loggedchatmessage$player);
            } else if (flag) {
               pHandler.accept(i, loggedchatmessage$player);
            }
         }
      }

   }

   public void trackContext(PlayerChatMessage pLastChainMessage) {
      this.activeCollectors.add(new ChatReportContextBuilder.Collector(pLastChainMessage));
   }

   public boolean acceptContext(PlayerChatMessage pLastChainMessage) {
      boolean flag = false;
      Iterator<ChatReportContextBuilder.Collector> iterator = this.activeCollectors.iterator();

      while(iterator.hasNext()) {
         ChatReportContextBuilder.Collector chatreportcontextbuilder$collector = iterator.next();
         if (chatreportcontextbuilder$collector.accept(pLastChainMessage)) {
            flag = true;
            if (chatreportcontextbuilder$collector.isComplete()) {
               iterator.remove();
            }
         }
      }

      return flag;
   }

   public boolean isActive() {
      return !this.activeCollectors.isEmpty();
   }

   @OnlyIn(Dist.CLIENT)
   class Collector {
      private final Set<MessageSignature> lastSeenSignatures;
      private PlayerChatMessage lastChainMessage;
      private boolean collectingChain = true;
      private int count;

      Collector(PlayerChatMessage pLastChainMessage) {
         this.lastSeenSignatures = new ObjectOpenHashSet<>(pLastChainMessage.signedBody().lastSeen().entries());
         this.lastChainMessage = pLastChainMessage;
      }

      boolean accept(PlayerChatMessage pMessage) {
         if (pMessage.equals(this.lastChainMessage)) {
            return false;
         } else {
            boolean flag = this.lastSeenSignatures.remove(pMessage.signature());
            if (this.collectingChain && this.lastChainMessage.sender().equals(pMessage.sender())) {
               if (this.lastChainMessage.link().isDescendantOf(pMessage.link())) {
                  flag = true;
                  this.lastChainMessage = pMessage;
               } else {
                  this.collectingChain = false;
               }
            }

            if (flag) {
               ++this.count;
            }

            return flag;
         }
      }

      boolean isComplete() {
         return this.count >= ChatReportContextBuilder.this.leadingCount || !this.collectingChain && this.lastSeenSignatures.isEmpty();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public interface Handler {
      void accept(int pIndex, LoggedChatMessage.Player pPlayer);
   }
}