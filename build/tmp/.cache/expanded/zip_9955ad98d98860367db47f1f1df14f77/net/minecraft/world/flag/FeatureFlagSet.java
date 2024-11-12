package net.minecraft.world.flag;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;

public final class FeatureFlagSet {
   private static final FeatureFlagSet EMPTY = new FeatureFlagSet((FeatureFlagUniverse)null, 0L);
   public static final int MAX_CONTAINER_SIZE = 64;
   @Nullable
   private final FeatureFlagUniverse universe;
   private final long mask;

   private FeatureFlagSet(@Nullable FeatureFlagUniverse pUniverse, long pMask) {
      this.universe = pUniverse;
      this.mask = pMask;
   }

   static FeatureFlagSet create(FeatureFlagUniverse pUniverse, Collection<FeatureFlag> pFlags) {
      if (pFlags.isEmpty()) {
         return EMPTY;
      } else {
         long i = computeMask(pUniverse, 0L, pFlags);
         return new FeatureFlagSet(pUniverse, i);
      }
   }

   public static FeatureFlagSet of() {
      return EMPTY;
   }

   public static FeatureFlagSet of(FeatureFlag pFlag) {
      return new FeatureFlagSet(pFlag.universe, pFlag.mask);
   }

   public static FeatureFlagSet of(FeatureFlag pFlag, FeatureFlag... pOthers) {
      long i = pOthers.length == 0 ? pFlag.mask : computeMask(pFlag.universe, pFlag.mask, Arrays.asList(pOthers));
      return new FeatureFlagSet(pFlag.universe, i);
   }

   private static long computeMask(FeatureFlagUniverse pUniverse, long pMask, Iterable<FeatureFlag> pFlags) {
      for(FeatureFlag featureflag : pFlags) {
         if (pUniverse != featureflag.universe) {
            throw new IllegalStateException("Mismatched feature universe, expected '" + pUniverse + "', but got '" + featureflag.universe + "'");
         }

         pMask |= featureflag.mask;
      }

      return pMask;
   }

   public boolean contains(FeatureFlag pFlag) {
      if (this.universe != pFlag.universe) {
         return false;
      } else {
         return (this.mask & pFlag.mask) != 0L;
      }
   }

   public boolean isSubsetOf(FeatureFlagSet pSet) {
      if (this.universe == null) {
         return true;
      } else if (this.universe != pSet.universe) {
         return false;
      } else {
         return (this.mask & ~pSet.mask) == 0L;
      }
   }

   public FeatureFlagSet join(FeatureFlagSet pOther) {
      if (this.universe == null) {
         return pOther;
      } else if (pOther.universe == null) {
         return this;
      } else if (this.universe != pOther.universe) {
         throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + pOther.universe + "'");
      } else {
         return new FeatureFlagSet(this.universe, this.mask | pOther.mask);
      }
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else {
         if (pOther instanceof FeatureFlagSet) {
            FeatureFlagSet featureflagset = (FeatureFlagSet)pOther;
            if (this.universe == featureflagset.universe && this.mask == featureflagset.mask) {
               return true;
            }
         }

         return false;
      }
   }

   public int hashCode() {
      return (int)HashCommon.mix(this.mask);
   }
}