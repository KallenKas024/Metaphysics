package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BreathAirGoal extends Goal {
   private final PathfinderMob mob;

   public BreathAirGoal(PathfinderMob pMob) {
      this.mob = pMob;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.getAirSupply() < 140;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean isInterruptable() {
      return false;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.findAirPosition();
   }

   private void findAirPosition() {
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(this.mob.getX() - 1.0D), this.mob.getBlockY(), Mth.floor(this.mob.getZ() - 1.0D), Mth.floor(this.mob.getX() + 1.0D), Mth.floor(this.mob.getY() + 8.0D), Mth.floor(this.mob.getZ() + 1.0D));
      BlockPos blockpos = null;

      for(BlockPos blockpos1 : iterable) {
         if (this.givesAir(this.mob.level(), blockpos1)) {
            blockpos = blockpos1;
            break;
         }
      }

      if (blockpos == null) {
         blockpos = BlockPos.containing(this.mob.getX(), this.mob.getY() + 8.0D, this.mob.getZ());
      }

      this.mob.getNavigation().moveTo((double)blockpos.getX(), (double)(blockpos.getY() + 1), (double)blockpos.getZ(), 1.0D);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.findAirPosition();
      this.mob.moveRelative(0.02F, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
      this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
   }

   private boolean givesAir(LevelReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return (pLevel.getFluidState(pPos).isEmpty() || blockstate.is(Blocks.BUBBLE_COLUMN)) && blockstate.isPathfindable(pLevel, pPos, PathComputationType.LAND);
   }
}