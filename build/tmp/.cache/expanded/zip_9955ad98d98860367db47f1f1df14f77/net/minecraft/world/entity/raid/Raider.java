package net.minecraft.world.entity.raid;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PathfindToRaidGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public abstract class Raider extends PatrollingMonster {
   protected static final EntityDataAccessor<Boolean> IS_CELEBRATING = SynchedEntityData.defineId(Raider.class, EntityDataSerializers.BOOLEAN);
   static final Predicate<ItemEntity> ALLOWED_ITEMS = (p_289494_) -> {
      return !p_289494_.hasPickUpDelay() && p_289494_.isAlive() && ItemStack.matches(p_289494_.getItem(), Raid.getLeaderBannerInstance());
   };
   @Nullable
   protected Raid raid;
   private int wave;
   private boolean canJoinRaid;
   private int ticksOutsideRaid;

   protected Raider(EntityType<? extends Raider> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new Raider.ObtainRaidLeaderBannerGoal<>(this));
      this.goalSelector.addGoal(3, new PathfindToRaidGoal<>(this));
      this.goalSelector.addGoal(4, new Raider.RaiderMoveThroughVillageGoal(this, (double)1.05F, 1));
      this.goalSelector.addGoal(5, new Raider.RaiderCelebration(this));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(IS_CELEBRATING, false);
   }

   public abstract void applyRaidBuffs(int pWave, boolean pUnusedFalse);

   public boolean canJoinRaid() {
      return this.canJoinRaid;
   }

   public void setCanJoinRaid(boolean pCanJoinRaid) {
      this.canJoinRaid = pCanJoinRaid;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.level() instanceof ServerLevel && this.isAlive()) {
         Raid raid = this.getCurrentRaid();
         if (this.canJoinRaid()) {
            if (raid == null) {
               if (this.level().getGameTime() % 20L == 0L) {
                  Raid raid1 = ((ServerLevel)this.level()).getRaidAt(this.blockPosition());
                  if (raid1 != null && Raids.canJoinRaid(this, raid1)) {
                     raid1.joinRaid(raid1.getGroupsSpawned(), this, (BlockPos)null, true);
                  }
               }
            } else {
               LivingEntity livingentity = this.getTarget();
               if (livingentity != null && (livingentity.getType() == EntityType.PLAYER || livingentity.getType() == EntityType.IRON_GOLEM)) {
                  this.noActionTime = 0;
               }
            }
         }
      }

      super.aiStep();
   }

   protected void updateNoActionTime() {
      this.noActionTime += 2;
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      if (this.level() instanceof ServerLevel) {
         Entity entity = pCause.getEntity();
         Raid raid = this.getCurrentRaid();
         if (raid != null) {
            if (this.isPatrolLeader()) {
               raid.removeLeader(this.getWave());
            }

            if (entity != null && entity.getType() == EntityType.PLAYER) {
               raid.addHeroOfTheVillage(entity);
            }

            raid.removeFromRaid(this, false);
         }

         if (this.isPatrolLeader() && raid == null && ((ServerLevel)this.level()).getRaidAt(this.blockPosition()) == null) {
            ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
            Player player = null;
            if (entity instanceof Player) {
               player = (Player)entity;
            } else if (entity instanceof Wolf) {
               Wolf wolf = (Wolf)entity;
               LivingEntity livingentity = wolf.getOwner();
               if (wolf.isTame() && livingentity instanceof Player) {
                  player = (Player)livingentity;
               }
            }

            if (!itemstack.isEmpty() && ItemStack.matches(itemstack, Raid.getLeaderBannerInstance()) && player != null) {
               MobEffectInstance mobeffectinstance1 = player.getEffect(MobEffects.BAD_OMEN);
               int i = 1;
               if (mobeffectinstance1 != null) {
                  i += mobeffectinstance1.getAmplifier();
                  player.removeEffectNoUpdate(MobEffects.BAD_OMEN);
               } else {
                  --i;
               }

               i = Mth.clamp(i, 0, 4);
               MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.BAD_OMEN, 120000, i, false, false, true);
               if (!this.level().getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                  player.addEffect(mobeffectinstance);
               }
            }
         }
      }

      super.die(pCause);
   }

   public boolean canJoinPatrol() {
      return !this.hasActiveRaid();
   }

   public void setCurrentRaid(@Nullable Raid pRaid) {
      this.raid = pRaid;
   }

   @Nullable
   public Raid getCurrentRaid() {
      return this.raid;
   }

   public boolean hasActiveRaid() {
      return this.getCurrentRaid() != null && this.getCurrentRaid().isActive();
   }

   public void setWave(int pWave) {
      this.wave = pWave;
   }

   public int getWave() {
      return this.wave;
   }

   public boolean isCelebrating() {
      return this.entityData.get(IS_CELEBRATING);
   }

   public void setCelebrating(boolean pCelebrating) {
      this.entityData.set(IS_CELEBRATING, pCelebrating);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Wave", this.wave);
      pCompound.putBoolean("CanJoinRaid", this.canJoinRaid);
      if (this.raid != null) {
         pCompound.putInt("RaidId", this.raid.getId());
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.wave = pCompound.getInt("Wave");
      this.canJoinRaid = pCompound.getBoolean("CanJoinRaid");
      if (pCompound.contains("RaidId", 3)) {
         if (this.level() instanceof ServerLevel) {
            this.raid = ((ServerLevel)this.level()).getRaids().get(pCompound.getInt("RaidId"));
         }

         if (this.raid != null) {
            this.raid.addWaveMob(this.wave, this, false);
            if (this.isPatrolLeader()) {
               this.raid.setLeader(this.wave, this);
            }
         }
      }

   }

   /**
    * Tests if this entity should pick up a weapon or an armor piece. Entity drops current weapon or armor if the new
    * one is better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      ItemStack itemstack = pItemEntity.getItem();
      boolean flag = this.hasActiveRaid() && this.getCurrentRaid().getLeader(this.getWave()) != null;
      if (this.hasActiveRaid() && !flag && ItemStack.matches(itemstack, Raid.getLeaderBannerInstance())) {
         EquipmentSlot equipmentslot = EquipmentSlot.HEAD;
         ItemStack itemstack1 = this.getItemBySlot(equipmentslot);
         double d0 = (double)this.getEquipmentDropChance(equipmentslot);
         if (!itemstack1.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1F, 0.0F) < d0) {
            this.spawnAtLocation(itemstack1);
         }

         this.onItemPickup(pItemEntity);
         this.setItemSlot(equipmentslot, itemstack);
         this.take(pItemEntity, itemstack.getCount());
         pItemEntity.discard();
         this.getCurrentRaid().setLeader(this.getWave(), this);
         this.setPatrolLeader(true);
      } else {
         super.pickUpItem(pItemEntity);
      }

   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return this.getCurrentRaid() == null ? super.removeWhenFarAway(pDistanceToClosestPlayer) : false;
   }

   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.getCurrentRaid() != null;
   }

   public int getTicksOutsideRaid() {
      return this.ticksOutsideRaid;
   }

   public void setTicksOutsideRaid(int pTicksOutsideRaid) {
      this.ticksOutsideRaid = pTicksOutsideRaid;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.hasActiveRaid()) {
         this.getCurrentRaid().updateBossbar();
      }

      return super.hurt(pSource, pAmount);
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      this.setCanJoinRaid(this.getType() != EntityType.WITCH || pReason != MobSpawnType.NATURAL);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public abstract SoundEvent getCelebrateSound();

   protected class HoldGroundAttackGoal extends Goal {
      private final Raider mob;
      private final float hostileRadiusSqr;
      public final TargetingConditions shoutTargeting = TargetingConditions.forNonCombat().range(8.0D).ignoreLineOfSight().ignoreInvisibilityTesting();

      public HoldGroundAttackGoal(AbstractIllager pMob, float pHostileRadiusSquare) {
         this.mob = pMob;
         this.hostileRadiusSqr = pHostileRadiusSquare * pHostileRadiusSquare;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         LivingEntity livingentity = this.mob.getLastHurtByMob();
         return this.mob.getCurrentRaid() == null && this.mob.isPatrolling() && this.mob.getTarget() != null && !this.mob.isAggressive() && (livingentity == null || livingentity.getType() != EntityType.PLAYER);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.mob.getNavigation().stop();

         for(Raider raider : this.mob.level().getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
            raider.setTarget(this.mob.getTarget());
         }

      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         super.stop();
         LivingEntity livingentity = this.mob.getTarget();
         if (livingentity != null) {
            for(Raider raider : this.mob.level().getNearbyEntities(Raider.class, this.shoutTargeting, this.mob, this.mob.getBoundingBox().inflate(8.0D, 8.0D, 8.0D))) {
               raider.setTarget(livingentity);
               raider.setAggressive(true);
            }

            this.mob.setAggressive(true);
         }

      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = this.mob.getTarget();
         if (livingentity != null) {
            if (this.mob.distanceToSqr(livingentity) > (double)this.hostileRadiusSqr) {
               this.mob.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
               if (this.mob.random.nextInt(50) == 0) {
                  this.mob.playAmbientSound();
               }
            } else {
               this.mob.setAggressive(true);
            }

            super.tick();
         }
      }
   }

   public class ObtainRaidLeaderBannerGoal<T extends Raider> extends Goal {
      private final T mob;

      public ObtainRaidLeaderBannerGoal(T pMob) {
         this.mob = pMob;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         Raid raid = this.mob.getCurrentRaid();
         if (this.mob.hasActiveRaid() && !this.mob.getCurrentRaid().isOver() && this.mob.canBeLeader() && !ItemStack.matches(this.mob.getItemBySlot(EquipmentSlot.HEAD), Raid.getLeaderBannerInstance())) {
            Raider raider = raid.getLeader(this.mob.getWave());
            if (raider == null || !raider.isAlive()) {
               List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(16.0D, 8.0D, 16.0D), Raider.ALLOWED_ITEMS);
               if (!list.isEmpty()) {
                  return this.mob.getNavigation().moveTo(list.get(0), (double)1.15F);
               }
            }

            return false;
         } else {
            return false;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.mob.getNavigation().getTargetPos().closerToCenterThan(this.mob.position(), 1.414D)) {
            List<ItemEntity> list = this.mob.level().getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(4.0D, 4.0D, 4.0D), Raider.ALLOWED_ITEMS);
            if (!list.isEmpty()) {
               this.mob.pickUpItem(list.get(0));
            }
         }

      }
   }

   public class RaiderCelebration extends Goal {
      private final Raider mob;

      RaiderCelebration(Raider pMob) {
         this.mob = pMob;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         Raid raid = this.mob.getCurrentRaid();
         return this.mob.isAlive() && this.mob.getTarget() == null && raid != null && raid.isLoss();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.mob.setCelebrating(true);
         super.start();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.mob.setCelebrating(false);
         super.stop();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (!this.mob.isSilent() && this.mob.random.nextInt(this.adjustedTickDelay(100)) == 0) {
            Raider.this.playSound(Raider.this.getCelebrateSound(), Raider.this.getSoundVolume(), Raider.this.getVoicePitch());
         }

         if (!this.mob.isPassenger() && this.mob.random.nextInt(this.adjustedTickDelay(50)) == 0) {
            this.mob.getJumpControl().jump();
         }

         super.tick();
      }
   }

   static class RaiderMoveThroughVillageGoal extends Goal {
      private final Raider raider;
      private final double speedModifier;
      private BlockPos poiPos;
      private final List<BlockPos> visited = Lists.newArrayList();
      private final int distanceToPoi;
      private boolean stuck;

      public RaiderMoveThroughVillageGoal(Raider pRaider, double pSpeedModifier, int pDistanceToPoi) {
         this.raider = pRaider;
         this.speedModifier = pSpeedModifier;
         this.distanceToPoi = pDistanceToPoi;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         this.updateVisited();
         return this.isValidRaid() && this.hasSuitablePoi() && this.raider.getTarget() == null;
      }

      private boolean isValidRaid() {
         return this.raider.hasActiveRaid() && !this.raider.getCurrentRaid().isOver();
      }

      private boolean hasSuitablePoi() {
         ServerLevel serverlevel = (ServerLevel)this.raider.level();
         BlockPos blockpos = this.raider.blockPosition();
         Optional<BlockPos> optional = serverlevel.getPoiManager().getRandom((p_219843_) -> {
            return p_219843_.is(PoiTypes.HOME);
         }, this::hasNotVisited, PoiManager.Occupancy.ANY, blockpos, 48, this.raider.random);
         if (!optional.isPresent()) {
            return false;
         } else {
            this.poiPos = optional.get().immutable();
            return true;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (this.raider.getNavigation().isDone()) {
            return false;
         } else {
            return this.raider.getTarget() == null && !this.poiPos.closerToCenterThan(this.raider.position(), (double)(this.raider.getBbWidth() + (float)this.distanceToPoi)) && !this.stuck;
         }
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         if (this.poiPos.closerToCenterThan(this.raider.position(), (double)this.distanceToPoi)) {
            this.visited.add(this.poiPos);
         }

      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         super.start();
         this.raider.setNoActionTime(0);
         this.raider.getNavigation().moveTo((double)this.poiPos.getX(), (double)this.poiPos.getY(), (double)this.poiPos.getZ(), this.speedModifier);
         this.stuck = false;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.raider.getNavigation().isDone()) {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.poiPos);
            Vec3 vec31 = DefaultRandomPos.getPosTowards(this.raider, 16, 7, vec3, (double)((float)Math.PI / 10F));
            if (vec31 == null) {
               vec31 = DefaultRandomPos.getPosTowards(this.raider, 8, 7, vec3, (double)((float)Math.PI / 2F));
            }

            if (vec31 == null) {
               this.stuck = true;
               return;
            }

            this.raider.getNavigation().moveTo(vec31.x, vec31.y, vec31.z, this.speedModifier);
         }

      }

      private boolean hasNotVisited(BlockPos p_37943_) {
         for(BlockPos blockpos : this.visited) {
            if (Objects.equals(p_37943_, blockpos)) {
               return false;
            }
         }

         return true;
      }

      private void updateVisited() {
         if (this.visited.size() > 2) {
            this.visited.remove(0);
         }

      }
   }
}