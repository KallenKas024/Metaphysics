package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.Cipher;

/**
 * Channel handler that handles protocol encryption.
 * 
 * @see Connection#setEncryptionKey
 */
public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {
   private final CipherBase cipher;

   public CipherEncoder(Cipher pCipher) {
      this.cipher = new CipherBase(pCipher);
   }

   protected void encode(ChannelHandlerContext pContext, ByteBuf pMessage, ByteBuf pOut) throws Exception {
      this.cipher.encipher(pMessage, pOut);
   }
}