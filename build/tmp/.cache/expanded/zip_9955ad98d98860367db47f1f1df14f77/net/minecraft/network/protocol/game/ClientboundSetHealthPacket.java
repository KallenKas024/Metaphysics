package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetHealthPacket implements Packet<ClientGamePacketListener> {
   private final float health;
   private final int food;
   private final float saturation;

   public ClientboundSetHealthPacket(float pHealth, int pFood, float pSaturation) {
      this.health = pHealth;
      this.food = pFood;
      this.saturation = pSaturation;
   }

   public ClientboundSetHealthPacket(FriendlyByteBuf pBuffer) {
      this.health = pBuffer.readFloat();
      this.food = pBuffer.readVarInt();
      this.saturation = pBuffer.readFloat();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeFloat(this.health);
      pBuffer.writeVarInt(this.food);
      pBuffer.writeFloat(this.saturation);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetHealth(this);
   }

   public float getHealth() {
      return this.health;
   }

   public int getFood() {
      return this.food;
   }

   public float getSaturation() {
      return this.saturation;
   }
}