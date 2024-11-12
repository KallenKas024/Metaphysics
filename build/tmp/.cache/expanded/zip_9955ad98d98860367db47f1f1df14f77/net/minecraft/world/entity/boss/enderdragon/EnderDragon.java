package net.minecraft.world.entity.boss.enderdragon;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class EnderDragon extends Mob implements Enemy {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(EnderDragon.class, EntityDataSerializers.INT);
   private static final TargetingConditions CRYSTAL_DESTROY_TARGETING = TargetingConditions.forCombat().range(64.0D);
   private static final int GROWL_INTERVAL_MIN = 200;
   private static final int GROWL_INTERVAL_MAX = 400;
   private static final float SITTING_ALLOWED_DAMAGE_PERCENTAGE = 0.25F;
   private static final String DRAGON_DEATH_TIME_KEY = "DragonDeathTime";
   private static final String DRAGON_PHASE_KEY = "DragonPhase";
   public final double[][] positions = new double[64][3];
   public int posPointer = -1;
   private final EnderDragonPart[] subEntities;
   public final EnderDragonPart head;
   private final EnderDragonPart neck;
   private final EnderDragonPart body;
   private final EnderDragonPart tail1;
   private final EnderDragonPart tail2;
   private final EnderDragonPart tail3;
   private final EnderDragonPart wing1;
   private final EnderDragonPart wing2;
   public float oFlapTime;
   public float flapTime;
   public boolean inWall;
   public int dragonDeathTime;
   public float yRotA;
   @Nullable
   public EndCrystal nearestCrystal;
   @Nullable
   private EndDragonFight dragonFight;
   private BlockPos fightOrigin = BlockPos.ZERO;
   private final EnderDragonPhaseManager phaseManager;
   private int growlTime = 100;
   private float sittingDamageReceived;
   private final Node[] nodes = new Node[24];
   private final int[] nodeAdjacency = new int[24];
   private final BinaryHeap openSet = new BinaryHeap();

   public EnderDragon(EntityType<? extends EnderDragon> pEntityType, Level pLevel) {
      super(EntityType.ENDER_DRAGON, pLevel);
      this.head = new EnderDragonPart(this, "head", 1.0F, 1.0F);
      this.neck = new EnderDragonPart(this, "neck", 3.0F, 3.0F);
      this.body = new EnderDragonPart(this, "body", 5.0F, 3.0F);
      this.tail1 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.tail2 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.tail3 = new EnderDragonPart(this, "tail", 2.0F, 2.0F);
      this.wing1 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
      this.wing2 = new EnderDragonPart(this, "wing", 4.0F, 2.0F);
      this.subEntities = new EnderDragonPart[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
      this.setHealth(this.getMaxHealth());
      this.noPhysics = true;
      this.noCulling = true;
      this.phaseManager = new EnderDragonPhaseManager(this);
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
   }

   @Override
   public void setId(int pId) {
      super.setId(pId);
      for (int i = 0; i < this.subEntities.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
         this.subEntities[i].setId(pId + i + 1);
   }

   public void setDragonFight(EndDragonFight pDragonFight) {
      this.dragonFight = pDragonFight;
   }

   public void setFightOrigin(BlockPos pFightOrigin) {
      this.fightOrigin = pFightOrigin;
   }

   public BlockPos getFightOrigin() {
      return this.fightOrigin;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0D);
   }

   public boolean isFlapping() {
      float f = Mth.cos(this.flapTime * ((float)Math.PI * 2F));
      float f1 = Mth.cos(this.oFlapTime * ((float)Math.PI * 2F));
      return f1 <= -0.3F && f >= -0.3F;
   }

   public void onFlap() {
      if (this.level().isClientSide && !this.isSilent()) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_PHASE, EnderDragonPhase.HOVERING.getId());
   }

   /**
    * Returns a double[3] array with movement offsets, used to calculate trailing tail/neck positions. [0] = yaw offset,
    * [1] = y offset, [2] = unused, always 0. Parameters: buffer index offset, partial ticks.
    */
   public double[] getLatencyPos(int pBufferIndexOffset, float pPartialTicks) {
      if (this.isDeadOrDying()) {
         pPartialTicks = 0.0F;
      }

      pPartialTicks = 1.0F - pPartialTicks;
      int i = this.posPointer - pBufferIndexOffset & 63;
      int j = this.posPointer - pBufferIndexOffset - 1 & 63;
      double[] adouble = new double[3];
      double d0 = this.positions[i][0];
      double d1 = Mth.wrapDegrees(this.positions[j][0] - d0);
      adouble[0] = d0 + d1 * (double)pPartialTicks;
      d0 = this.positions[i][1];
      d1 = this.positions[j][1] - d0;
      adouble[1] = d0 + d1 * (double)pPartialTicks;
      adouble[2] = Mth.lerp((double)pPartialTicks, this.positions[i][2], this.positions[j][2]);
      return adouble;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   @org.jetbrains.annotations.Nullable private Player unlimitedLastHurtByPlayer = null;
   public void aiStep() {
      // lastHurtByPlayer is cleared after 100 ticks, capture it indefinitely in unlimitedLastHurtByPlayer for LivingExperienceDropEvent
      if (this.lastHurtByPlayer != null) this.unlimitedLastHurtByPlayer = lastHurtByPlayer;
      if (this.unlimitedLastHurtByPlayer != null && this.unlimitedLastHurtByPlayer.isRemoved()) this.unlimitedLastHurtByPlayer = null;
      this.processFlappingMovement();
      if (this.level().isClientSide) {
         this.setHealth(this.getHealth());
         if (!this.isSilent() && !this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
            this.growlTime = 200 + this.random.nextInt(200);
         }
      }

      if (this.dragonFight == null) {
         Level $$6 = this.level();
         if ($$6 instanceof ServerLevel) {
            ServerLevel serverlevel = (ServerLevel)$$6;
            EndDragonFight enddragonfight = serverlevel.getDragonFight();
            if (enddragonfight != null && this.getUUID().equals(enddragonfight.getDragonUUID())) {
               this.dragonFight = enddragonfight;
            }
         }
      }

      this.oFlapTime = this.flapTime;
      if (this.isDeadOrDying()) {
         float f8 = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f10 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f11 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f8, this.getY() + 2.0D + (double)f10, this.getZ() + (double)f11, 0.0D, 0.0D, 0.0D);
      } else {
         this.checkCrystals();
         Vec3 vec34 = this.getDeltaMovement();
         float f9 = 0.2F / ((float)vec34.horizontalDistance() * 10.0F + 1.0F);
         f9 *= (float)Math.pow(2.0D, vec34.y);
         if (this.phaseManager.getCurrentPhase().isSitting()) {
            this.flapTime += 0.1F;
         } else if (this.inWall) {
            this.flapTime += f9 * 0.5F;
         } else {
            this.flapTime += f9;
         }

         this.setYRot(Mth.wrapDegrees(this.getYRot()));
         if (this.isNoAi()) {
            this.flapTime = 0.5F;
         } else {
            if (this.posPointer < 0) {
               for(int i = 0; i < this.positions.length; ++i) {
                  this.positions[i][0] = (double)this.getYRot();
                  this.positions[i][1] = this.getY();
               }
            }

            if (++this.posPointer == this.positions.length) {
               this.posPointer = 0;
            }

            this.positions[this.posPointer][0] = (double)this.getYRot();
            this.positions[this.posPointer][1] = this.getY();
            if (this.level().isClientSide) {
               if (this.lerpSteps > 0) {
                  double d6 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
                  double d0 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
                  double d1 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
                  double d2 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
                  this.setYRot(this.getYRot() + (float)d2 / (float)this.lerpSteps);
                  this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
                  --this.lerpSteps;
                  this.setPos(d6, d0, d1);
                  this.setRot(this.getYRot(), this.getXRot());
               }

               this.phaseManager.getCurrentPhase().doClientTick();
            } else {
               DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
               dragonphaseinstance.doServerTick();
               if (this.phaseManager.getCurrentPhase() != dragonphaseinstance) {
                  dragonphaseinstance = this.phaseManager.getCurrentPhase();
                  dragonphaseinstance.doServerTick();
               }

               Vec3 vec3 = dragonphaseinstance.getFlyTargetLocation();
               if (vec3 != null) {
                  double d7 = vec3.x - this.getX();
                  double d8 = vec3.y - this.getY();
                  double d9 = vec3.z - this.getZ();
                  double d3 = d7 * d7 + d8 * d8 + d9 * d9;
                  float f4 = dragonphaseinstance.getFlySpeed();
                  double d4 = Math.sqrt(d7 * d7 + d9 * d9);
                  if (d4 > 0.0D) {
                     d8 = Mth.clamp(d8 / d4, (double)(-f4), (double)f4);
                  }

                  this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d8 * 0.01D, 0.0D));
                  this.setYRot(Mth.wrapDegrees(this.getYRot()));
                  Vec3 vec31 = vec3.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                  Vec3 vec32 = (new Vec3((double)Mth.sin(this.getYRot() * ((float)Math.PI / 180F)), this.getDeltaMovement().y, (double)(-Mth.cos(this.getYRot() * ((float)Math.PI / 180F))))).normalize();
                  float f5 = Math.max(((float)vec32.dot(vec31) + 0.5F) / 1.5F, 0.0F);
                  if (Math.abs(d7) > (double)1.0E-5F || Math.abs(d9) > (double)1.0E-5F) {
                     float f6 = Mth.clamp(Mth.wrapDegrees(180.0F - (float)Mth.atan2(d7, d9) * (180F / (float)Math.PI) - this.getYRot()), -50.0F, 50.0F);
                     this.yRotA *= 0.8F;
                     this.yRotA += f6 * dragonphaseinstance.getTurnSpeed();
                     this.setYRot(this.getYRot() + this.yRotA * 0.1F);
                  }

                  float f19 = (float)(2.0D / (d3 + 1.0D));
                  float f7 = 0.06F;
                  this.moveRelative(0.06F * (f5 * f19 + (1.0F - f19)), new Vec3(0.0D, 0.0D, -1.0D));
                  if (this.inWall) {
                     this.move(MoverType.SELF, this.getDeltaMovement().scale((double)0.8F));
                  } else {
                     this.move(MoverType.SELF, this.getDeltaMovement());
                  }

                  Vec3 vec33 = this.getDeltaMovement().normalize();
                  double d5 = 0.8D + 0.15D * (vec33.dot(vec32) + 1.0D) / 2.0D;
                  this.setDeltaMovement(this.getDeltaMovement().multiply(d5, (double)0.91F, d5));
               }
            }

            this.yBodyRot = this.getYRot();
            Vec3[] avec3 = new Vec3[this.subEntities.length];

            for(int j = 0; j < this.subEntities.length; ++j) {
               avec3[j] = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
            }

            float f12 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
            float f13 = Mth.cos(f12);
            float f = Mth.sin(f12);
            float f14 = this.getYRot() * ((float)Math.PI / 180F);
            float f1 = Mth.sin(f14);
            float f15 = Mth.cos(f14);
            this.tickPart(this.body, (double)(f1 * 0.5F), 0.0D, (double)(-f15 * 0.5F));
            this.tickPart(this.wing1, (double)(f15 * 4.5F), 2.0D, (double)(f1 * 4.5F));
            this.tickPart(this.wing2, (double)(f15 * -4.5F), 2.0D, (double)(f1 * -4.5F));
            if (!this.level().isClientSide && this.hurtTime == 0) {
               this.knockBack(this.level().getEntities(this, this.wing1.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.knockBack(this.level().getEntities(this, this.wing2.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level().getEntities(this, this.head.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level().getEntities(this, this.neck.getBoundingBox().inflate(1.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
            }

            float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f16 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f3 = this.getHeadYOffset();
            this.tickPart(this.head, (double)(f2 * 6.5F * f13), (double)(f3 + f * 6.5F), (double)(-f16 * 6.5F * f13));
            this.tickPart(this.neck, (double)(f2 * 5.5F * f13), (double)(f3 + f * 5.5F), (double)(-f16 * 5.5F * f13));
            double[] adouble = this.getLatencyPos(5, 1.0F);

            for(int k = 0; k < 3; ++k) {
               EnderDragonPart enderdragonpart = null;
               if (k == 0) {
                  enderdragonpart = this.tail1;
               }

               if (k == 1) {
                  enderdragonpart = this.tail2;
               }

               if (k == 2) {
                  enderdragonpart = this.tail3;
               }

               double[] adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
               float f17 = this.getYRot() * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
               float f18 = Mth.sin(f17);
               float f20 = Mth.cos(f17);
               float f21 = 1.5F;
               float f22 = (float)(k + 1) * 2.0F;
               this.tickPart(enderdragonpart, (double)(-(f1 * 1.5F + f18 * f22) * f13), adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f) + 1.5D, (double)((f15 * 1.5F + f20 * f22) * f13));
            }

            if (!this.level().isClientSide) {
               this.inWall = this.checkWalls(this.head.getBoundingBox()) | this.checkWalls(this.neck.getBoundingBox()) | this.checkWalls(this.body.getBoundingBox());
               if (this.dragonFight != null) {
                  this.dragonFight.updateDragon(this);
               }
            }

            for(int l = 0; l < this.subEntities.length; ++l) {
               this.subEntities[l].xo = avec3[l].x;
               this.subEntities[l].yo = avec3[l].y;
               this.subEntities[l].zo = avec3[l].z;
               this.subEntities[l].xOld = avec3[l].x;
               this.subEntities[l].yOld = avec3[l].y;
               this.subEntities[l].zOld = avec3[l].z;
            }

         }
      }
   }

   private void tickPart(EnderDragonPart pPart, double pOffsetX, double pOffsetY, double pOffsetZ) {
      pPart.setPos(this.getX() + pOffsetX, this.getY() + pOffsetY, this.getZ() + pOffsetZ);
   }

   private float getHeadYOffset() {
      if (this.phaseManager.getCurrentPhase().isSitting()) {
         return -1.0F;
      } else {
         double[] adouble = this.getLatencyPos(5, 1.0F);
         double[] adouble1 = this.getLatencyPos(0, 1.0F);
         return (float)(adouble[1] - adouble1[1]);
      }
   }

   /**
    * Updates the state of the dragon's current endercrystal.
    */
   private void checkCrystals() {
      if (this.nearestCrystal != null) {
         if (this.nearestCrystal.isRemoved()) {
            this.nearestCrystal = null;
         } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
         }
      }

      if (this.random.nextInt(10) == 0) {
         List<EndCrystal> list = this.level().getEntitiesOfClass(EndCrystal.class, this.getBoundingBox().inflate(32.0D));
         EndCrystal endcrystal = null;
         double d0 = Double.MAX_VALUE;

         for(EndCrystal endcrystal1 : list) {
            double d1 = endcrystal1.distanceToSqr(this);
            if (d1 < d0) {
               d0 = d1;
               endcrystal = endcrystal1;
            }
         }

         this.nearestCrystal = endcrystal;
      }

   }

   /**
    * Pushes all entities inside the list away from the enderdragon.
    */
   private void knockBack(List<Entity> pEntities) {
      double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
      double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;

      for(Entity entity : pEntities) {
         if (entity instanceof LivingEntity) {
            double d2 = entity.getX() - d0;
            double d3 = entity.getZ() - d1;
            double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
            entity.push(d2 / d4 * 4.0D, (double)0.2F, d3 / d4 * 4.0D);
            if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 2) {
               entity.hurt(this.damageSources().mobAttack(this), 5.0F);
               this.doEnchantDamageEffects(this, entity);
            }
         }
      }

   }

   /**
    * Attacks all entities inside this list, dealing 5 hearts of damage.
    */
   private void hurt(List<Entity> pEntities) {
      for(Entity entity : pEntities) {
         if (entity instanceof LivingEntity) {
            entity.hurt(this.damageSources().mobAttack(this), 10.0F);
            this.doEnchantDamageEffects(this, entity);
         }
      }

   }

   /**
    * Simplifies the value of a number by adding/subtracting 180 to the point that the number is between -180 and 180.
    */
   private float rotWrap(double pAngle) {
      return (float)Mth.wrapDegrees(pAngle);
   }

   /**
    * Destroys all blocks that aren't associated with 'The End' inside the given bounding box.
    */
   private boolean checkWalls(AABB pArea) {
      int i = Mth.floor(pArea.minX);
      int j = Mth.floor(pArea.minY);
      int k = Mth.floor(pArea.minZ);
      int l = Mth.floor(pArea.maxX);
      int i1 = Mth.floor(pArea.maxY);
      int j1 = Mth.floor(pArea.maxZ);
      boolean flag = false;
      boolean flag1 = false;

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = j; l1 <= i1; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               BlockPos blockpos = new BlockPos(k1, l1, i2);
               BlockState blockstate = this.level().getBlockState(blockpos);
               if (!blockstate.isAir() && !blockstate.is(BlockTags.DRAGON_TRANSPARENT)) {
                  if (net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.level(), blockpos, this) && !blockstate.is(BlockTags.DRAGON_IMMUNE)) {
                     flag1 = this.level().removeBlock(blockpos, false) || flag1;
                  } else {
                     flag = true;
                  }
               }
            }
         }
      }

      if (flag1) {
         BlockPos blockpos1 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(i1 - j + 1), k + this.random.nextInt(j1 - k + 1));
         this.level().levelEvent(2008, blockpos1, 0);
      }

      return flag;
   }

   public boolean hurt(EnderDragonPart pPart, DamageSource pSource, float pDamage) {
      if (this.phaseManager.getCurrentPhase().getPhase() == EnderDragonPhase.DYING) {
         return false;
      } else {
         pDamage = this.phaseManager.getCurrentPhase().onHurt(pSource, pDamage);
         if (pPart != this.head) {
            pDamage = pDamage / 4.0F + Math.min(pDamage, 1.0F);
         }

         if (pDamage < 0.01F) {
            return false;
         } else {
            if (pSource.getEntity() instanceof Player || pSource.is(DamageTypeTags.ALWAYS_HURTS_ENDER_DRAGONS)) {
               float f = this.getHealth();
               this.reallyHurt(pSource, pDamage);
               if (this.isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                  this.setHealth(1.0F);
                  this.phaseManager.setPhase(EnderDragonPhase.DYING);
               }

               if (this.phaseManager.getCurrentPhase().isSitting()) {
                  this.sittingDamageReceived = this.sittingDamageReceived + f - this.getHealth();
                  if (this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                     this.sittingDamageReceived = 0.0F;
                     this.phaseManager.setPhase(EnderDragonPhase.TAKEOFF);
                  }
               }
            }

            return true;
         }
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      return !this.level().isClientSide ? this.hurt(this.body, pSource, pAmount) : false;
   }

   /**
    * Provides a way to cause damage to an ender dragon.
    */
   protected boolean reallyHurt(DamageSource pDamageSource, float pAmount) {
      return super.hurt(pDamageSource, pAmount);
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.remove(Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
         this.dragonFight.setDragonKilled(this);
      }

   }

   /**
    * Handles entity death timer, experience orb, and particle creation.
    */
   protected void tickDeath() {
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
      }

      ++this.dragonDeathTime;
      if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
         float f = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0D + (double)f1, this.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
      }

      boolean flag = this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
      int i = 500;
      if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
         i = 12000;
      }

      if (this.level() instanceof ServerLevel) {
         if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && flag) {
            int award = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, Mth.floor((float)i * 0.08F));
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
         }

         if (this.dragonDeathTime == 1 && !this.isSilent()) {
            this.level().globalLevelEvent(1028, this.blockPosition(), 0);
         }
      }

      this.move(MoverType.SELF, new Vec3(0.0D, (double)0.1F, 0.0D));
      if (this.dragonDeathTime == 200 && this.level() instanceof ServerLevel) {
         if (flag) {
            int award = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.unlimitedLastHurtByPlayer, Mth.floor((float)i * 0.2F));
            ExperienceOrb.award((ServerLevel) this.level(), this.position(), award);
         }

         if (this.dragonFight != null) {
            this.dragonFight.setDragonKilled(this);
         }

         this.remove(Entity.RemovalReason.KILLED);
         this.gameEvent(GameEvent.ENTITY_DIE);
      }

   }

   /**
    * Generates values for the fields pathPoints, and neighbors, and then returns the nearest pathPoint to the specified
    * position.
    */
   public int findClosestNode() {
      if (this.nodes[0] == null) {
         for(int i = 0; i < 24; ++i) {
            int j = 5;
            int l;
            int i1;
            if (i < 12) {
               l = Mth.floor(60.0F * Mth.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
               i1 = Mth.floor(60.0F * Mth.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
            } else if (i < 20) {
               int $$2 = i - 12;
               l = Mth.floor(40.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)$$2)));
               i1 = Mth.floor(40.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)$$2)));
               j += 10;
            } else {
               int k1 = i - 20;
               l = Mth.floor(20.0F * Mth.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
               i1 = Mth.floor(20.0F * Mth.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
            }

            int j1 = Math.max(this.level().getSeaLevel() + 10, this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, i1)).getY() + j);
            this.nodes[i] = new Node(l, j1, i1);
         }

         this.nodeAdjacency[0] = 6146;
         this.nodeAdjacency[1] = 8197;
         this.nodeAdjacency[2] = 8202;
         this.nodeAdjacency[3] = 16404;
         this.nodeAdjacency[4] = 32808;
         this.nodeAdjacency[5] = 32848;
         this.nodeAdjacency[6] = 65696;
         this.nodeAdjacency[7] = 131392;
         this.nodeAdjacency[8] = 131712;
         this.nodeAdjacency[9] = 263424;
         this.nodeAdjacency[10] = 526848;
         this.nodeAdjacency[11] = 525313;
         this.nodeAdjacency[12] = 1581057;
         this.nodeAdjacency[13] = 3166214;
         this.nodeAdjacency[14] = 2138120;
         this.nodeAdjacency[15] = 6373424;
         this.nodeAdjacency[16] = 4358208;
         this.nodeAdjacency[17] = 12910976;
         this.nodeAdjacency[18] = 9044480;
         this.nodeAdjacency[19] = 9706496;
         this.nodeAdjacency[20] = 15216640;
         this.nodeAdjacency[21] = 13688832;
         this.nodeAdjacency[22] = 11763712;
         this.nodeAdjacency[23] = 8257536;
      }

      return this.findClosestNode(this.getX(), this.getY(), this.getZ());
   }

   /**
    * Returns the index into pathPoints of the nearest PathPoint.
    */
   public int findClosestNode(double pX, double pY, double pZ) {
      float f = 10000.0F;
      int i = 0;
      Node node = new Node(Mth.floor(pX), Mth.floor(pY), Mth.floor(pZ));
      int j = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         j = 12;
      }

      for(int k = j; k < 24; ++k) {
         if (this.nodes[k] != null) {
            float f1 = this.nodes[k].distanceToSqr(node);
            if (f1 < f) {
               f = f1;
               i = k;
            }
         }
      }

      return i;
   }

   /**
    * Find and return a path among the circles described by pathPoints, or null if the shortest path would just be
    * directly between the start and finish with no intermediate points.
    * 
    * Starting with pathPoint[startIdx], it searches the neighboring points (and their neighboring points, and so on)
    * until it reaches pathPoint[finishIdx], at which point it calls makePath to seal the deal.
    */
   @Nullable
   public Path findPath(int pStartIndex, int pFinishIndex, @Nullable Node pAndThen) {
      for(int i = 0; i < 24; ++i) {
         Node node = this.nodes[i];
         node.closed = false;
         node.f = 0.0F;
         node.g = 0.0F;
         node.h = 0.0F;
         node.cameFrom = null;
         node.heapIdx = -1;
      }

      Node node4 = this.nodes[pStartIndex];
      Node node5 = this.nodes[pFinishIndex];
      node4.g = 0.0F;
      node4.h = node4.distanceTo(node5);
      node4.f = node4.h;
      this.openSet.clear();
      this.openSet.insert(node4);
      Node node1 = node4;
      int j = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         j = 12;
      }

      while(!this.openSet.isEmpty()) {
         Node node2 = this.openSet.pop();
         if (node2.equals(node5)) {
            if (pAndThen != null) {
               pAndThen.cameFrom = node5;
               node5 = pAndThen;
            }

            return this.reconstructPath(node4, node5);
         }

         if (node2.distanceTo(node5) < node1.distanceTo(node5)) {
            node1 = node2;
         }

         node2.closed = true;
         int k = 0;

         for(int l = 0; l < 24; ++l) {
            if (this.nodes[l] == node2) {
               k = l;
               break;
            }
         }

         for(int i1 = j; i1 < 24; ++i1) {
            if ((this.nodeAdjacency[k] & 1 << i1) > 0) {
               Node node3 = this.nodes[i1];
               if (!node3.closed) {
                  float f = node2.g + node2.distanceTo(node3);
                  if (!node3.inOpenSet() || f < node3.g) {
                     node3.cameFrom = node2;
                     node3.g = f;
                     node3.h = node3.distanceTo(node5);
                     if (node3.inOpenSet()) {
                        this.openSet.changeCost(node3, node3.g + node3.h);
                     } else {
                        node3.f = node3.g + node3.h;
                        this.openSet.insert(node3);
                     }
                  }
               }
            }
         }
      }

      if (node1 == node4) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", pStartIndex, pFinishIndex);
         if (pAndThen != null) {
            pAndThen.cameFrom = node1;
            node1 = pAndThen;
         }

         return this.reconstructPath(node4, node1);
      }
   }

   /**
    * Create and return a new PathEntity defining a path from the start to the finish, using the connections already
    * made by the caller, findPath.
    */
   private Path reconstructPath(Node pStart, Node pFinish) {
      List<Node> list = Lists.newArrayList();
      Node node = pFinish;
      list.add(0, pFinish);

      while(node.cameFrom != null) {
         node = node.cameFrom;
         list.add(0, node);
      }

      return new Path(list, new BlockPos(pFinish.x, pFinish.y, pFinish.z), true);
   }

   public void addAdditionalSaveData(CompoundTag pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
      pCompound.putInt("DragonDeathTime", this.dragonDeathTime);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("DragonPhase")) {
         this.phaseManager.setPhase(EnderDragonPhase.getById(pCompound.getInt("DragonPhase")));
      }

      if (pCompound.contains("DragonDeathTime")) {
         this.dragonDeathTime = pCompound.getInt("DragonDeathTime");
      }

   }

   /**
    * Makes the entity despawn if requirements are reached
    */
   public void checkDespawn() {
   }

   public EnderDragonPart[] getSubEntities() {
      return this.subEntities;
   }

   /**
    * Returns {@code true} if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return false;
   }

   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENDER_DRAGON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ENDER_DRAGON_HURT;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 5.0F;
   }

   public float getHeadPartYOffset(int pPartIndex, double[] pSpineEndOffsets, double[] pHeadPartOffsets) {
      DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
      double d0;
      if (enderdragonphase != EnderDragonPhase.LANDING && enderdragonphase != EnderDragonPhase.TAKEOFF) {
         if (dragonphaseinstance.isSitting()) {
            d0 = (double)pPartIndex;
         } else if (pPartIndex == 6) {
            d0 = 0.0D;
         } else {
            d0 = pHeadPartOffsets[1] - pSpineEndOffsets[1];
         }
      } else {
         BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
         double d1 = Math.max(Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0D, 1.0D);
         d0 = (double)pPartIndex / d1;
      }

      return (float)d0;
   }

   public Vec3 getHeadLookVector(float pPartialTicks) {
      DragonPhaseInstance dragonphaseinstance = this.phaseManager.getCurrentPhase();
      EnderDragonPhase<? extends DragonPhaseInstance> enderdragonphase = dragonphaseinstance.getPhase();
      Vec3 vec3;
      if (enderdragonphase != EnderDragonPhase.LANDING && enderdragonphase != EnderDragonPhase.TAKEOFF) {
         if (dragonphaseinstance.isSitting()) {
            float f4 = this.getXRot();
            float f5 = 1.5F;
            this.setXRot(-45.0F);
            vec3 = this.getViewVector(pPartialTicks);
            this.setXRot(f4);
         } else {
            vec3 = this.getViewVector(pPartialTicks);
         }
      } else {
         BlockPos blockpos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.fightOrigin));
         float f = Math.max((float)Math.sqrt(blockpos.distToCenterSqr(this.position())) / 4.0F, 1.0F);
         float f1 = 6.0F / f;
         float f2 = this.getXRot();
         float f3 = 1.5F;
         this.setXRot(-f1 * 1.5F * 5.0F);
         vec3 = this.getViewVector(pPartialTicks);
         this.setXRot(f2);
      }

      return vec3;
   }

   public void onCrystalDestroyed(EndCrystal pCrystal, BlockPos pPos, DamageSource pDamageSource) {
      Player player;
      if (pDamageSource.getEntity() instanceof Player) {
         player = (Player)pDamageSource.getEntity();
      } else {
         player = this.level().getNearestPlayer(CRYSTAL_DESTROY_TARGETING, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());
      }

      if (pCrystal == this.nearestCrystal) {
         this.hurt(this.head, this.damageSources().explosion(pCrystal, player), 10.0F);
      }

      this.phaseManager.getCurrentPhase().onCrystalDestroyed(pCrystal, pPos, pDamageSource, player);
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (DATA_PHASE.equals(pKey) && this.level().isClientSide) {
         this.phaseManager.setPhase(EnderDragonPhase.getById(this.getEntityData().get(DATA_PHASE)));
      }

      super.onSyncedDataUpdated(pKey);
   }

   public EnderDragonPhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   @Nullable
   public EndDragonFight getDragonFight() {
      return this.dragonFight;
   }

   public boolean addEffect(MobEffectInstance pEffectInstance, @Nullable Entity pEntity) {
      return false;
   }

   protected boolean canRide(Entity pEntity) {
      return false;
   }

   /**
    * Returns false if this Entity can't move between dimensions. True if it can.
    */
   public boolean canChangeDimensions() {
      return false;
   }

   @Override
   public boolean isMultipartEntity() {
      return true;
   }

   @Override
   public net.minecraftforge.entity.PartEntity<?>[] getParts() {
      return this.subEntities;
   }

   public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
      super.recreateFromPacket(pPacket);
      if (true) return; // Forge: Fix MC-158205: Moved into setId()
      EnderDragonPart[] aenderdragonpart = this.getSubEntities();

      for(int i = 0; i < aenderdragonpart.length; ++i) {
         aenderdragonpart[i].setId(i + pPacket.getId());
      }

   }

   public boolean canAttack(LivingEntity pTarget) {
      return pTarget.canBeSeenAsEnemy();
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)this.body.getBbHeight();
   }
}
