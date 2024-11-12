package com.mojang.realmsclient;

import java.util.Arrays;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyCombo {
   private final char[] chars;
   private int matchIndex;
   private final Runnable onCompletion;

   public KeyCombo(char[] pChars, Runnable pOnCompletion) {
      this.onCompletion = pOnCompletion;
      if (pChars.length < 1) {
         throw new IllegalArgumentException("Must have at least one char");
      } else {
         this.chars = pChars;
      }
   }

   public KeyCombo(char[] pChars) {
      this(pChars, () -> {
      });
   }

   public boolean keyPressed(char pKey) {
      if (pKey == this.chars[this.matchIndex++]) {
         if (this.matchIndex == this.chars.length) {
            this.reset();
            this.onCompletion.run();
            return true;
         }
      } else {
         this.reset();
      }

      return false;
   }

   public void reset() {
      this.matchIndex = 0;
   }

   public String toString() {
      return "KeyCombo{chars=" + Arrays.toString(this.chars) + ", matchIndex=" + this.matchIndex + "}";
   }
}