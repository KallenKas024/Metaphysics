package net.minecraft.client.main;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SilentInitException extends RuntimeException {
   public SilentInitException(String pMessage) {
      super(pMessage);
   }

   public SilentInitException(String pMessage, Throwable pCause) {
      super(pMessage, pCause);
   }
}