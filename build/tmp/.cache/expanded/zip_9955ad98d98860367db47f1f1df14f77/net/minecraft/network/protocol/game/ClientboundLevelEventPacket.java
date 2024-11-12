package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundLevelEventPacket implements Packet<ClientGamePacketListener> {
   private final int type;
   private final BlockPos pos;
   /** can be a block/item id or other depending on the soundtype */
   private final int data;
   /** If true the sound is played across the server */
   private final boolean globalEvent;

   public ClientboundLevelEventPacket(int pType, BlockPos pPos, int pData, boolean pGlobalEvent) {
      this.type = pType;
      this.pos = pPos.immutable();
      this.data = pData;
      this.globalEvent = pGlobalEvent;
   }

   public ClientboundLevelEventPacket(FriendlyByteBuf pBuffer) {
      this.type = pBuffer.readInt();
      this.pos = pBuffer.readBlockPos();
      this.data = pBuffer.readInt();
      this.globalEvent = pBuffer.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.type);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeInt(this.data);
      pBuffer.writeBoolean(this.globalEvent);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleLevelEvent(this);
   }

   public boolean isGlobalEvent() {
      return this.globalEvent;
   }

   public int getType() {
      return this.type;
   }

   public int getData() {
      return this.data;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}