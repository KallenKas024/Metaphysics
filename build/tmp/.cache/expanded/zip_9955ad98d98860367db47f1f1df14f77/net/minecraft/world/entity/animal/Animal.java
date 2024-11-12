package net.minecraft.world.entity.animal;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class Animal extends AgeableMob {
   protected static final int PARENT_AGE_AFTER_BREEDING = 6000;
   private int inLove;
   @Nullable
   private UUID loveCause;

   protected Animal(EntityType<? extends Animal> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
   }

   protected void customServerAiStep() {
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      super.customServerAiStep();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      if (this.inLove > 0) {
         --this.inLove;
         if (this.inLove % 10 == 0) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
         }
      }

   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         this.inLove = 0;
         return super.hurt(pSource, pAmount);
      }
   }

   public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
      return pLevel.getBlockState(pPos.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : pLevel.getPathfindingCostFromLightLevels(pPos);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("InLove", this.inLove);
      if (this.loveCause != null) {
         pCompound.putUUID("LoveCause", this.loveCause);
      }

   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return 0.14D;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.inLove = pCompound.getInt("InLove");
      this.loveCause = pCompound.hasUUID("LoveCause") ? pCompound.getUUID("LoveCause") : null;
   }

   public static boolean checkAnimalSpawnRules(EntityType<? extends Animal> pAnimal, LevelAccessor pLevel, MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
      return pLevel.getBlockState(pPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && isBrightEnoughToSpawn(pLevel, pPos);
   }

   protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter pLevel, BlockPos pPos) {
      return pLevel.getRawBrightness(pPos, 0) > 8;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 120;
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return false;
   }

   public int getExperienceReward() {
      return 1 + this.level().random.nextInt(3);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.is(Items.WHEAT);
   }

   public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (this.isFood(itemstack)) {
         int i = this.getAge();
         if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
            this.usePlayerItem(pPlayer, pHand, itemstack);
            this.setInLove(pPlayer);
            return InteractionResult.SUCCESS;
         }

         if (this.isBaby()) {
            this.usePlayerItem(pPlayer, pHand, itemstack);
            this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
         }

         if (this.level().isClientSide) {
            return InteractionResult.CONSUME;
         }
      }

      return super.mobInteract(pPlayer, pHand);
   }

   protected void usePlayerItem(Player pPlayer, InteractionHand pHand, ItemStack pStack) {
      if (!pPlayer.getAbilities().instabuild) {
         pStack.shrink(1);
      }

   }

   public boolean canFallInLove() {
      return this.inLove <= 0;
   }

   public void setInLove(@Nullable Player pPlayer) {
      this.inLove = 600;
      if (pPlayer != null) {
         this.loveCause = pPlayer.getUUID();
      }

      this.level().broadcastEntityEvent(this, (byte)18);
   }

   public void setInLoveTime(int pInLove) {
      this.inLove = pInLove;
   }

   public int getInLoveTime() {
      return this.inLove;
   }

   @Nullable
   public ServerPlayer getLoveCause() {
      if (this.loveCause == null) {
         return null;
      } else {
         Player player = this.level().getPlayerByUUID(this.loveCause);
         return player instanceof ServerPlayer ? (ServerPlayer)player : null;
      }
   }

   /**
    * Returns if the entity is currently in 'love mode'.
    */
   public boolean isInLove() {
      return this.inLove > 0;
   }

   public void resetLove() {
      this.inLove = 0;
   }

   /**
    * Returns {@code true} if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(Animal pOtherAnimal) {
      if (pOtherAnimal == this) {
         return false;
      } else if (pOtherAnimal.getClass() != this.getClass()) {
         return false;
      } else {
         return this.isInLove() && pOtherAnimal.isInLove();
      }
   }

   public void spawnChildFromBreeding(ServerLevel pLevel, Animal pMate) {
      AgeableMob ageablemob = this.getBreedOffspring(pLevel, pMate);
      final net.minecraftforge.event.entity.living.BabyEntitySpawnEvent event = new net.minecraftforge.event.entity.living.BabyEntitySpawnEvent(this, pMate, ageablemob);
      final boolean cancelled = net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
      ageablemob = event.getChild();
      if (cancelled) {
         //Reset the "inLove" state for the animals
         this.setAge(6000);
         pMate.setAge(6000);
         this.resetLove();
         pMate.resetLove();
         return;
      }
      if (ageablemob != null) {
         ageablemob.setBaby(true);
         ageablemob.moveTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         this.finalizeSpawnChildFromBreeding(pLevel, pMate, ageablemob);
         pLevel.addFreshEntityWithPassengers(ageablemob);
      }
   }

   public void finalizeSpawnChildFromBreeding(ServerLevel pLevel, Animal pAnimal, @Nullable AgeableMob pBaby) {
      Optional.ofNullable(this.getLoveCause()).or(() -> {
         return Optional.ofNullable(pAnimal.getLoveCause());
      }).ifPresent((p_277486_) -> {
         p_277486_.awardStat(Stats.ANIMALS_BRED);
         CriteriaTriggers.BRED_ANIMALS.trigger(p_277486_, this, pAnimal, pBaby);
      });
      this.setAge(6000);
      pAnimal.setAge(6000);
      this.resetLove();
      pAnimal.resetLove();
      pLevel.broadcastEntityEvent(this, (byte)18);
      if (pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         pLevel.addFreshEntity(new ExperienceOrb(pLevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
      }

   }

   /**
    * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
    */
   public void handleEntityEvent(byte pId) {
      if (pId == 18) {
         for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
         }
      } else {
         super.handleEntityEvent(pId);
      }

   }
}
