package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket implements Packet<ClientGamePacketListener> {
   private static final int POS_IN_SECTION_BITS = 12;
   private final SectionPos sectionPos;
   private final short[] positions;
   private final BlockState[] states;

   public ClientboundSectionBlocksUpdatePacket(SectionPos pSectionPos, ShortSet pPositions, LevelChunkSection pSection) {
      this.sectionPos = pSectionPos;
      int i = pPositions.size();
      this.positions = new short[i];
      this.states = new BlockState[i];
      int j = 0;

      for(short short1 : pPositions) {
         this.positions[j] = short1;
         this.states[j] = pSection.getBlockState(SectionPos.sectionRelativeX(short1), SectionPos.sectionRelativeY(short1), SectionPos.sectionRelativeZ(short1));
         ++j;
      }

   }

   public ClientboundSectionBlocksUpdatePacket(FriendlyByteBuf pBuffer) {
      this.sectionPos = SectionPos.of(pBuffer.readLong());
      int i = pBuffer.readVarInt();
      this.positions = new short[i];
      this.states = new BlockState[i];

      for(int j = 0; j < i; ++j) {
         long k = pBuffer.readVarLong();
         this.positions[j] = (short)((int)(k & 4095L));
         this.states[j] = Block.BLOCK_STATE_REGISTRY.byId((int)(k >>> 12));
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeLong(this.sectionPos.asLong());
      pBuffer.writeVarInt(this.positions.length);

      for(int i = 0; i < this.positions.length; ++i) {
         pBuffer.writeVarLong((long)Block.getId(this.states[i]) << 12 | (long)this.positions[i]);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleChunkBlocksUpdate(this);
   }

   public void runUpdates(BiConsumer<BlockPos, BlockState> pConsumer) {
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i = 0; i < this.positions.length; ++i) {
         short short1 = this.positions[i];
         blockpos$mutableblockpos.set(this.sectionPos.relativeToBlockX(short1), this.sectionPos.relativeToBlockY(short1), this.sectionPos.relativeToBlockZ(short1));
         pConsumer.accept(blockpos$mutableblockpos, this.states[i]);
      }

   }
}