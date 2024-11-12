package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLog {
   private final LoggedChatEvent[] buffer;
   private int nextId;

   public static Codec<ChatLog> codec(int pSize) {
      return Codec.list(LoggedChatEvent.CODEC).comapFlatMap((p_274704_) -> {
         int i = p_274704_.size();
         return i > pSize ? DataResult.error(() -> {
            return "Expected: a buffer of size less than or equal to " + pSize + " but: " + i + " is greater than " + pSize;
         }) : DataResult.success(new ChatLog(pSize, p_274704_));
      }, ChatLog::loggedChatEvents);
   }

   public ChatLog(int pSize) {
      this.buffer = new LoggedChatEvent[pSize];
   }

   private ChatLog(int pSize, List<LoggedChatEvent> pEvents) {
      this.buffer = pEvents.toArray((p_253908_) -> {
         return new LoggedChatEvent[pSize];
      });
      this.nextId = pEvents.size();
   }

   private List<LoggedChatEvent> loggedChatEvents() {
      List<LoggedChatEvent> list = new ArrayList<>(this.size());

      for(int i = this.start(); i <= this.end(); ++i) {
         list.add(this.lookup(i));
      }

      return list;
   }

   public void push(LoggedChatEvent pEvent) {
      this.buffer[this.index(this.nextId++)] = pEvent;
   }

   @Nullable
   public LoggedChatEvent lookup(int pId) {
      return pId >= this.start() && pId <= this.end() ? this.buffer[this.index(pId)] : null;
   }

   private int index(int pIndex) {
      return pIndex % this.buffer.length;
   }

   public int start() {
      return Math.max(this.nextId - this.buffer.length, 0);
   }

   public int end() {
      return this.nextId - 1;
   }

   private int size() {
      return this.end() - this.start() + 1;
   }
}