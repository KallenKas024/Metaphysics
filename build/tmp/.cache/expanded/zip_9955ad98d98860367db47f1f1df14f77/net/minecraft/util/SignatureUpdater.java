package net.minecraft.util;

import java.security.SignatureException;

@FunctionalInterface
public interface SignatureUpdater {
   void update(SignatureUpdater.Output pOutput) throws SignatureException;

   @FunctionalInterface
   public interface Output {
      void update(byte[] pBodyDigest) throws SignatureException;
   }
}