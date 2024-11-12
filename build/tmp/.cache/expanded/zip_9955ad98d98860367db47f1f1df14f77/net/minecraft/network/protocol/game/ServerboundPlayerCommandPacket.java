package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ServerboundPlayerCommandPacket implements Packet<ServerGamePacketListener> {
   private final int id;
   private final ServerboundPlayerCommandPacket.Action action;
   private final int data;

   public ServerboundPlayerCommandPacket(Entity pEntity, ServerboundPlayerCommandPacket.Action pAction) {
      this(pEntity, pAction, 0);
   }

   public ServerboundPlayerCommandPacket(Entity pEntity, ServerboundPlayerCommandPacket.Action pAction, int pData) {
      this.id = pEntity.getId();
      this.action = pAction;
      this.data = pData;
   }

   public ServerboundPlayerCommandPacket(FriendlyByteBuf pBuffer) {
      this.id = pBuffer.readVarInt();
      this.action = pBuffer.readEnum(ServerboundPlayerCommandPacket.Action.class);
      this.data = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeEnum(this.action);
      pBuffer.writeVarInt(this.data);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handlePlayerCommand(this);
   }

   public int getId() {
      return this.id;
   }

   public ServerboundPlayerCommandPacket.Action getAction() {
      return this.action;
   }

   public int getData() {
      return this.data;
   }

   public static enum Action {
      PRESS_SHIFT_KEY,
      RELEASE_SHIFT_KEY,
      STOP_SLEEPING,
      START_SPRINTING,
      STOP_SPRINTING,
      START_RIDING_JUMP,
      STOP_RIDING_JUMP,
      OPEN_INVENTORY,
      START_FALL_FLYING;
   }
}