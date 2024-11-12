package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ClientboundBlockUpdatePacket implements Packet<ClientGamePacketListener> {
   private final BlockPos pos;
   private final BlockState blockState;

   public ClientboundBlockUpdatePacket(BlockPos pPos, BlockState pBlockState) {
      this.pos = pPos;
      this.blockState = pBlockState;
   }

   public ClientboundBlockUpdatePacket(BlockGetter pBlockGetter, BlockPos pPos) {
      this(pPos, pBlockGetter.getBlockState(pPos));
   }

   public ClientboundBlockUpdatePacket(FriendlyByteBuf pBuffer) {
      this.pos = pBuffer.readBlockPos();
      this.blockState = pBuffer.readById(Block.BLOCK_STATE_REGISTRY);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeId(Block.BLOCK_STATE_REGISTRY, this.blockState);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ClientGamePacketListener pHandler) {
      pHandler.handleBlockUpdate(this);
   }

   public BlockState getBlockState() {
      return this.blockState;
   }

   public BlockPos getPos() {
      return this.pos;
   }
}