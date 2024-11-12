package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLevelChunkWithLightPacket implements Packet<ClientGamePacketListener> {
   private final int x;
   private final int z;
   private final ClientboundLevelChunkPacketData chunkData;
   private final ClientboundLightUpdatePacketData lightData;

   public ClientboundLevelChunkWithLightPacket(LevelChunk pChunk, LevelLightEngine pLightEngine, @Nullable BitSet pSkyLight, @Nullable BitSet pBlockLight) {
      ChunkPos chunkpos = pChunk.getPos();
      this.x = chunkpos.x;
      this.z = chunkpos.z;
      this.chunkData = new ClientboundLevelChunkPacketData(pChunk);
      this.lightData = new ClientboundLightUpdatePacketData(chunkpos, pLightEngine, pSkyLight, pBlockLight);
   }

   public ClientboundLevelChunkWithLightPacket(FriendlyByteBuf pBuffer) {
      this.x = pBuffer.readInt();
      this.z = pBuffer.readInt();
      this.chunkData = new ClientboundLevelChunkPacketData(pBuffer, this.x, this.z);
      this.lightData = new ClientboundLightUpdatePacketData(pBuffer, this.x, this.z);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeInt(this.x);
      pBuffer.writeInt(this.z);
      this.chunkData.write(pBuffer);
      this.lightData.write(pBuffer);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleLevelChunkWithLight(this);
   }

   public int getX() {
      return this.x;
   }

   public int getZ() {
      return this.z;
   }

   public ClientboundLevelChunkPacketData getChunkData() {
      return this.chunkData;
   }

   public ClientboundLightUpdatePacketData getLightData() {
      return this.lightData;
   }
}