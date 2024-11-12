package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
   private final byte[] keybytes;
   private final byte[] encryptedChallenge;

   public ServerboundKeyPacket(SecretKey pSecretKey, PublicKey pPublicKey, byte[] pChallenge) throws CryptException {
      this.keybytes = Crypt.encryptUsingKey(pPublicKey, pSecretKey.getEncoded());
      this.encryptedChallenge = Crypt.encryptUsingKey(pPublicKey, pChallenge);
   }

   public ServerboundKeyPacket(FriendlyByteBuf pBuffer) {
      this.keybytes = pBuffer.readByteArray();
      this.encryptedChallenge = pBuffer.readByteArray();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(FriendlyByteBuf pBuffer) {
      pBuffer.writeByteArray(this.keybytes);
      pBuffer.writeByteArray(this.encryptedChallenge);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(ServerLoginPacketListener pHandler) {
      pHandler.handleKey(this);
   }

   public SecretKey getSecretKey(PrivateKey pKey) throws CryptException {
      return Crypt.decryptByteToSecretKey(pKey, this.keybytes);
   }

   public boolean isChallengeValid(byte[] pExpected, PrivateKey pKey) {
      try {
         return Arrays.equals(pExpected, Crypt.decryptUsingKey(pKey, this.encryptedChallenge));
      } catch (CryptException cryptexception) {
         return false;
      }
   }
}