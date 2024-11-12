package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
   private final int containerId;
   private final int stateId;
   private final List<ItemStack> items;
   private final ItemStack carriedItem;

   public ClientboundContainerSetContentPacket(int pContainerId, int pStateId, NonNullList<ItemStack> pItems, ItemStack pCarriedItem) {
      this.containerId = pContainerId;
      this.stateId = pStateId;
      this.items = NonNullList.withSize(pItems.size(), ItemStack.EMPTY);

      for(int i = 0; i < pItems.size(); ++i) {
         this.items.set(i, pItems.get(i).copy());
      }

      this.carriedItem = pCarriedItem.copy();
   }

   public ClientboundContainerSetContentPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readUnsignedByte();
      this.stateId = pBuffer.readVarInt();
      this.items = pBuffer.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readItem);
      this.carriedItem = pBuffer.readItem();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeVarInt(this.stateId);
      pBuffer.writeCollection(this.items, FriendlyByteBuf::writeItem);
      pBuffer.writeItem(this.carriedItem);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleContainerContent(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public List<ItemStack> getItems() {
      return this.items;
   }

   public ItemStack getCarriedItem() {
      return this.carriedItem;
   }

   public int getStateId() {
      return this.stateId;
   }
}