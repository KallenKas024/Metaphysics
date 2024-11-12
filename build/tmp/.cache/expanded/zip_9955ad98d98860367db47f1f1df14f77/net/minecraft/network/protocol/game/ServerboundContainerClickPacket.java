package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.IntFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class ServerboundContainerClickPacket implements Packet<ServerGamePacketListener> {
   private static final int MAX_SLOT_COUNT = 128;
   /** The id of the window which was clicked. 0 for player inventory. */
   private final int containerId;
   private final int stateId;
   /** Id of the clicked slot */
   private final int slotNum;
   /** Button used */
   private final int buttonNum;
   /** Inventory operation mode */
   private final ClickType clickType;
   private final ItemStack carriedItem;
   private final Int2ObjectMap<ItemStack> changedSlots;

   public ServerboundContainerClickPacket(int pContainerId, int pStateId, int pSlotNum, int pButtonNum, ClickType pClickType, ItemStack pCarriedItem, Int2ObjectMap<ItemStack> pChangedSlots) {
      this.containerId = pContainerId;
      this.stateId = pStateId;
      this.slotNum = pSlotNum;
      this.buttonNum = pButtonNum;
      this.clickType = pClickType;
      this.carriedItem = pCarriedItem;
      this.changedSlots = Int2ObjectMaps.unmodifiable(pChangedSlots);
   }

   public ServerboundContainerClickPacket(FriendlyByteBuf pBuffer) {
      this.containerId = pBuffer.readByte();
      this.stateId = pBuffer.readVarInt();
      this.slotNum = pBuffer.readShort();
      this.buttonNum = pBuffer.readByte();
      this.clickType = pBuffer.readEnum(ClickType.class);
      IntFunction<Int2ObjectOpenHashMap<ItemStack>> intfunction = FriendlyByteBuf.limitValue(Int2ObjectOpenHashMap::new, 128);
      this.changedSlots = Int2ObjectMaps.unmodifiable(pBuffer.readMap(intfunction, (p_179580_) -> {
         return Integer.valueOf(p_179580_.readShort());
      }, FriendlyByteBuf::readItem));
      this.carriedItem = pBuffer.readItem();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeVarInt(this.stateId);
      pBuffer.writeShort(this.slotNum);
      pBuffer.writeByte(this.buttonNum);
      pBuffer.writeEnum(this.clickType);
      pBuffer.writeMap(this.changedSlots, FriendlyByteBuf::writeShort, FriendlyByteBuf::writeItem);
      pBuffer.writeItemStack(this.carriedItem, false); //Forge: Include full tag for C->S
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleContainerClick(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public int getSlotNum() {
      return this.slotNum;
   }

   public int getButtonNum() {
      return this.buttonNum;
   }

   public ItemStack getCarriedItem() {
      return this.carriedItem;
   }

   public Int2ObjectMap<ItemStack> getChangedSlots() {
      return this.changedSlots;
   }

   public ClickType getClickType() {
      return this.clickType;
   }

   public int getStateId() {
      return this.stateId;
   }
}
