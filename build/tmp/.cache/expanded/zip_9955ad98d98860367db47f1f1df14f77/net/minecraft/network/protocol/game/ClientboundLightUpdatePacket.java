package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;
   private final ClientboundLightUpdatePacketData lightData;

   public ClientboundLightUpdatePacket(ChunkPos pChunkPos, LevelLightEngine pLightEngine, @Nullable BitSet pSkyLight, @Nullable BitSet pBlockLight) {
      this.x = pChunkPos.x;
      this.z = pChunkPos.z;
      this.lightData = new ClientboundLightUpdatePacketData(pChunkPos, pLightEngine, pSkyLight, pBlockLight);
   }

   public ClientboundLightUpdatePacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readVarInt();
      this.z = pBuffer.readVarInt();
      this.lightData = new ClientboundLightUpdatePacketData(pBuffer, this.x, this.z);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeVarInt(this.x);
      pBuffer.writeVarInt(this.z);
      this.lightData.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleLightUpdatePacket(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public ClientboundLightUpdatePacketData getLightData() {
      return this.lightData;
   }
}