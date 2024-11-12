package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;

public class ServerboundUseItemOnPacket implements Packet<ServerGamePacketListener> {
   private final BlockHitResult blockHit;
   private final InteractionHand hand;
   private final int sequence;

   public ServerboundUseItemOnPacket(InteractionHand pHand, BlockHitResult pBlockHit, int pSequence) {
      this.hand = pHand;
      this.blockHit = pBlockHit;
      this.sequence = pSequence;
   }

   public ServerboundUseItemOnPacket(FriendlyByteBuf pBuffer) {
      this.hand = pBuffer.readEnum(InteractionHand.class);
      this.blockHit = pBuffer.readBlockHitResult();
      this.sequence = pBuffer.readVarInt();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeEnum(this.hand);
      pBuffer.writeBlockHitResult(this.blockHit);
      pBuffer.writeVarInt(this.sequence);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleUseItemOn(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }

   public BlockHitResult getHitResult() {
      return this.blockHit;
   }

   public int getSequence() {
      return this.sequence;
   }
}