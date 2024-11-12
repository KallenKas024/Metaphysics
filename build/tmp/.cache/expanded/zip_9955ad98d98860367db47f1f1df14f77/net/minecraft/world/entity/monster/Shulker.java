package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Shulker extends AbstractGolem implements VariantHolder<Optional<DyeColor>>, Enemy {
   private static final UUID COVERED_ARMOR_MODIFIER_UUID = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F");
   private static final AttributeModifier COVERED_ARMOR_MODIFIER = new AttributeModifier(COVERED_ARMOR_MODIFIER_UUID, "Covered armor bonus", 20.0D, AttributeModifier.Operation.ADDITION);
   protected static final EntityDataAccessor<Direction> DATA_ATTACH_FACE_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.DIRECTION);
   protected static final EntityDataAccessor<Byte> DATA_PEEK_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Byte> DATA_COLOR_ID = SynchedEntityData.defineId(Shulker.class, EntityDataSerializers.BYTE);
   private static final int TELEPORT_STEPS = 6;
   private static final byte NO_COLOR = 16;
   private static final byte DEFAULT_COLOR = 16;
   private static final int MAX_TELEPORT_DISTANCE = 8;
   private static final int OTHER_SHULKER_SCAN_RADIUS = 8;
   private static final int OTHER_SHULKER_LIMIT = 5;
   private static final float PEEK_PER_TICK = 0.05F;
   static final Vector3f FORWARD = Util.make(() -> {
      Vec3i vec3i = Direction.SOUTH.getNormal();
      return new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
   });
   private float currentPeekAmountO;
   private float currentPeekAmount;
   @Nullable
   private BlockPos clientOldAttachPosition;
   private int clientSideTeleportInterpolation;
   private static final float MAX_LID_OPEN = 1.0F;

   public Shulker(EntityType<? extends Shulker> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.xpReward = 5;
      this.lookControl = new Shulker.ShulkerLookControl(this);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F, 0.02F, true));
      this.goalSelector.addGoal(4, new Shulker.ShulkerAttackGoal());
      this.goalSelector.addGoal(7, new Shulker.ShulkerPeekGoal());
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, this.getClass())).setAlertOthers());
      this.targetSelector.addGoal(2, new Shulker.ShulkerNearestAttackGoal(this));
      this.targetSelector.addGoal(3, new Shulker.ShulkerDefenseAttackGoal(this));
   }

   protected Entity.MovementEmission getMovementEmission() {
      return Entity.MovementEmission.NONE;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SHULKER_AMBIENT;
   }

   /**
    * Plays living's sound at its position
    */
   public void playAmbientSound() {
      if (!this.isClosed()) {
         super.playAmbientSound();
      }

   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SHULKER_DEATH;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return this.isClosed() ? SoundEvents.SHULKER_HURT_CLOSED : SoundEvents.SHULKER_HURT;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ATTACH_FACE_ID, Direction.DOWN);
      this.entityData.define(DATA_PEEK_ID, (byte)0);
      this.entityData.define(DATA_COLOR_ID, (byte)16);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D);
   }

   protected BodyRotationControl createBodyControl() {
      return new Shulker.ShulkerBodyRotationControl(this);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setAttachFace(Direction.from3DDataValue(pCompound.getByte("AttachFace")));
      this.entityData.set(DATA_PEEK_ID, pCompound.getByte("Peek"));
      if (pCompound.contains("Color", 99)) {
         this.entityData.set(DATA_COLOR_ID, pCompound.getByte("Color"));
      }

   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putByte("AttachFace", (byte)this.getAttachFace().get3DDataValue());
      pCompound.putByte("Peek", this.entityData.get(DATA_PEEK_ID));
      pCompound.putByte("Color", this.entityData.get(DATA_COLOR_ID));
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (!this.level().isClientSide && !this.isPassenger() && !this.canStayAt(this.blockPosition(), this.getAttachFace())) {
         this.findNewAttachment();
      }

      if (this.updatePeekAmount()) {
         this.onPeekAmountChange();
      }

      if (this.level().isClientSide) {
         if (this.clientSideTeleportInterpolation > 0) {
            --this.clientSideTeleportInterpolation;
         } else {
            this.clientOldAttachPosition = null;
         }
      }

   }

   private void findNewAttachment() {
      Direction direction = this.findAttachableSurface(this.blockPosition());
      if (direction != null) {
         this.setAttachFace(direction);
      } else {
         this.teleportSomewhere();
      }

   }

   protected AABB makeBoundingBox() {
      float f = getPhysicalPeek(this.currentPeekAmount);
      Direction direction = this.getAttachFace().getOpposite();
      float f1 = this.getType().getWidth() / 2.0F;
      return getProgressAabb(direction, f).move(this.getX() - (double)f1, this.getY(), this.getZ() - (double)f1);
   }

   private static float getPhysicalPeek(float pPeek) {
      return 0.5F - Mth.sin((0.5F + pPeek) * (float)Math.PI) * 0.5F;
   }

   private boolean updatePeekAmount() {
      this.currentPeekAmountO = this.currentPeekAmount;
      float f = (float)this.getRawPeekAmount() * 0.01F;
      if (this.currentPeekAmount == f) {
         return false;
      } else {
         if (this.currentPeekAmount > f) {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount - 0.05F, f, 1.0F);
         } else {
            this.currentPeekAmount = Mth.clamp(this.currentPeekAmount + 0.05F, 0.0F, f);
         }

         return true;
      }
   }

   private void onPeekAmountChange() {
      this.reapplyPosition();
      float f = getPhysicalPeek(this.currentPeekAmount);
      float f1 = getPhysicalPeek(this.currentPeekAmountO);
      Direction direction = this.getAttachFace().getOpposite();
      float f2 = f - f1;
      if (!(f2 <= 0.0F)) {
         for(Entity entity : this.level().getEntities(this, getProgressDeltaAabb(direction, f1, f).move(this.getX() - 0.5D, this.getY(), this.getZ() - 0.5D), EntitySelector.NO_SPECTATORS.and((p_149771_) -> {
            return !p_149771_.isPassengerOfSameVehicle(this);
         }))) {
            if (!(entity instanceof Shulker) && !entity.noPhysics) {
               entity.move(MoverType.SHULKER, new Vec3((double)(f2 * (float)direction.getStepX()), (double)(f2 * (float)direction.getStepY()), (double)(f2 * (float)direction.getStepZ())));
            }
         }

      }
   }

   public static AABB getProgressAabb(Direction pDirection, float pDelta) {
      return getProgressDeltaAabb(pDirection, -1.0F, pDelta);
   }

   public static AABB getProgressDeltaAabb(Direction pDirection, float pDelta, float pDeltaO) {
      double d0 = (double)Math.max(pDelta, pDeltaO);
      double d1 = (double)Math.min(pDelta, pDeltaO);
      return (new AABB(BlockPos.ZERO)).expandTowards((double)pDirection.getStepX() * d0, (double)pDirection.getStepY() * d0, (double)pDirection.getStepZ() * d0).contract((double)(-pDirection.getStepX()) * (1.0D + d1), (double)(-pDirection.getStepY()) * (1.0D + d1), (double)(-pDirection.getStepZ()) * (1.0D + d1));
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      EntityType<?> entitytype = this.getVehicle().getType();
      return !(this.getVehicle() instanceof Boat) && entitytype != EntityType.MINECART ? super.getMyRidingOffset() : 0.1875D - this.getVehicle().getPassengersRidingOffset();
   }

   public boolean startRiding(Entity pEntity, boolean pForce) {
      if (this.level().isClientSide()) {
         this.clientOldAttachPosition = null;
         this.clientSideTeleportInterpolation = 0;
      }

      this.setAttachFace(Direction.DOWN);
      return super.startRiding(pEntity, pForce);
   }

   /**
    * Dismounts this entity from the entity it is riding.
    */
   public void stopRiding() {
      super.stopRiding();
      if (this.level().isClientSide) {
         this.clientOldAttachPosition = this.blockPosition();
      }

      this.yBodyRotO = 0.0F;
      this.yBodyRot = 0.0F;
   }

   @Nullable
   public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
      this.setYRot(0.0F);
      this.yHeadRot = this.getYRot();
      this.setOldPosAndRot();
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public void move(MoverType pType, Vec3 pPos) {
      if (pType == MoverType.SHULKER_BOX) {
         this.teleportSomewhere();
      } else {
         super.move(pType, pPos);
      }

   }

   public Vec3 getDeltaMovement() {
      return Vec3.ZERO;
   }

   public void setDeltaMovement(Vec3 pDeltaMovement) {
   }

   /**
    * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
    */
   public void setPos(double pX, double pY, double pZ) {
      BlockPos blockpos = this.blockPosition();
      if (this.isPassenger()) {
         super.setPos(pX, pY, pZ);
      } else {
         super.setPos((double)Mth.floor(pX) + 0.5D, (double)Mth.floor(pY + 0.5D), (double)Mth.floor(pZ) + 0.5D);
      }

      if (this.tickCount != 0) {
         BlockPos blockpos1 = this.blockPosition();
         if (!blockpos1.equals(blockpos)) {
            this.entityData.set(DATA_PEEK_ID, (byte)0);
            this.hasImpulse = true;
            if (this.level().isClientSide && !this.isPassenger() && !blockpos1.equals(this.clientOldAttachPosition)) {
               this.clientOldAttachPosition = blockpos;
               this.clientSideTeleportInterpolation = 6;
               this.xOld = this.getX();
               this.yOld = this.getY();
               this.zOld = this.getZ();
            }
         }

      }
   }

   @Nullable
   protected Direction findAttachableSurface(BlockPos pPos) {
      for(Direction direction : Direction.values()) {
         if (this.canStayAt(pPos, direction)) {
            return direction;
         }
      }

      return null;
   }

   boolean canStayAt(BlockPos pPos, Direction pFacing) {
      if (this.isPositionBlocked(pPos)) {
         return false;
      } else {
         Direction direction = pFacing.getOpposite();
         if (!this.level().loadedAndEntityCanStandOnFace(pPos.relative(pFacing), this, direction)) {
            return false;
         } else {
            AABB aabb = getProgressAabb(direction, 1.0F).move(pPos).deflate(1.0E-6D);
            return this.level().noCollision(this, aabb);
         }
      }
   }

   private boolean isPositionBlocked(BlockPos pPos) {
      BlockState blockstate = this.level().getBlockState(pPos);
      if (blockstate.isAir()) {
         return false;
      } else {
         boolean flag = blockstate.is(Blocks.MOVING_PISTON) && pPos.equals(this.blockPosition());
         return !flag;
      }
   }

   protected boolean teleportSomewhere() {
      if (!this.isNoAi() && this.isAlive()) {
         BlockPos blockpos = this.blockPosition();

         for(int i = 0; i < 5; ++i) {
            BlockPos blockpos1 = blockpos.offset(Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8), Mth.randomBetweenInclusive(this.random, -8, 8));
            if (blockpos1.getY() > this.level().getMinBuildHeight() && this.level().isEmptyBlock(blockpos1) && this.level().getWorldBorder().isWithinBounds(blockpos1) && this.level().noCollision(this, (new AABB(blockpos1)).deflate(1.0E-6D))) {
               Direction direction = this.findAttachableSurface(blockpos1);
               if (direction != null) {
                  net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
                  if (event.isCanceled()) direction = null;
                  blockpos1 = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
               }

               if (direction != null) {
                  this.unRide();
                  this.setAttachFace(direction);
                  this.playSound(SoundEvents.SHULKER_TELEPORT, 1.0F, 1.0F);
                  this.setPos((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY(), (double)blockpos1.getZ() + 0.5D);
                  this.level().gameEvent(GameEvent.TELEPORT, blockpos, GameEvent.Context.of(this));
                  this.entityData.set(DATA_PEEK_ID, (byte)0);
                  this.setTarget((LivingEntity)null);
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
      this.lerpSteps = 0;
      this.setPos(pX, pY, pZ);
      this.setRot(pYaw, pPitch);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isClosed()) {
         Entity entity = pSource.getDirectEntity();
         if (entity instanceof AbstractArrow) {
            return false;
         }
      }

      if (!super.hurt(pSource, pAmount)) {
         return false;
      } else {
         if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5D && this.random.nextInt(4) == 0) {
            this.teleportSomewhere();
         } else if (pSource.is(DamageTypeTags.IS_PROJECTILE)) {
            Entity entity1 = pSource.getDirectEntity();
            if (entity1 != null && entity1.getType() == EntityType.SHULKER_BULLET) {
               this.hitByShulkerBullet();
            }
         }

         return true;
      }
   }

   private boolean isClosed() {
      return this.getRawPeekAmount() == 0;
   }

   private void hitByShulkerBullet() {
      Vec3 vec3 = this.position();
      AABB aabb = this.getBoundingBox();
      if (!this.isClosed() && this.teleportSomewhere()) {
         int i = this.level().getEntities(EntityType.SHULKER, aabb.inflate(8.0D), Entity::isAlive).size();
         float f = (float)(i - 1) / 5.0F;
         if (!(this.level().random.nextFloat() < f)) {
            Shulker shulker = EntityType.SHULKER.create(this.level());
            if (shulker != null) {
               shulker.setVariant(this.getVariant());
               shulker.moveTo(vec3);
               this.level().addFreshEntity(shulker);
            }

         }
      }
   }

   public boolean canBeCollidedWith() {
      return this.isAlive();
   }

   public Direction getAttachFace() {
      return this.entityData.get(DATA_ATTACH_FACE_ID);
   }

   private void setAttachFace(Direction pAttachFace) {
      this.entityData.set(DATA_ATTACH_FACE_ID, pAttachFace);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (DATA_ATTACH_FACE_ID.equals(pKey)) {
         this.setBoundingBox(this.makeBoundingBox());
      }

      super.onSyncedDataUpdated(pKey);
   }

   private int getRawPeekAmount() {
      return this.entityData.get(DATA_PEEK_ID);
   }

   /**
    * Applies or removes armor modifier
    */
   void setRawPeekAmount(int pPeekAmount) {
      if (!this.level().isClientSide) {
         this.getAttribute(Attributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER);
         if (pPeekAmount == 0) {
            this.getAttribute(Attributes.ARMOR).addPermanentModifier(COVERED_ARMOR_MODIFIER);
            this.playSound(SoundEvents.SHULKER_CLOSE, 1.0F, 1.0F);
            this.gameEvent(GameEvent.CONTAINER_CLOSE);
         } else {
            this.playSound(SoundEvents.SHULKER_OPEN, 1.0F, 1.0F);
            this.gameEvent(GameEvent.CONTAINER_OPEN);
         }
      }

      this.entityData.set(DATA_PEEK_ID, (byte)pPeekAmount);
   }

   public float getClientPeekAmount(float pPartialTick) {
      return Mth.lerp(pPartialTick, this.currentPeekAmountO, this.currentPeekAmount);
   }

   protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
      return 0.5F;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
      super.recreateFromPacket(pPacket);
      this.yBodyRot = 0.0F;
      this.yBodyRotO = 0.0F;
   }

   /**
    * The speed it takes to move the entity's head rotation through the faceEntity method.
    */
   public int getMaxHeadXRot() {
      return 180;
   }

   public int getMaxHeadYRot() {
      return 180;
   }

   /**
    * Applies a velocity to the entities, to push them away from each other.
    */
   public void push(Entity pEntity) {
   }

   public float getPickRadius() {
      return 0.0F;
   }

   public Optional<Vec3> getRenderPosition(float pPartial) {
      if (this.clientOldAttachPosition != null && this.clientSideTeleportInterpolation > 0) {
         double d0 = (double)((float)this.clientSideTeleportInterpolation - pPartial) / 6.0D;
         d0 *= d0;
         BlockPos blockpos = this.blockPosition();
         double d1 = (double)(blockpos.getX() - this.clientOldAttachPosition.getX()) * d0;
         double d2 = (double)(blockpos.getY() - this.clientOldAttachPosition.getY()) * d0;
         double d3 = (double)(blockpos.getZ() - this.clientOldAttachPosition.getZ()) * d0;
         return Optional.of(new Vec3(-d1, -d2, -d3));
      } else {
         return Optional.empty();
      }
   }

   public void setVariant(Optional<DyeColor> pVariant) {
      this.entityData.set(DATA_COLOR_ID, pVariant.map((p_262566_) -> {
         return (byte)p_262566_.getId();
      }).orElse((byte)16));
   }

   public Optional<DyeColor> getVariant() {
      return Optional.ofNullable(this.getColor());
   }

   @Nullable
   public DyeColor getColor() {
      byte b0 = this.entityData.get(DATA_COLOR_ID);
      return b0 != 16 && b0 <= 15 ? DyeColor.byId(b0) : null;
   }

   class ShulkerAttackGoal extends Goal {
      private int attackTime;

      public ShulkerAttackGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         LivingEntity livingentity = Shulker.this.getTarget();
         if (livingentity != null && livingentity.isAlive()) {
            return Shulker.this.level().getDifficulty() != Difficulty.PEACEFUL;
         } else {
            return false;
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.attackTime = 20;
         Shulker.this.setRawPeekAmount(100);
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         Shulker.this.setRawPeekAmount(0);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (Shulker.this.level().getDifficulty() != Difficulty.PEACEFUL) {
            --this.attackTime;
            LivingEntity livingentity = Shulker.this.getTarget();
            if (livingentity != null) {
               Shulker.this.getLookControl().setLookAt(livingentity, 180.0F, 180.0F);
               double d0 = Shulker.this.distanceToSqr(livingentity);
               if (d0 < 400.0D) {
                  if (this.attackTime <= 0) {
                     this.attackTime = 20 + Shulker.this.random.nextInt(10) * 20 / 2;
                     Shulker.this.level().addFreshEntity(new ShulkerBullet(Shulker.this.level(), Shulker.this, livingentity, Shulker.this.getAttachFace().getAxis()));
                     Shulker.this.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (Shulker.this.random.nextFloat() - Shulker.this.random.nextFloat()) * 0.2F + 1.0F);
                  }
               } else {
                  Shulker.this.setTarget((LivingEntity)null);
               }

               super.tick();
            }
         }
      }
   }

   static class ShulkerBodyRotationControl extends BodyRotationControl {
      public ShulkerBodyRotationControl(Mob pMob) {
         super(pMob);
      }

      /**
       * Update the Head and Body rendering angles
       */
      public void clientTick() {
      }
   }

   static class ShulkerDefenseAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
      public ShulkerDefenseAttackGoal(Shulker pShulker) {
         super(pShulker, LivingEntity.class, 10, true, false, (p_33501_) -> {
            return p_33501_ instanceof Enemy;
         });
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.mob.getTeam() == null ? false : super.canUse();
      }

      protected AABB getTargetSearchArea(double pTargetDistance) {
         Direction direction = ((Shulker)this.mob).getAttachFace();
         if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().inflate(4.0D, pTargetDistance, pTargetDistance);
         } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(pTargetDistance, pTargetDistance, 4.0D) : this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
         }
      }
   }

   class ShulkerLookControl extends LookControl {
      public ShulkerLookControl(Mob pMob) {
         super(pMob);
      }

      protected void clampHeadRotationToBody() {
      }

      protected Optional<Float> getYRotD() {
         Direction direction = Shulker.this.getAttachFace().getOpposite();
         Vector3f vector3f = direction.getRotation().transform(new Vector3f((Vector3fc)Shulker.FORWARD));
         Vec3i vec3i = direction.getNormal();
         Vector3f vector3f1 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
         vector3f1.cross(vector3f);
         double d0 = this.wantedX - this.mob.getX();
         double d1 = this.wantedY - this.mob.getEyeY();
         double d2 = this.wantedZ - this.mob.getZ();
         Vector3f vector3f2 = new Vector3f((float)d0, (float)d1, (float)d2);
         float f = vector3f1.dot(vector3f2);
         float f1 = vector3f.dot(vector3f2);
         return !(Math.abs(f) > 1.0E-5F) && !(Math.abs(f1) > 1.0E-5F) ? Optional.empty() : Optional.of((float)(Mth.atan2((double)(-f), (double)f1) * (double)(180F / (float)Math.PI)));
      }

      protected Optional<Float> getXRotD() {
         return Optional.of(0.0F);
      }
   }

   class ShulkerNearestAttackGoal extends NearestAttackableTargetGoal<Player> {
      public ShulkerNearestAttackGoal(Shulker pShulker) {
         super(pShulker, Player.class, true);
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return Shulker.this.level().getDifficulty() == Difficulty.PEACEFUL ? false : super.canUse();
      }

      protected AABB getTargetSearchArea(double pTargetDistance) {
         Direction direction = ((Shulker)this.mob).getAttachFace();
         if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().inflate(4.0D, pTargetDistance, pTargetDistance);
         } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(pTargetDistance, pTargetDistance, 4.0D) : this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
         }
      }
   }

   class ShulkerPeekGoal extends Goal {
      private int peekTime;

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return Shulker.this.getTarget() == null && Shulker.this.random.nextInt(reducedTickDelay(40)) == 0 && Shulker.this.canStayAt(Shulker.this.blockPosition(), Shulker.this.getAttachFace());
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return Shulker.this.getTarget() == null && this.peekTime > 0;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.peekTime = this.adjustedTickDelay(20 * (1 + Shulker.this.random.nextInt(3)));
         Shulker.this.setRawPeekAmount(30);
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         if (Shulker.this.getTarget() == null) {
            Shulker.this.setRawPeekAmount(0);
         }

      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         --this.peekTime;
      }
   }
}
