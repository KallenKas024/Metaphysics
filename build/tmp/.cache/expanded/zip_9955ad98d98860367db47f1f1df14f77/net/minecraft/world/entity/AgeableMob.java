package net.minecraft.world.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public abstract class AgeableMob extends PathfinderMob {
   private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(AgeableMob.class, EntityDataSerializers.BOOLEAN);
   public static final int BABY_START_AGE = -24000;
   private static final int FORCED_AGE_PARTICLE_TICKS = 40;
   protected int age;
   protected int forcedAge;
   protected int forcedAgeTimer;

   protected AgeableMob(EntityType<? extends AgeableMob> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
   }

   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableMob.AgeableMobGroupData(true);
      }

      AgeableMob.AgeableMobGroupData ageablemob$ageablemobgroupdata = (AgeableMob.AgeableMobGroupData)pSpawnData;
      if (ageablemob$ageablemobgroupdata.isShouldSpawnBaby() && ageablemob$ageablemobgroupdata.getGroupSize() > 0 && pLevel.getRandom().nextFloat() <= ageablemob$ageablemobgroupdata.getBabySpawnChance()) {
         this.setAge(-24000);
      }

      ageablemob$ageablemobgroupdata.increaseGroupSizeByOne();
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   @Nullable
   public abstract AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent);

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
   }

   public boolean canBreed() {
      return false;
   }

   public int getAge() {
      if (this.level().isClientSide) {
         return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
      } else {
         return this.age;
      }
   }

   public void ageUp(int pAmount, boolean pForced) {
      int i = this.getAge();
      i += pAmount * 20;
      if (i > 0) {
         i = 0;
      }

      int j = i - i;
      this.setAge(i);
      if (pForced) {
         this.forcedAge += j;
         if (this.forcedAgeTimer == 0) {
            this.forcedAgeTimer = 40;
         }
      }

      if (this.getAge() == 0) {
         this.setAge(this.forcedAge);
      }

   }

   public void ageUp(int pAmount) {
      this.ageUp(pAmount, false);
   }

   public void setAge(int pAge) {
      int i = this.getAge();
      this.age = pAge;
      if (i < 0 && pAge >= 0 || i >= 0 && pAge < 0) {
         this.entityData.set(DATA_BABY_ID, pAge < 0);
         this.ageBoundaryReached();
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Age", this.getAge());
      pCompound.putInt("ForcedAge", this.forcedAge);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setAge(pCompound.getInt("Age"));
      this.forcedAge = pCompound.getInt("ForcedAge");
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.level().isClientSide) {
         if (this.forcedAgeTimer > 0) {
            if (this.forcedAgeTimer % 4 == 0) {
               this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            }

            --this.forcedAgeTimer;
         }
      } else if (this.isAlive()) {
         int i = this.getAge();
         if (i < 0) {
            ++i;
            this.setAge(i);
         } else if (i > 0) {
            --i;
            this.setAge(i);
         }
      }

   }

   protected void ageBoundaryReached() {
      if (!this.isBaby() && this.isPassenger()) {
         Entity entity = this.getVehicle();
         if (entity instanceof Boat) {
            Boat boat = (Boat)entity;
            if (!boat.hasEnoughSpaceFor(this)) {
               this.stopRiding();
            }
         }
      }

   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getAge() < 0;
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pBaby) {
      this.setAge(pBaby ? -24000 : 0);
   }

   public static int getSpeedUpSecondsWhenFeeding(int pTicksUntilAdult) {
      return (int)((float)(pTicksUntilAdult / 20) * 0.1F);
   }

   public static class AgeableMobGroupData implements SpawnGroupData {
      private int groupSize;
      private final boolean shouldSpawnBaby;
      private final float babySpawnChance;

      private AgeableMobGroupData(boolean pShouldSpawnBaby, float pBabySpawnChance) {
         this.shouldSpawnBaby = pShouldSpawnBaby;
         this.babySpawnChance = pBabySpawnChance;
      }

      public AgeableMobGroupData(boolean pShouldSpawnBaby) {
         this(pShouldSpawnBaby, 0.05F);
      }

      public AgeableMobGroupData(float pBabySpawnChance) {
         this(true, pBabySpawnChance);
      }

      public int getGroupSize() {
         return this.groupSize;
      }

      public void increaseGroupSizeByOne() {
         ++this.groupSize;
      }

      public boolean isShouldSpawnBaby() {
         return this.shouldSpawnBaby;
      }

      public float getBabySpawnChance() {
         return this.babySpawnChance;
      }
   }
}