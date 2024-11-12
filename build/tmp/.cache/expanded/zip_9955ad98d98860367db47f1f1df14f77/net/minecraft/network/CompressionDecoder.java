package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Handles decompression of network traffic.
 * 
 * @see Connection#setupCompression
 */
public class CompressionDecoder extends ByteToMessageDecoder {
   public static final int MAXIMUM_COMPRESSED_LENGTH = 2097152;
   public static final int MAXIMUM_UNCOMPRESSED_LENGTH = 8388608;
   private final Inflater inflater;
   private int threshold;
   private boolean validateDecompressed;

   public CompressionDecoder(int pThreshold, boolean pValidateDecompressed) {
      this.threshold = pThreshold;
      this.validateDecompressed = pValidateDecompressed;
      this.inflater = new Inflater();
   }

   protected void decode(ChannelHandlerContext pContext, ByteBuf pIn, List<Object> pOut) throws Exception {
      if (pIn.readableBytes() != 0) {
         FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(pIn);
         int i = friendlybytebuf.readVarInt();
         if (i == 0) {
            pOut.add(friendlybytebuf.readBytes(friendlybytebuf.readableBytes()));
         } else {
            if (this.validateDecompressed) {
               if (i < this.threshold) {
                  throw new DecoderException("Badly compressed packet - size of " + i + " is below server threshold of " + this.threshold);
               }

               if (i > 8388608) {
                  throw new DecoderException("Badly compressed packet - size of " + i + " is larger than protocol maximum of 8388608");
               }
            }

            byte[] abyte = new byte[friendlybytebuf.readableBytes()];
            friendlybytebuf.readBytes(abyte);
            this.inflater.setInput(abyte);
            byte[] abyte1 = new byte[i];
            this.inflater.inflate(abyte1);
            pOut.add(Unpooled.wrappedBuffer(abyte1));
            this.inflater.reset();
         }
      }
   }

   public void setThreshold(int pThreshold, boolean pValidateDecompressed) {
      this.threshold = pThreshold;
      this.validateDecompressed = pValidateDecompressed;
   }
}