package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundEditBookPacket implements Packet<ServerGamePacketListener> {
   public static final int MAX_BYTES_PER_CHAR = 4;
   private static final int TITLE_MAX_CHARS = 128;
   private static final int PAGE_MAX_CHARS = 8192;
   private static final int MAX_PAGES_COUNT = 200;
   private final int slot;
   private final List<String> pages;
   private final Optional<String> title;

   public ServerboundEditBookPacket(int pSlot, List<String> pPages, Optional<String> pTitle) {
      this.slot = pSlot;
      this.pages = ImmutableList.copyOf(pPages);
      this.title = pTitle;
   }

   public ServerboundEditBookPacket(FriendlyByteBuf pBuffer) {
      this.slot = pBuffer.readVarInt();
      this.pages = pBuffer.readCollection(FriendlyByteBuf.limitValue(Lists::newArrayListWithCapacity, 200), (p_182763_) -> {
         return p_182763_.readUtf(8192);
      });
      this.title = pBuffer.readOptional((p_182757_) -> {
         return p_182757_.readUtf(128);
      });
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.slot);
      pBuffer.writeCollection(this.pages, (p_182759_, p_182760_) -> {
         p_182759_.writeUtf(p_182760_, 8192);
      });
      pBuffer.writeOptional(this.title, (p_182753_, p_182754_) -> {
         p_182753_.writeUtf(p_182754_, 128);
      });
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleEditBook(this);
   }

   public List<String> getPages() {
      return this.pages;
   }

   public Optional<String> getTitle() {
      return this.title;
   }

   public int getSlot() {
      return this.slot;
   }
}