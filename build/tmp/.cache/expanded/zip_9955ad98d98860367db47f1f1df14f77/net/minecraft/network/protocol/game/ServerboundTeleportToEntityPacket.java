package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ServerboundTeleportToEntityPacket implements Packet<ServerGamePacketListener> {
   private final UUID uuid;

   public ServerboundTeleportToEntityPacket(UUID pUuid) {
      this.uuid = pUuid;
   }

   public ServerboundTeleportToEntityPacket(FriendlyByteBuf pBuffer) {
      this.uuid = pBuffer.readUUID();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeUUID(this.uuid);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleTeleportToEntityPacket(this);
   }

   @Nullable
   public Entity getEntity(ServerLevel pLevel) {
      return pLevel.getEntity(this.uuid);
   }
}