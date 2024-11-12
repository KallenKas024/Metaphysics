package net.minecraft.util.random;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.slf4j.Logger;

public class Weight {
   public static final Codec<Weight> CODEC = Codec.INT.xmap(Weight::of, Weight::asInt);
   private static final Weight ONE = new Weight(1);
   private static final Logger LOGGER = LogUtils.getLogger();
   private final int value;

   private Weight(int pWeight) {
      this.value = pWeight;
   }

   public static Weight of(int p_146283_) {
      if (p_146283_ == 1) {
         return ONE;
      } else {
         validateWeight(p_146283_);
         return new Weight(p_146283_);
      }
   }

   public int asInt() {
      return this.value;
   }

   private static void validateWeight(int pWeight) {
      if (pWeight < 0) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("Weight should be >= 0"));
      } else {
         if (pWeight == 0 && SharedConstants.IS_RUNNING_IN_IDE) {
            LOGGER.warn("Found 0 weight, make sure this is intentional!");
         }

      }
   }

   public String toString() {
      return Integer.toString(this.value);
   }

   public int hashCode() {
      return Integer.hashCode(this.value);
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         return pOther instanceof Weight && this.value == ((Weight)pOther).value;
      }
   }
}