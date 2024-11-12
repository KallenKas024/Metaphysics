package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.LevelReader;

public class CatLieOnBedGoal extends MoveToBlockGoal {
   private final Cat cat;

   public CatLieOnBedGoal(Cat pCat, double pSpeedModifier, int pSearchRange) {
      super(pCat, pSpeedModifier, pSearchRange, 6);
      this.cat = pCat;
      this.verticalSearchStart = -2;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.cat.isTame() && !this.cat.isOrderedToSit() && !this.cat.isLying() && super.canUse();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.cat.setInSittingPose(false);
   }

   protected int nextStartTick(PathfinderMob pCreature) {
      return 40;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.cat.setLying(false);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      super.tick();
      this.cat.setInSittingPose(false);
      if (!this.isReachedTarget()) {
         this.cat.setLying(false);
      } else if (!this.cat.isLying()) {
         this.cat.setLying(true);
      }

   }

   /**
    * Return {@code true} to set given position as destination
    */
   protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
      return pLevel.isEmptyBlock(pPos.above()) && pLevel.getBlockState(pPos).is(BlockTags.BEDS);
   }
}