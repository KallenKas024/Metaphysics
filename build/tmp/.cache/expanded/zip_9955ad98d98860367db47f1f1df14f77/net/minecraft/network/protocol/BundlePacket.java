package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public abstract class BundlePacket<T extends PacketListener> implements Packet<T> {
   private final Iterable<Packet<T>> packets;

   protected BundlePacket(Iterable<Packet<T>> pPackets) {
      this.packets = pPackets;
   }

   public final Iterable<Packet<T>> subPackets() {
      return this.packets;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public final void write(FriendlyByteBuf pBuffer) {
   }
}