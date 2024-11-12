package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;

public class ClientboundAddPlayerPacket implements Packet<ClientGamePacketListener> {
   private final int entityId;
   private final UUID playerId;
   private final double x;
   private final double y;
   private final double z;
   private final byte yRot;
   private final byte xRot;

   public ClientboundAddPlayerPacket(Player pPlayer) {
      this.entityId = pPlayer.getId();
      this.playerId = pPlayer.getGameProfile().getId();
      this.x = pPlayer.getX();
      this.y = pPlayer.getY();
      this.z = pPlayer.getZ();
      this.yRot = (byte)((int)(pPlayer.getYRot() * 256.0F / 360.0F));
      this.xRot = (byte)((int)(pPlayer.getXRot() * 256.0F / 360.0F));
   }

   public ClientboundAddPlayerPacket(FriendlyByteBuf pBuffer) {
      this.entityId = pBuffer.readVarInt();
      this.playerId = pBuffer.readUUID();
      this.x = pBuffer.readDouble();
      this.y = pBuffer.readDouble();
      this.z = pBuffer.readDouble();
      this.yRot = pBuffer.readByte();
      this.xRot = pBuffer.readByte();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.entityId);
      pBuffer.writeUUID(this.playerId);
      pBuffer.writeDouble(this.x);
      pBuffer.writeDouble(this.y);
      pBuffer.writeDouble(this.z);
      pBuffer.writeByte(this.yRot);
      pBuffer.writeByte(this.xRot);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleAddPlayer(this);
   }

   public int getEntityId() {
      return this.entityId;
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public double getX() {
      return this.x;
   }

   public double getY() {
      return this.y;
   }

   public double getZ() {
      return this.z;
   }

   public byte getyRot() {
      return this.yRot;
   }

   public byte getxRot() {
      return this.xRot;
   }
}