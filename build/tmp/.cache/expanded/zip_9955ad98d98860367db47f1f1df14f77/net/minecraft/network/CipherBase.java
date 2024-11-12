package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import javax.crypto.Cipher;
import javax.crypto.ShortBufferException;

public class CipherBase {
   private final Cipher cipher;
   private byte[] heapIn = new byte[0];
   private byte[] heapOut = new byte[0];

   protected CipherBase(Cipher pCipher) {
      this.cipher = pCipher;
   }

   private byte[] bufToByte(ByteBuf pBuffer) {
      int i = pBuffer.readableBytes();
      if (this.heapIn.length < i) {
         this.heapIn = new byte[i];
      }

      pBuffer.readBytes(this.heapIn, 0, i);
      return this.heapIn;
   }

   protected ByteBuf decipher(ChannelHandlerContext pCtx, ByteBuf pBuffer) throws ShortBufferException {
      int i = pBuffer.readableBytes();
      byte[] abyte = this.bufToByte(pBuffer);
      ByteBuf bytebuf = pCtx.alloc().heapBuffer(this.cipher.getOutputSize(i));
      bytebuf.writerIndex(this.cipher.update(abyte, 0, i, bytebuf.array(), bytebuf.arrayOffset()));
      return bytebuf;
   }

   protected void encipher(ByteBuf pInput, ByteBuf pOut) throws ShortBufferException {
      int i = pInput.readableBytes();
      byte[] abyte = this.bufToByte(pInput);
      int j = this.cipher.getOutputSize(i);
      if (this.heapOut.length < j) {
         this.heapOut = new byte[j];
      }

      pOut.writeBytes(this.heapOut, 0, this.cipher.update(abyte, 0, i, this.heapOut));
   }
}