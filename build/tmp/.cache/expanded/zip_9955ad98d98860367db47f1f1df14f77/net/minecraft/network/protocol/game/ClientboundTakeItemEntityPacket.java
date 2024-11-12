package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundTakeItemEntityPacket implements Packet<ClientGamePacketListener> {
   private final int itemId;
   private final int playerId;
   private final int amount;

   public ClientboundTakeItemEntityPacket(int pItemId, int pPlayerId, int pAmount) {
      this.itemId = pItemId;
      this.playerId = pPlayerId;
      this.amount = pAmount;
   }

   public ClientboundTakeItemEntityPacket(FriendlyByteBuf pBuffer) {
      this.itemId = pBuffer.readVarInt();
      this.playerId = pBuffer.readVarInt();
      this.amount = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.itemId);
      pBuffer.writeVarInt(this.playerId);
      pBuffer.writeVarInt(this.amount);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleTakeItemEntity(this);
   }

   public int getItemId() {
      return this.itemId;
   }

   public int getPlayerId() {
      return this.playerId;
   }

   public int getAmount() {
      return this.amount;
   }
}