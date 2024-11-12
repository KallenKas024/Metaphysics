package net.minecraft.world.entity.monster.piglin;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public abstract class AbstractPiglin extends Monster {
   protected static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(AbstractPiglin.class, EntityDataSerializers.BOOLEAN);
   protected static final int CONVERSION_TIME = 300;
   protected static final float PIGLIN_EYE_HEIGHT = 1.79F;
   protected int timeInOverworld;

   public AbstractPiglin(EntityType<? extends AbstractPiglin> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.setCanPickUpLoot(true);
      this.applyOpenDoorsAbility();
      this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
   }

   private void applyOpenDoorsAbility() {
      if (GoalUtils.hasGroundPathNavigation(this)) {
         ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
      }

   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
      return 1.79F;
   }

   protected abstract boolean canHunt();

   public void setImmuneToZombification(boolean pImmuneToZombification) {
      this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, pImmuneToZombification);
   }

   protected boolean isImmuneToZombification() {
      return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.isImmuneToZombification()) {
         pCompound.putBoolean("IsImmuneToZombification", true);
      }

      pCompound.putInt("TimeInOverworld", this.timeInOverworld);
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return this.isBaby() ? -0.05D : -0.45D;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setImmuneToZombification(pCompound.getBoolean("IsImmuneToZombification"));
      this.timeInOverworld = pCompound.getInt("TimeInOverworld");
   }

   protected void customServerAiStep() {
      super.customServerAiStep();
      if (this.isConverting()) {
         ++this.timeInOverworld;
      } else {
         this.timeInOverworld = 0;
         this.timeInOverworld = 0;
      }

      if (this.timeInOverworld > 300 && net.minecraftforge.event.ForgeEventFactory.canLivingConvert(this, EntityType.ZOMBIFIED_PIGLIN, (timer) -> this.timeInOverworld = timer)) {
         this.playConvertedSound();
         this.finishConversion((ServerLevel)this.level());
      }

   }

   public boolean isConverting() {
      return !this.level().dimensionType().piglinSafe() && !this.isImmuneToZombification() && !this.isNoAi();
   }

   protected void finishConversion(ServerLevel pServerLevel) {
      ZombifiedPiglin zombifiedpiglin = this.convertTo(EntityType.ZOMBIFIED_PIGLIN, true);
      if (zombifiedpiglin != null) {
         zombifiedpiglin.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
         net.minecraftforge.event.ForgeEventFactory.onLivingConvert(this, zombifiedpiglin);
      }

   }

   public boolean isAdult() {
      return !this.isBaby();
   }

   public abstract PiglinArmPose getArmPose();

   /**
    * Gets the active target the Goal system uses for tracking
    */
   @Nullable
   public LivingEntity getTarget() {
      return this.brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse((LivingEntity)null);
   }

   protected boolean isHoldingMeleeWeapon() {
      return this.getMainHandItem().getItem() instanceof TieredItem;
   }

   /**
    * Plays living's sound at its position
    */
   public void playAmbientSound() {
      if (PiglinAi.isIdle(this)) {
         super.playAmbientSound();
      }

   }

   protected void sendDebugPackets() {
      super.sendDebugPackets();
      DebugPackets.sendEntityBrain(this);
   }

   protected abstract void playConvertedSound();
}
