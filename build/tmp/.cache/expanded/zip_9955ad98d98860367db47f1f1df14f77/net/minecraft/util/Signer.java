package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.security.PrivateKey;
import java.security.Signature;
import org.slf4j.Logger;

public interface Signer {
   Logger LOGGER = LogUtils.getLogger();

   byte[] sign(SignatureUpdater pUpdater);

   default byte[] sign(byte[] pSignature) {
      return this.sign((p_216394_) -> {
         p_216394_.update(pSignature);
      });
   }

   static Signer from(PrivateKey pPrivateKey, String pAlgorithm) {
      return (p_216386_) -> {
         try {
            Signature signature = Signature.getInstance(pAlgorithm);
            signature.initSign(pPrivateKey);
            p_216386_.update(signature::update);
            return signature.sign();
         } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign message", exception);
         }
      };
   }
}