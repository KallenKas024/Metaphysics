package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;

public class TickingTracker extends ChunkTracker {
   public static final int MAX_LEVEL = 33;
   private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
   protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
   private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();

   public TickingTracker() {
      super(34, 16, 256);
      this.chunks.defaultReturnValue((byte)33);
   }

   private SortedArraySet<Ticket<?>> getTickets(long pChunkPos) {
      return this.tickets.computeIfAbsent(pChunkPos, (p_184180_) -> {
         return SortedArraySet.create(4);
      });
   }

   private int getTicketLevelAt(SortedArraySet<Ticket<?>> pTickets) {
      return pTickets.isEmpty() ? 34 : pTickets.first().getTicketLevel();
   }

   public void addTicket(long pChunkPos, Ticket<?> pTicket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(pChunkPos);
      int i = this.getTicketLevelAt(sortedarrayset);
      sortedarrayset.add(pTicket);
      if (pTicket.getTicketLevel() < i) {
         this.update(pChunkPos, pTicket.getTicketLevel(), true);
      }

   }

   public void removeTicket(long pChunkPos, Ticket<?> pTicket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(pChunkPos);
      sortedarrayset.remove(pTicket);
      if (sortedarrayset.isEmpty()) {
         this.tickets.remove(pChunkPos);
      }

      this.update(pChunkPos, this.getTicketLevelAt(sortedarrayset), false);
   }

   public <T> void addTicket(TicketType<T> pType, ChunkPos pChunkPos, int pTicketLevel, T pKey) {
      this.addTicket(pChunkPos.toLong(), new Ticket<>(pType, pTicketLevel, pKey));
   }

   public <T> void removeTicket(TicketType<T> pType, ChunkPos pChunkPos, int pTicketLevel, T pKey) {
      Ticket<T> ticket = new Ticket<>(pType, pTicketLevel, pKey);
      this.removeTicket(pChunkPos.toLong(), ticket);
   }

   public void replacePlayerTicketsLevel(int pTicketLevel) {
      List<Pair<Ticket<ChunkPos>, Long>> list = new ArrayList<>();

      for(Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry : this.tickets.long2ObjectEntrySet()) {
         for(Ticket<?> ticket : entry.getValue()) {
            if (ticket.getType() == TicketType.PLAYER) {
               list.add(Pair.of((Ticket<ChunkPos>)ticket, entry.getLongKey()));
            }
         }
      }

      for(Pair<Ticket<ChunkPos>, Long> pair : list) {
         Long olong = pair.getSecond();
         Ticket<ChunkPos> ticket1 = pair.getFirst();
         this.removeTicket(olong, ticket1);
         ChunkPos chunkpos = new ChunkPos(olong);
         TicketType<ChunkPos> tickettype = ticket1.getType();
         this.addTicket(tickettype, chunkpos, pTicketLevel, chunkpos);
      }

   }

   protected int getLevelFromSource(long pPos) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(pPos);
      return sortedarrayset != null && !sortedarrayset.isEmpty() ? sortedarrayset.first().getTicketLevel() : Integer.MAX_VALUE;
   }

   public int getLevel(ChunkPos pChunkPos) {
      return this.getLevel(pChunkPos.toLong());
   }

   protected int getLevel(long pChunkPos) {
      return this.chunks.get(pChunkPos);
   }

   protected void setLevel(long pChunkPos, int pLevel) {
      if (pLevel > 33) {
         this.chunks.remove(pChunkPos);
      } else {
         this.chunks.put(pChunkPos, (byte)pLevel);
      }

   }

   public void runAllUpdates() {
      this.runUpdates(Integer.MAX_VALUE);
   }

   public String getTicketDebugString(long pChunkPos) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(pChunkPos);
      return sortedarrayset != null && !sortedarrayset.isEmpty() ? sortedarrayset.first().toString() : "no_ticket";
   }
}