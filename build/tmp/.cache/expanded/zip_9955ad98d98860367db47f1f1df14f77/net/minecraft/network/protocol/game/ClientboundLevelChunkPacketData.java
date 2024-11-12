package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacketData {
   private static final int TWO_MEGABYTES = 2097152;
   private final CompoundTag heightmaps;
   private final byte[] buffer;
   private final List<ClientboundLevelChunkPacketData.BlockEntityInfo> blockEntitiesData;

   public ClientboundLevelChunkPacketData(LevelChunk pLevelChunk) {
      this.heightmaps = new CompoundTag();

      for(Map.Entry<Heightmap.Types, Heightmap> entry : pLevelChunk.getHeightmaps()) {
         if (entry.getKey().sendToClient()) {
            this.heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
         }
      }

      this.buffer = new byte[calculateChunkSize(pLevelChunk)];
      extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), pLevelChunk);
      this.blockEntitiesData = Lists.newArrayList();

      for(Map.Entry<BlockPos, BlockEntity> entry1 : pLevelChunk.getBlockEntities().entrySet()) {
         this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create(entry1.getValue()));
      }

   }

   public ClientboundLevelChunkPacketData(FriendlyByteBuf pHeightmaps, int pChunkX, int pChunkZ) {
      this.heightmaps = pHeightmaps.readNbt();
      if (this.heightmaps == null) {
         throw new RuntimeException("Can't read heightmap in packet for [" + pChunkX + ", " + pChunkZ + "]");
      } else {
         int i = pHeightmaps.readVarInt();
         if (i > 2097152) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
         } else {
            this.buffer = new byte[i];
            pHeightmaps.readBytes(this.buffer);
            this.blockEntitiesData = pHeightmaps.readList(ClientboundLevelChunkPacketData.BlockEntityInfo::new);
         }
      }
   }

   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeNbt(this.heightmaps);
      pBuffer.writeVarInt(this.buffer.length);
      pBuffer.writeBytes(this.buffer);
      pBuffer.writeCollection(this.blockEntitiesData, (p_195672_, p_195673_) -> {
         p_195673_.write(p_195672_);
      });
   }

   private static int calculateChunkSize(LevelChunk pChunk) {
      int i = 0;

      for(LevelChunkSection levelchunksection : pChunk.getSections()) {
         i += levelchunksection.getSerializedSize();
      }

      return i;
   }

   private ByteBuf getWriteBuffer() {
      ByteBuf bytebuf = Unpooled.wrappedBuffer(this.buffer);
      bytebuf.writerIndex(0);
      return bytebuf;
   }

   public static void extractChunkData(FriendlyByteBuf pBuffer, LevelChunk pChunk) {
      for(LevelChunkSection levelchunksection : pChunk.getSections()) {
         levelchunksection.write(pBuffer);
      }

   }

   public Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int pChunkX, int pChunkZ) {
      return (p_195663_) -> {
         this.getBlockEntitiesTags(p_195663_, pChunkX, pChunkZ);
      };
   }

   private void getBlockEntitiesTags(ClientboundLevelChunkPacketData.BlockEntityTagOutput pOutput, int pChunkX, int pChunkZ) {
      int i = 16 * pChunkX;
      int j = 16 * pChunkZ;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(ClientboundLevelChunkPacketData.BlockEntityInfo clientboundlevelchunkpacketdata$blockentityinfo : this.blockEntitiesData) {
         int k = i + SectionPos.sectionRelative(clientboundlevelchunkpacketdata$blockentityinfo.packedXZ >> 4);
         int l = j + SectionPos.sectionRelative(clientboundlevelchunkpacketdata$blockentityinfo.packedXZ);
         blockpos$mutableblockpos.set(k, clientboundlevelchunkpacketdata$blockentityinfo.y, l);
         pOutput.accept(blockpos$mutableblockpos, clientboundlevelchunkpacketdata$blockentityinfo.type, clientboundlevelchunkpacketdata$blockentityinfo.tag);
      }

   }

   public FriendlyByteBuf getReadBuffer() {
      return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
   }

   public CompoundTag getHeightmaps() {
      return this.heightmaps;
   }

   static class BlockEntityInfo {
      final int packedXZ;
      final int y;
      final BlockEntityType<?> type;
      @Nullable
      final CompoundTag tag;

      private BlockEntityInfo(int pPackedXZ, int pY, BlockEntityType<?> pType, @Nullable CompoundTag pTag) {
         this.packedXZ = pPackedXZ;
         this.y = pY;
         this.type = pType;
         this.tag = pTag;
      }

      private BlockEntityInfo(FriendlyByteBuf pBuffer) {
         this.packedXZ = pBuffer.readByte();
         this.y = pBuffer.readShort();
         this.type = pBuffer.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
         this.tag = pBuffer.readNbt();
      }

      void write(FriendlyByteBuf pBuffer) {
         pBuffer.writeByte(this.packedXZ);
         pBuffer.writeShort(this.y);
         pBuffer.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, this.type);
         pBuffer.writeNbt(this.tag);
      }

      static ClientboundLevelChunkPacketData.BlockEntityInfo create(BlockEntity pBlockEntity) {
         CompoundTag compoundtag = pBlockEntity.getUpdateTag();
         BlockPos blockpos = pBlockEntity.getBlockPos();
         int i = SectionPos.sectionRelative(blockpos.getX()) << 4 | SectionPos.sectionRelative(blockpos.getZ());
         return new ClientboundLevelChunkPacketData.BlockEntityInfo(i, blockpos.getY(), pBlockEntity.getType(), compoundtag.isEmpty() ? null : compoundtag);
      }
   }

   @FunctionalInterface
   public interface BlockEntityTagOutput {
      void accept(BlockPos pPos, BlockEntityType<?> pType, @Nullable CompoundTag pNbt);
   }
}