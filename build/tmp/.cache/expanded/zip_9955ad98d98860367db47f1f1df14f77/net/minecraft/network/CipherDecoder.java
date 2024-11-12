package net.minecraft.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import javax.crypto.Cipher;

/**
 * Channel handler that handles protocol decryption.
 * 
 * @see Connection#setEncryptionKey
 */
public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {
   private final CipherBase cipher;

   public CipherDecoder(Cipher pCipher) {
      this.cipher = new CipherBase(pCipher);
   }

   protected void decode(ChannelHandlerContext pContext, ByteBuf pIn, List<Object> pOut) throws Exception {
      pOut.add(this.cipher.decipher(pContext, pIn));
   }
}