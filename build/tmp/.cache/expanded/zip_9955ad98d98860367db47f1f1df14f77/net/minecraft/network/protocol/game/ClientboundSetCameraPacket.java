package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundSetCameraPacket implements Packet<ClientGamePacketListener> {
   private final int cameraId;

   public ClientboundSetCameraPacket(Entity pCameraEntity) {
      this.cameraId = pCameraEntity.getId();
   }

   public ClientboundSetCameraPacket(FriendlyByteBuf pBuffer) {
      this.cameraId = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.cameraId);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleSetCamera(this);
   }

   @Nullable
   public Entity getEntity(Level pLevel) {
      return pLevel.getEntity(this.cameraId);
   }
}