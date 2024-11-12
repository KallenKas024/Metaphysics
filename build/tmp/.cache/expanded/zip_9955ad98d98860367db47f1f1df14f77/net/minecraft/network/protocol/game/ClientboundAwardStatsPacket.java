package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket implements Packet<ClientGamePacketListener> {
   private final Object2IntMap<Stat<?>> stats;

   public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> pStats) {
      this.stats = pStats;
   }

   public ClientboundAwardStatsPacket(FriendlyByteBuf pBuffer) {
      this.stats = pBuffer.readMap(Object2IntOpenHashMap::new, (p_258209_) -> {
         StatType<?> stattype = p_258209_.readById(BuiltInRegistries.STAT_TYPE);
         return readStatCap(pBuffer, stattype);
      }, FriendlyByteBuf::readVarInt);
   }

   private static <T> Stat<T> readStatCap(FriendlyByteBuf pBuffer, StatType<T> pStatType) {
      return pStatType.get(pBuffer.readById(pStatType.getRegistry()));
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleAwardStats(this);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeMap(this.stats, ClientboundAwardStatsPacket::writeStatCap, FriendlyByteBuf::writeVarInt);
   }

   private static <T> void writeStatCap(FriendlyByteBuf p_237570_, Stat<T> p_237571_) {
      p_237570_.writeId(BuiltInRegistries.STAT_TYPE, p_237571_.getType());
      p_237570_.writeId(p_237571_.getType().getRegistry(), p_237571_.getValue());
   }

   public Map<Stat<?>, Integer> getStats() {
      return this.stats;
   }
}