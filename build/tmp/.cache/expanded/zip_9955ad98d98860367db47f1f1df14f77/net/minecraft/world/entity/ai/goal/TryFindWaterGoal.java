package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;

public class TryFindWaterGoal extends Goal {
   private final PathfinderMob mob;

   public TryFindWaterGoal(PathfinderMob pMob) {
      this.mob = pMob;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.onGround() && !this.mob.level().getFluidState(this.mob.blockPosition()).is(FluidTags.WATER);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      BlockPos blockpos = null;

      for(BlockPos blockpos1 : BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 2.0D), Mth.floor(this.mob.getY() - 2.0D), Mth.floor(this.mob.getZ() - 2.0D), Mth.floor(this.mob.getX() + 2.0D), this.mob.getBlockY(), Mth.floor(this.mob.getZ() + 2.0D))) {
         if (this.mob.level().getFluidState(blockpos1).is(FluidTags.WATER)) {
            blockpos = blockpos1;
            break;
         }
      }

      if (blockpos != null) {
         this.mob.getMoveControl().setWantedPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), 1.0D);
      }

   }
}