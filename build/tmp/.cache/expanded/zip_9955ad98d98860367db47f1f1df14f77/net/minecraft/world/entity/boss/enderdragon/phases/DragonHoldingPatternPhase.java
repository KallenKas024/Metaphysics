package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonHoldingPatternPhase extends AbstractDragonPhaseInstance {
   private static final TargetingConditions NEW_TARGET_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
   @Nullable
   private Path currentPath;
   @Nullable
   private Vec3 targetLocation;
   private boolean clockwise;

   public DragonHoldingPatternPhase(EnderDragon pDragon) {
      super(pDragon);
   }

   public EnderDragonPhase<DragonHoldingPatternPhase> getPhase() {
      return EnderDragonPhase.HOLDING_PATTERN;
   }

   /**
    * Gives the phase a chance to update its status.
    * Called by dragon's onLivingUpdate. Only used when !worldObj.isRemote.
    */
   public void doServerTick() {
      double d0 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
      if (d0 < 100.0D || d0 > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
         this.findNewTarget();
      }

   }

   /**
    * Called when this phase is set to active
    */
   public void begin() {
      this.currentPath = null;
      this.targetLocation = null;
   }

   /**
    * Returns the location the dragon is flying toward
    */
   @Nullable
   public Vec3 getFlyTargetLocation() {
      return this.targetLocation;
   }

   private void findNewTarget() {
      if (this.currentPath != null && this.currentPath.isDone()) {
         BlockPos blockpos = this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(EndPodiumFeature.getLocation(this.dragon.getFightOrigin())));
         int i = this.dragon.getDragonFight() == null ? 0 : this.dragon.getDragonFight().getCrystalsAlive();
         if (this.dragon.getRandom().nextInt(i + 3) == 0) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING_APPROACH);
            return;
         }

         Player player = this.dragon.level().getNearestPlayer(NEW_TARGET_TARGETING, this.dragon, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
         double d0;
         if (player != null) {
            d0 = blockpos.distToCenterSqr(player.position()) / 512.0D;
         } else {
            d0 = 64.0D;
         }

         if (player != null && (this.dragon.getRandom().nextInt((int)(d0 + 2.0D)) == 0 || this.dragon.getRandom().nextInt(i + 2) == 0)) {
            this.strafePlayer(player);
            return;
         }
      }

      if (this.currentPath == null || this.currentPath.isDone()) {
         int j = this.dragon.findClosestNode();
         int k = j;
         if (this.dragon.getRandom().nextInt(8) == 0) {
            this.clockwise = !this.clockwise;
            k = j + 6;
         }

         if (this.clockwise) {
            ++k;
         } else {
            --k;
         }

         if (this.dragon.getDragonFight() != null && this.dragon.getDragonFight().getCrystalsAlive() >= 0) {
            k %= 12;
            if (k < 0) {
               k += 12;
            }
         } else {
            k -= 12;
            k &= 7;
            k += 12;
         }

         this.currentPath = this.dragon.findPath(j, k, (Node)null);
         if (this.currentPath != null) {
            this.currentPath.advance();
         }
      }

      this.navigateToNextPathNode();
   }

   private void strafePlayer(Player pPlayer) {
      this.dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
      this.dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER).setTarget(pPlayer);
   }

   private void navigateToNextPathNode() {
      if (this.currentPath != null && !this.currentPath.isDone()) {
         Vec3i vec3i = this.currentPath.getNextNodePos();
         this.currentPath.advance();
         double d0 = (double)vec3i.getX();
         double d1 = (double)vec3i.getZ();

         double d2;
         do {
            d2 = (double)((float)vec3i.getY() + this.dragon.getRandom().nextFloat() * 20.0F);
         } while(d2 < (double)vec3i.getY());

         this.targetLocation = new Vec3(d0, d2, d1);
      }

   }

   public void onCrystalDestroyed(EndCrystal pCrystal, BlockPos pPos, DamageSource pDmgSrc, @Nullable Player pPlyr) {
      if (pPlyr != null && this.dragon.canAttack(pPlyr)) {
         this.strafePlayer(pPlyr);
      }

   }
}