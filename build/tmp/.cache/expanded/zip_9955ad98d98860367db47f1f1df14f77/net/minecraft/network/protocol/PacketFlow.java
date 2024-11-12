package net.minecraft.network.protocol;

/**
 * The direction of packets.
 */
public enum PacketFlow {
   SERVERBOUND,
   CLIENTBOUND;

   public PacketFlow getOpposite() {
      return this == CLIENTBOUND ? SERVERBOUND : CLIENTBOUND;
   }
}