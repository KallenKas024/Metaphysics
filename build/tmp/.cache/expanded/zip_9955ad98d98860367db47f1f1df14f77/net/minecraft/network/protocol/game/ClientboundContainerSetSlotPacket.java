package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
   public static final int CARRIED_ITEM = -1;
   public static final int PLAYER_INVENTORY = -2;
   private final int containerId;
   private final int stateId;
   private final int slot;
   private final ItemStack itemStack;

   public ClientboundContainerSetSlotPacket(int pContainerId, int pStateId, int pSlot, ItemStack pItemStack) {
      this.containerId = pContainerId;
      this.stateId = pStateId;
      this.slot = pSlot;
      this.itemStack = pItemStack.copy();
   }

   public ClientboundContainerSetSlotPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
      this.stateId = pBuffer.readVarInt();
      this.slot = pBuffer.readShort();
      this.itemStack = pBuffer.readItem();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeVarInt(this.stateId);
      pBuffer.writeShort(this.slot);
      pBuffer.writeItem(this.itemStack);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleContainerSetSlot(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSlot() {
      return this.slot;
   }

   public ItemStack getItem() {
      return this.itemStack;
   }

   public int getStateId() {
      return this.stateId;
   }
}