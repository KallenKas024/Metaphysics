package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener> {
   private final int vehicle;
   private final int[] passengers;

   public ClientboundSetPassengersPacket(Entity pVehicle) {
      this.vehicle = pVehicle.getId();
      List<Entity> list = pVehicle.getPassengers();
      this.passengers = new int[list.size()];

      for(int i = 0; i < list.size(); ++i) {
         this.passengers[i] = list.get(i).getId();
      }

   }

   public ClientboundSetPassengersPacket(FriendlyByteBuf pBuffer) {
      this.vehicle = pBuffer.readVarInt();
      this.passengers = pBuffer.readVarIntArray();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.vehicle);
      pBuffer.writeVarIntArray(this.passengers);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetEntityPassengersPacket(this);
   }

   public int[] getPassengers() {
      return this.passengers;
   }

   public int getVehicle() {
      return this.vehicle;
   }
}