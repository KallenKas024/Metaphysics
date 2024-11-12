package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.core.BlockPos;

public class PotentialCalculator {
   private final List<PotentialCalculator.PointCharge> charges = Lists.newArrayList();

   public void addCharge(BlockPos pPos, double pCharge) {
      if (pCharge != 0.0D) {
         this.charges.add(new PotentialCalculator.PointCharge(pPos, pCharge));
      }

   }

   public double getPotentialEnergyChange(BlockPos pPos, double pCharge) {
      if (pCharge == 0.0D) {
         return 0.0D;
      } else {
         double d0 = 0.0D;

         for(PotentialCalculator.PointCharge potentialcalculator$pointcharge : this.charges) {
            d0 += potentialcalculator$pointcharge.getPotentialChange(pPos);
         }

         return d0 * pCharge;
      }
   }

   static class PointCharge {
      private final BlockPos pos;
      private final double charge;

      public PointCharge(BlockPos pPos, double pCharge) {
         this.pos = pPos;
         this.charge = pCharge;
      }

      public double getPotentialChange(BlockPos pPos) {
         double d0 = this.pos.distSqr(pPos);
         return d0 == 0.0D ? Double.POSITIVE_INFINITY : this.charge / Math.sqrt(d0);
      }
   }
}