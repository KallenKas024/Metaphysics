package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FollowOwnerGoal extends Goal {
   public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
   private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
   private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
   private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
   private final TamableAnimal tamable;
   private LivingEntity owner;
   private final LevelReader level;
   private final double speedModifier;
   private final PathNavigation navigation;
   private int timeToRecalcPath;
   private final float stopDistance;
   private final float startDistance;
   private float oldWaterCost;
   private final boolean canFly;

   public FollowOwnerGoal(TamableAnimal pTamable, double pSpeedModifier, float pStartDistance, float pStopDistance, boolean pCanFly) {
      this.tamable = pTamable;
      this.level = pTamable.level();
      this.speedModifier = pSpeedModifier;
      this.navigation = pTamable.getNavigation();
      this.startDistance = pStartDistance;
      this.stopDistance = pStopDistance;
      this.canFly = pCanFly;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      if (!(pTamable.getNavigation() instanceof GroundPathNavigation) && !(pTamable.getNavigation() instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      LivingEntity livingentity = this.tamable.getOwner();
      if (livingentity == null) {
         return false;
      } else if (livingentity.isSpectator()) {
         return false;
      } else if (this.unableToMove()) {
         return false;
      } else if (this.tamable.distanceToSqr(livingentity) < (double)(this.startDistance * this.startDistance)) {
         return false;
      } else {
         this.owner = livingentity;
         return true;
      }
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      if (this.navigation.isDone()) {
         return false;
      } else if (this.unableToMove()) {
         return false;
      } else {
         return !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
      }
   }

   private boolean unableToMove() {
      return this.tamable.isOrderedToSit() || this.tamable.isPassenger() || this.tamable.isLeashed();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
      this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.owner = null;
      this.navigation.stop();
      this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
      if (--this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = this.adjustedTickDelay(10);
         if (this.tamable.distanceToSqr(this.owner) >= 144.0D) {
            this.teleportToOwner();
         } else {
            this.navigation.moveTo(this.owner, this.speedModifier);
         }

      }
   }

   private void teleportToOwner() {
      BlockPos blockpos = this.owner.blockPosition();

      for(int i = 0; i < 10; ++i) {
         int j = this.randomIntInclusive(-3, 3);
         int k = this.randomIntInclusive(-1, 1);
         int l = this.randomIntInclusive(-3, 3);
         boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
         if (flag) {
            return;
         }
      }

   }

   private boolean maybeTeleportTo(int pX, int pY, int pZ) {
      if (Math.abs((double)pX - this.owner.getX()) < 2.0D && Math.abs((double)pZ - this.owner.getZ()) < 2.0D) {
         return false;
      } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
         return false;
      } else {
         this.tamable.moveTo((double)pX + 0.5D, (double)pY, (double)pZ + 0.5D, this.tamable.getYRot(), this.tamable.getXRot());
         this.navigation.stop();
         return true;
      }
   }

   private boolean canTeleportTo(BlockPos pPos) {
      BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pPos.mutable());
      if (blockpathtypes != BlockPathTypes.WALKABLE) {
         return false;
      } else {
         BlockState blockstate = this.level.getBlockState(pPos.below());
         if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
            return false;
         } else {
            BlockPos blockpos = pPos.subtract(this.tamable.blockPosition());
            return this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(blockpos));
         }
      }
   }

   private int randomIntInclusive(int pMin, int pMax) {
      return this.tamable.getRandom().nextInt(pMax - pMin + 1) + pMin;
   }
}