package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

/**
 * Counterpart to {@link Varint21LengthFieldPrepender}. Decodes each frame ("packet") by first reading its length and
 * then its data.
 */
public class Varint21FrameDecoder extends ByteToMessageDecoder {
   protected void decode(ChannelHandlerContext pContext, ByteBuf pIn, List<Object> pOut) {
      pIn.markReaderIndex();
      byte[] abyte = new byte[3];

      for(int i = 0; i < abyte.length; ++i) {
         if (!pIn.isReadable()) {
            pIn.resetReaderIndex();
            return;
         }

         abyte[i] = pIn.readByte();
         if (abyte[i] >= 0) {
            FriendlyByteBuf friendlybytebuf = new FriendlyByteBuf(Unpooled.wrappedBuffer(abyte));

            try {
               int j = friendlybytebuf.readVarInt();
               if (pIn.readableBytes() >= j) {
                  pOut.add(pIn.readBytes(j));
                  return;
               }

               pIn.resetReaderIndex();
            } finally {
               friendlybytebuf.release();
            }

            return;
         }
      }

      throw new CorruptedFrameException("length wider than 21-bit");
   }
}