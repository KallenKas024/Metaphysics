package net.minecraft.world;

import javax.annotation.Nullable;

public interface Clearable {
   void clearContent();

   static void tryClear(@Nullable Object pObject) {
      if (pObject instanceof Clearable) {
         ((Clearable)pObject).clearContent();
      }

   }
}