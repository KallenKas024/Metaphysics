package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ServerboundCustomPayloadPacket implements Packet<ServerGamePacketListener>, net.minecraftforge.network.ICustomPacket<ServerboundCustomPayloadPacket> {
   private static final int MAX_PAYLOAD_SIZE = 32767;
   public static final ResourceLocation BRAND = new ResourceLocation("brand");
   private final ResourceLocation identifier;
   private final FriendlyByteBuf data;

   public ServerboundCustomPayloadPacket(ResourceLocation pIdentifier, FriendlyByteBuf pData) {
      this.identifier = pIdentifier;
      this.data = pData;
   }

   public ServerboundCustomPayloadPacket(FriendlyByteBuf pBuffer) {
      this.identifier = pBuffer.readResourceLocation();
      int i = pBuffer.readableBytes();
      if (i >= 0 && i <= 32767) {
         this.data = new FriendlyByteBuf(pBuffer.readBytes(i));
      } else {
         throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
      }
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeResourceLocation(this.identifier);
      pBuffer.writeBytes((ByteBuf)this.data.slice()); // Use Slice instead of copy, to not update the read index, allowing packet to be sent multiple times.
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerGamePacketListener pHandler) {
      pHandler.handleCustomPayload(this);
      this.data.release();
   }

   public ResourceLocation getIdentifier() {
      return this.identifier;
   }

   public FriendlyByteBuf getData() {
      return this.data;
   }

   @Override public int getIndex() { return Integer.MAX_VALUE; }
   @Override public ResourceLocation getName() { return getIdentifier(); }
   @org.jetbrains.annotations.Nullable @Override public FriendlyByteBuf getInternalData() { return getData(); }
}
