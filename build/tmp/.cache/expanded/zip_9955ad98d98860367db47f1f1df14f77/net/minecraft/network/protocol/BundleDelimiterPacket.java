package net.minecraft.network.protocol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;

public class BundleDelimiterPacket<T extends PacketListener> implements Packet<T> {
   /**
    * Writes the raw packet data to the data stream.
    */
   public final void write(FriendlyByteBuf p_265437_) {
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public final void handle(T p_265392_) {
      throw new AssertionError("This packet should be handled by pipeline");
   }
}