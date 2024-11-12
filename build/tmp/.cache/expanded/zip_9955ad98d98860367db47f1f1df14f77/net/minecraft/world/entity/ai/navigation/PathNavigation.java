package net.minecraft.world.entity.ai.navigation;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class PathNavigation {
   private static final int MAX_TIME_RECOMPUTE = 20;
   private static final int STUCK_CHECK_INTERVAL = 100;
   private static final float STUCK_THRESHOLD_DISTANCE_FACTOR = 0.25F;
   protected final Mob mob;
   protected final Level level;
   @Nullable
   protected Path path;
   protected double speedModifier;
   protected int tick;
   protected int lastStuckCheck;
   protected Vec3 lastStuckCheckPos = Vec3.ZERO;
   protected Vec3i timeoutCachedNode = Vec3i.ZERO;
   protected long timeoutTimer;
   protected long lastTimeoutCheck;
   protected double timeoutLimit;
   protected float maxDistanceToWaypoint = 0.5F;
   /**
    * Whether the path can be changed by {@link net.minecraft.pathfinding.PathNavigate#onUpdateNavigation()
    * onUpdateNavigation()}
    */
   protected boolean hasDelayedRecomputation;
   protected long timeLastRecompute;
   protected NodeEvaluator nodeEvaluator;
   @Nullable
   private BlockPos targetPos;
   /** Distance in which a path point counts as target-reaching */
   private int reachRange;
   private float maxVisitedNodesMultiplier = 1.0F;
   private final PathFinder pathFinder;
   private boolean isStuck;

   public PathNavigation(Mob pMob, Level pLevel) {
      this.mob = pMob;
      this.level = pLevel;
      int i = Mth.floor(pMob.getAttributeValue(Attributes.FOLLOW_RANGE) * 16.0D);
      this.pathFinder = this.createPathFinder(i);
   }

   public void resetMaxVisitedNodesMultiplier() {
      this.maxVisitedNodesMultiplier = 1.0F;
   }

   public void setMaxVisitedNodesMultiplier(float pMultiplier) {
      this.maxVisitedNodesMultiplier = pMultiplier;
   }

   @Nullable
   public BlockPos getTargetPos() {
      return this.targetPos;
   }

   protected abstract PathFinder createPathFinder(int pMaxVisitedNodes);

   /**
    * Sets the speed
    */
   public void setSpeedModifier(double pSpeed) {
      this.speedModifier = pSpeed;
   }

   public void recomputePath() {
      if (this.level.getGameTime() - this.timeLastRecompute > 20L) {
         if (this.targetPos != null) {
            this.path = null;
            this.path = this.createPath(this.targetPos, this.reachRange);
            this.timeLastRecompute = this.level.getGameTime();
            this.hasDelayedRecomputation = false;
         }
      } else {
         this.hasDelayedRecomputation = true;
      }

   }

   /**
    * Returns path to given BlockPos
    */
   @Nullable
   public final Path createPath(double pX, double pY, double pZ, int pAccuracy) {
      return this.createPath(BlockPos.containing(pX, pY, pZ), pAccuracy);
   }

   /**
    * Returns a path to one of the elements of the stream or null
    */
   @Nullable
   public Path createPath(Stream<BlockPos> pTargets, int pAccuracy) {
      return this.createPath(pTargets.collect(Collectors.toSet()), 8, false, pAccuracy);
   }

   @Nullable
   public Path createPath(Set<BlockPos> pPositions, int pDistance) {
      return this.createPath(pPositions, 8, false, pDistance);
   }

   /**
    * Returns path to given BlockPos
    */
   @Nullable
   public Path createPath(BlockPos pPos, int pAccuracy) {
      return this.createPath(ImmutableSet.of(pPos), 8, false, pAccuracy);
   }

   @Nullable
   public Path createPath(BlockPos pPos, int pRegionOffset, int pAccuracy) {
      return this.createPath(ImmutableSet.of(pPos), 8, false, pRegionOffset, (float)pAccuracy);
   }

   /**
    * Returns a path to the given entity or null
    */
   @Nullable
   public Path createPath(Entity pEntity, int pAccuracy) {
      return this.createPath(ImmutableSet.of(pEntity.blockPosition()), 16, true, pAccuracy);
   }

   /**
    * Returns a path to one of the given targets or null
    */
   @Nullable
   protected Path createPath(Set<BlockPos> pTargets, int pRegionOffset, boolean pOffsetUpward, int pAccuracy) {
      return this.createPath(pTargets, pRegionOffset, pOffsetUpward, pAccuracy, (float)this.mob.getAttributeValue(Attributes.FOLLOW_RANGE));
   }

   @Nullable
   protected Path createPath(Set<BlockPos> pTargets, int pRegionOffset, boolean pOffsetUpward, int pAccuracy, float pFollowRange) {
      if (pTargets.isEmpty()) {
         return null;
      } else if (this.mob.getY() < (double)this.level.getMinBuildHeight()) {
         return null;
      } else if (!this.canUpdatePath()) {
         return null;
      } else if (this.path != null && !this.path.isDone() && pTargets.contains(this.targetPos)) {
         return this.path;
      } else {
         this.level.getProfiler().push("pathfind");
         BlockPos blockpos = pOffsetUpward ? this.mob.blockPosition().above() : this.mob.blockPosition();
         int i = (int)(pFollowRange + (float)pRegionOffset);
         PathNavigationRegion pathnavigationregion = new PathNavigationRegion(this.level, blockpos.offset(-i, -i, -i), blockpos.offset(i, i, i));
         Path path = this.pathFinder.findPath(pathnavigationregion, this.mob, pTargets, pFollowRange, pAccuracy, this.maxVisitedNodesMultiplier);
         this.level.getProfiler().pop();
         if (path != null && path.getTarget() != null) {
            this.targetPos = path.getTarget();
            this.reachRange = pAccuracy;
            this.resetStuckTimeout();
         }

         return path;
      }
   }

   /**
    * Try to find and set a path to XYZ. Returns {@code true} if successful.
    */
   public boolean moveTo(double pX, double pY, double pZ, double pSpeed) {
      return this.moveTo(this.createPath(pX, pY, pZ, 1), pSpeed);
   }

   /**
    * Try to find and set a path to EntityLiving. Returns {@code true} if successful.
    */
   public boolean moveTo(Entity pEntity, double pSpeed) {
      Path path = this.createPath(pEntity, 1);
      return path != null && this.moveTo(path, pSpeed);
   }

   /**
    * Sets a new path. If it's different from the old path. Checks to adjust path for sun avoiding, and stores start
    * coords.
    */
   public boolean moveTo(@Nullable Path pPathentity, double pSpeed) {
      if (pPathentity == null) {
         this.path = null;
         return false;
      } else {
         if (!pPathentity.sameAs(this.path)) {
            this.path = pPathentity;
         }

         if (this.isDone()) {
            return false;
         } else {
            this.trimPath();
            if (this.path.getNodeCount() <= 0) {
               return false;
            } else {
               this.speedModifier = pSpeed;
               Vec3 vec3 = this.getTempMobPos();
               this.lastStuckCheck = this.tick;
               this.lastStuckCheckPos = vec3;
               return true;
            }
         }
      }
   }

   /**
    * Gets the actively used {@link net.minecraft.world.level.pathfinder.Path}.
    */
   @Nullable
   public Path getPath() {
      return this.path;
   }

   public void tick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vec3 vec3 = this.getTempMobPos();
            Vec3 vec31 = this.path.getNextEntityPos(this.mob);
            if (vec3.y > vec31.y && !this.mob.onGround() && Mth.floor(vec3.x) == Mth.floor(vec31.x) && Mth.floor(vec3.z) == Mth.floor(vec31.z)) {
               this.path.advance();
            }
         }

         DebugPackets.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
         if (!this.isDone()) {
            Vec3 vec32 = this.path.getNextEntityPos(this.mob);
            this.mob.getMoveControl().setWantedPosition(vec32.x, this.getGroundY(vec32), vec32.z, this.speedModifier);
         }
      }
   }

   protected double getGroundY(Vec3 pVec) {
      BlockPos blockpos = BlockPos.containing(pVec);
      return this.level.getBlockState(blockpos.below()).isAir() ? pVec.y : WalkNodeEvaluator.getFloorLevel(this.level, blockpos);
   }

   protected void followThePath() {
      Vec3 vec3 = this.getTempMobPos();
      this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F ? this.mob.getBbWidth() / 2.0F : 0.75F - this.mob.getBbWidth() / 2.0F;
      Vec3i vec3i = this.path.getNextNodePos();
      double d0 = Math.abs(this.mob.getX() - ((double)vec3i.getX() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
      double d1 = Math.abs(this.mob.getY() - (double)vec3i.getY());
      double d2 = Math.abs(this.mob.getZ() - ((double)vec3i.getZ() + (this.mob.getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
      boolean flag = d0 <= (double)this.maxDistanceToWaypoint && d2 <= (double)this.maxDistanceToWaypoint && d1 < 1.0D; //Forge: Fix MC-94054
      if (flag || this.canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(vec3)) {
         this.path.advance();
      }

      this.doStuckDetection(vec3);
   }

   private boolean shouldTargetNextNodeInDirection(Vec3 pVec) {
      if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
         return false;
      } else {
         Vec3 vec3 = Vec3.atBottomCenterOf(this.path.getNextNodePos());
         if (!pVec.closerThan(vec3, 2.0D)) {
            return false;
         } else if (this.canMoveDirectly(pVec, this.path.getNextEntityPos(this.mob))) {
            return true;
         } else {
            Vec3 vec31 = Vec3.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
            Vec3 vec32 = vec3.subtract(pVec);
            Vec3 vec33 = vec31.subtract(pVec);
            double d0 = vec32.lengthSqr();
            double d1 = vec33.lengthSqr();
            boolean flag = d1 < d0;
            boolean flag1 = d0 < 0.5D;
            if (!flag && !flag1) {
               return false;
            } else {
               Vec3 vec34 = vec32.normalize();
               Vec3 vec35 = vec33.normalize();
               return vec35.dot(vec34) < 0.0D;
            }
         }
      }
   }

   /**
    * Checks if entity haven't been moved when last checked and if so, stops the current navigation.
    */
   protected void doStuckDetection(Vec3 pPositionVec3) {
      if (this.tick - this.lastStuckCheck > 100) {
         float f = this.mob.getSpeed() >= 1.0F ? this.mob.getSpeed() : this.mob.getSpeed() * this.mob.getSpeed();
         float f1 = f * 100.0F * 0.25F;
         if (pPositionVec3.distanceToSqr(this.lastStuckCheckPos) < (double)(f1 * f1)) {
            this.isStuck = true;
            this.stop();
         } else {
            this.isStuck = false;
         }

         this.lastStuckCheck = this.tick;
         this.lastStuckCheckPos = pPositionVec3;
      }

      if (this.path != null && !this.path.isDone()) {
         Vec3i vec3i = this.path.getNextNodePos();
         long i = this.level.getGameTime();
         if (vec3i.equals(this.timeoutCachedNode)) {
            this.timeoutTimer += i - this.lastTimeoutCheck;
         } else {
            this.timeoutCachedNode = vec3i;
            double d0 = pPositionVec3.distanceTo(Vec3.atBottomCenterOf(this.timeoutCachedNode));
            this.timeoutLimit = this.mob.getSpeed() > 0.0F ? d0 / (double)this.mob.getSpeed() * 20.0D : 0.0D;
         }

         if (this.timeoutLimit > 0.0D && (double)this.timeoutTimer > this.timeoutLimit * 3.0D) {
            this.timeoutPath();
         }

         this.lastTimeoutCheck = i;
      }

   }

   private void timeoutPath() {
      this.resetStuckTimeout();
      this.stop();
   }

   private void resetStuckTimeout() {
      this.timeoutCachedNode = Vec3i.ZERO;
      this.timeoutTimer = 0L;
      this.timeoutLimit = 0.0D;
      this.isStuck = false;
   }

   /**
    * If null path or reached the end
    */
   public boolean isDone() {
      return this.path == null || this.path.isDone();
   }

   public boolean isInProgress() {
      return !this.isDone();
   }

   /**
    * Sets the active {@link net.minecraft.world.level.pathfinder.Path} to {@code null}.
    */
   public void stop() {
      this.path = null;
   }

   protected abstract Vec3 getTempMobPos();

   /**
    * If on ground or swimming and can swim
    */
   protected abstract boolean canUpdatePath();

   /**
    * Returns {@code true} if the entity is in water or lava, {@code false} otherwise.
    */
   protected boolean isInLiquid() {
      return this.mob.isInWaterOrBubble() || this.mob.isInLava();
   }

   /**
    * Trims path data from the end to the first sun covered block
    */
   protected void trimPath() {
      if (this.path != null) {
         for(int i = 0; i < this.path.getNodeCount(); ++i) {
            Node node = this.path.getNode(i);
            Node node1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
            BlockState blockstate = this.level.getBlockState(new BlockPos(node.x, node.y, node.z));
            if (blockstate.is(BlockTags.CAULDRONS)) {
               this.path.replaceNode(i, node.cloneAndMove(node.x, node.y + 1, node.z));
               if (node1 != null && node.y >= node1.y) {
                  this.path.replaceNode(i + 1, node.cloneAndMove(node1.x, node.y + 1, node1.z));
               }
            }
         }

      }
   }

   /**
    * Checks if the specified entity can safely walk to the specified location.
    */
   protected boolean canMoveDirectly(Vec3 pPosVec31, Vec3 pPosVec32) {
      return false;
   }

   public boolean canCutCorner(BlockPathTypes pPathType) {
      return pPathType != BlockPathTypes.DANGER_FIRE && pPathType != BlockPathTypes.DANGER_OTHER && pPathType != BlockPathTypes.WALKABLE_DOOR;
   }

   protected static boolean isClearForMovementBetween(Mob pMob, Vec3 pPos1, Vec3 pPos2, boolean pAllowSwimming) {
      Vec3 vec3 = new Vec3(pPos2.x, pPos2.y + (double)pMob.getBbHeight() * 0.5D, pPos2.z);
      return pMob.level().clip(new ClipContext(pPos1, vec3, ClipContext.Block.COLLIDER, pAllowSwimming ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, pMob)).getType() == HitResult.Type.MISS;
   }

   public boolean isStableDestination(BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      return this.level.getBlockState(blockpos).isSolidRender(this.level, blockpos);
   }

   public NodeEvaluator getNodeEvaluator() {
      return this.nodeEvaluator;
   }

   public void setCanFloat(boolean pCanSwim) {
      this.nodeEvaluator.setCanFloat(pCanSwim);
   }

   public boolean canFloat() {
      return this.nodeEvaluator.canFloat();
   }

   public boolean shouldRecomputePath(BlockPos pPos) {
      if (this.hasDelayedRecomputation) {
         return false;
      } else if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
         Node node = this.path.getEndNode();
         Vec3 vec3 = new Vec3(((double)node.x + this.mob.getX()) / 2.0D, ((double)node.y + this.mob.getY()) / 2.0D, ((double)node.z + this.mob.getZ()) / 2.0D);
         return pPos.closerToCenterThan(vec3, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()));
      } else {
         return false;
      }
   }

   public float getMaxDistanceToWaypoint() {
      return this.maxDistanceToWaypoint;
   }

   public boolean isStuck() {
      return this.isStuck;
   }
}
