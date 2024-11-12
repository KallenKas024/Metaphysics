package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class ClientboundSetEquipmentPacket implements Packet<ClientGamePacketListener> {
   private static final byte CONTINUE_MASK = -128;
   private final int entity;
   private final List<Pair<EquipmentSlot, ItemStack>> slots;

   public ClientboundSetEquipmentPacket(int pEntity, List<Pair<EquipmentSlot, ItemStack>> pSlots) {
      this.entity = pEntity;
      this.slots = pSlots;
   }

   public ClientboundSetEquipmentPacket(FriendlyByteBuf pBuffer) {
      this.entity = pBuffer.readVarInt();
      EquipmentSlot[] aequipmentslot = EquipmentSlot.values();
      this.slots = Lists.newArrayList();

      int i;
      do {
         i = pBuffer.readByte();
         EquipmentSlot equipmentslot = aequipmentslot[i & 127];
         ItemStack itemstack = pBuffer.readItem();
         this.slots.add(Pair.of(equipmentslot, itemstack));
      } while((i & -128) != 0);

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entity);
      int i = this.slots.size();

      for(int j = 0; j < i; ++j) {
         Pair<EquipmentSlot, ItemStack> pair = this.slots.get(j);
         EquipmentSlot equipmentslot = pair.getFirst();
         boolean flag = j != i - 1;
         int k = equipmentslot.ordinal();
         pBuffer.writeByte(flag ? k | -128 : k);
         pBuffer.writeItem(pair.getSecond());
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetEquipment(this);
   }

   public int getEntity() {
      return this.entity;
   }

   public List<Pair<EquipmentSlot, ItemStack>> getSlots() {
      return this.slots;
   }
}