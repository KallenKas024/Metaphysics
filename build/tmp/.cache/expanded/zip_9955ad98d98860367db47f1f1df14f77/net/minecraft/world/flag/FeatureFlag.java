package net.minecraft.world.flag;

public class FeatureFlag {
   final FeatureFlagUniverse universe;
   final long mask;

   FeatureFlag(FeatureFlagUniverse pUniverse, int pMaskBit) {
      this.universe = pUniverse;
      this.mask = 1L << pMaskBit;
   }
}