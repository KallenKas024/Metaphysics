package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud extends Entity implements TraceableEntity {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int TIME_BETWEEN_APPLICATIONS = 5;
   private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
   private static final float MAX_RADIUS = 32.0F;
   private static final float MINIMAL_RADIUS = 0.5F;
   private static final float DEFAULT_RADIUS = 3.0F;
   public static final float DEFAULT_WIDTH = 6.0F;
   public static final float HEIGHT = 0.5F;
   private Potion potion = Potions.EMPTY;
   private final List<MobEffectInstance> effects = Lists.newArrayList();
   private final Map<Entity, Integer> victims = Maps.newHashMap();
   private int duration = 600;
   private int waitTime = 20;
   private int reapplicationDelay = 20;
   private boolean fixedColor;
   private int durationOnUse;
   private float radiusOnUse;
   private float radiusPerTick;
   @Nullable
   private LivingEntity owner;
   @Nullable
   private UUID ownerUUID;

   public AreaEffectCloud(EntityType<? extends AreaEffectCloud> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.noPhysics = true;
   }

   public AreaEffectCloud(Level pLevel, double pX, double pY, double pZ) {
      this(EntityType.AREA_EFFECT_CLOUD, pLevel);
      this.setPos(pX, pY, pZ);
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_COLOR, 0);
      this.getEntityData().define(DATA_RADIUS, 3.0F);
      this.getEntityData().define(DATA_WAITING, false);
      this.getEntityData().define(DATA_PARTICLE, ParticleTypes.ENTITY_EFFECT);
   }

   public void setRadius(float pRadius) {
      if (!this.level().isClientSide) {
         this.getEntityData().set(DATA_RADIUS, Mth.clamp(pRadius, 0.0F, 32.0F));
      }

   }

   public void refreshDimensions() {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      super.refreshDimensions();
      this.setPos(d0, d1, d2);
   }

   public float getRadius() {
      return this.getEntityData().get(DATA_RADIUS);
   }

   public void setPotion(Potion pPotion) {
      this.potion = pPotion;
      if (!this.fixedColor) {
         this.updateColor();
      }

   }

   private void updateColor() {
      if (this.potion == Potions.EMPTY && this.effects.isEmpty()) {
         this.getEntityData().set(DATA_COLOR, 0);
      } else {
         this.getEntityData().set(DATA_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
      }

   }

   public void addEffect(MobEffectInstance pEffectInstance) {
      this.effects.add(pEffectInstance);
      if (!this.fixedColor) {
         this.updateColor();
      }

   }

   public int getColor() {
      return this.getEntityData().get(DATA_COLOR);
   }

   public void setFixedColor(int pColor) {
      this.fixedColor = true;
      this.getEntityData().set(DATA_COLOR, pColor);
   }

   public ParticleOptions getParticle() {
      return this.getEntityData().get(DATA_PARTICLE);
   }

   public void setParticle(ParticleOptions pParticleOption) {
      this.getEntityData().set(DATA_PARTICLE, pParticleOption);
   }

   /**
    * Sets if the cloud is waiting. While waiting, the radius is ignored and the cloud shows fewer particles in its
    * area.
    */
   protected void setWaiting(boolean pWaiting) {
      this.getEntityData().set(DATA_WAITING, pWaiting);
   }

   /**
    * Returns {@code true} if the cloud is waiting. While waiting, the radius is ignored and the cloud shows fewer
    * particles in its area.
    */
   public boolean isWaiting() {
      return this.getEntityData().get(DATA_WAITING);
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int pDuration) {
      this.duration = pDuration;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      boolean flag = this.isWaiting();
      float f = this.getRadius();
      if (this.level().isClientSide) {
         if (flag && this.random.nextBoolean()) {
            return;
         }

         ParticleOptions particleoptions = this.getParticle();
         int i;
         float f1;
         if (flag) {
            i = 2;
            f1 = 0.2F;
         } else {
            i = Mth.ceil((float)Math.PI * f * f);
            f1 = f;
         }

         for(int j = 0; j < i; ++j) {
            float f2 = this.random.nextFloat() * ((float)Math.PI * 2F);
            float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
            double d0 = this.getX() + (double)(Mth.cos(f2) * f3);
            double d2 = this.getY();
            double d4 = this.getZ() + (double)(Mth.sin(f2) * f3);
            double d5;
            double d6;
            double d7;
            if (particleoptions.getType() == ParticleTypes.ENTITY_EFFECT) {
               int k = flag && this.random.nextBoolean() ? 16777215 : this.getColor();
               d5 = (double)((float)(k >> 16 & 255) / 255.0F);
               d6 = (double)((float)(k >> 8 & 255) / 255.0F);
               d7 = (double)((float)(k & 255) / 255.0F);
            } else if (flag) {
               d5 = 0.0D;
               d6 = 0.0D;
               d7 = 0.0D;
            } else {
               d5 = (0.5D - this.random.nextDouble()) * 0.15D;
               d6 = (double)0.01F;
               d7 = (0.5D - this.random.nextDouble()) * 0.15D;
            }

            this.level().addAlwaysVisibleParticle(particleoptions, d0, d2, d4, d5, d6, d7);
         }
      } else {
         if (this.tickCount >= this.waitTime + this.duration) {
            this.discard();
            return;
         }

         boolean flag1 = this.tickCount < this.waitTime;
         if (flag != flag1) {
            this.setWaiting(flag1);
         }

         if (flag1) {
            return;
         }

         if (this.radiusPerTick != 0.0F) {
            f += this.radiusPerTick;
            if (f < 0.5F) {
               this.discard();
               return;
            }

            this.setRadius(f);
         }

         if (this.tickCount % 5 == 0) {
            this.victims.entrySet().removeIf((p_287380_) -> {
               return this.tickCount >= p_287380_.getValue();
            });
            List<MobEffectInstance> list = Lists.newArrayList();

            for(MobEffectInstance mobeffectinstance : this.potion.getEffects()) {
               list.add(new MobEffectInstance(mobeffectinstance.getEffect(), mobeffectinstance.mapDuration((p_267926_) -> {
                  return p_267926_ / 4;
               }), mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()));
            }

            list.addAll(this.effects);
            if (list.isEmpty()) {
               this.victims.clear();
            } else {
               List<LivingEntity> list1 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
               if (!list1.isEmpty()) {
                  for(LivingEntity livingentity : list1) {
                     if (!this.victims.containsKey(livingentity) && livingentity.isAffectedByPotions()) {
                        double d8 = livingentity.getX() - this.getX();
                        double d1 = livingentity.getZ() - this.getZ();
                        double d3 = d8 * d8 + d1 * d1;
                        if (d3 <= (double)(f * f)) {
                           this.victims.put(livingentity, this.tickCount + this.reapplicationDelay);

                           for(MobEffectInstance mobeffectinstance1 : list) {
                              if (mobeffectinstance1.getEffect().isInstantenous()) {
                                 mobeffectinstance1.getEffect().applyInstantenousEffect(this, this.getOwner(), livingentity, mobeffectinstance1.getAmplifier(), 0.5D);
                              } else {
                                 livingentity.addEffect(new MobEffectInstance(mobeffectinstance1), this);
                              }
                           }

                           if (this.radiusOnUse != 0.0F) {
                              f += this.radiusOnUse;
                              if (f < 0.5F) {
                                 this.discard();
                                 return;
                              }

                              this.setRadius(f);
                           }

                           if (this.durationOnUse != 0) {
                              this.duration += this.durationOnUse;
                              if (this.duration <= 0) {
                                 this.discard();
                                 return;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public float getRadiusOnUse() {
      return this.radiusOnUse;
   }

   public void setRadiusOnUse(float pRadiusOnUse) {
      this.radiusOnUse = pRadiusOnUse;
   }

   public float getRadiusPerTick() {
      return this.radiusPerTick;
   }

   public void setRadiusPerTick(float pRadiusPerTick) {
      this.radiusPerTick = pRadiusPerTick;
   }

   public int getDurationOnUse() {
      return this.durationOnUse;
   }

   public void setDurationOnUse(int pDurationOnUse) {
      this.durationOnUse = pDurationOnUse;
   }

   public int getWaitTime() {
      return this.waitTime;
   }

   public void setWaitTime(int pWaitTime) {
      this.waitTime = pWaitTime;
   }

   public void setOwner(@Nullable LivingEntity pOwner) {
      this.owner = pOwner;
      this.ownerUUID = pOwner == null ? null : pOwner.getUUID();
   }

   /**
    * Returns null or the entityliving it was ignited by
    */
   @Nullable
   public LivingEntity getOwner() {
      if (this.owner == null && this.ownerUUID != null && this.level() instanceof ServerLevel) {
         Entity entity = ((ServerLevel)this.level()).getEntity(this.ownerUUID);
         if (entity instanceof LivingEntity) {
            this.owner = (LivingEntity)entity;
         }
      }

      return this.owner;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      this.tickCount = pCompound.getInt("Age");
      this.duration = pCompound.getInt("Duration");
      this.waitTime = pCompound.getInt("WaitTime");
      this.reapplicationDelay = pCompound.getInt("ReapplicationDelay");
      this.durationOnUse = pCompound.getInt("DurationOnUse");
      this.radiusOnUse = pCompound.getFloat("RadiusOnUse");
      this.radiusPerTick = pCompound.getFloat("RadiusPerTick");
      this.setRadius(pCompound.getFloat("Radius"));
      if (pCompound.hasUUID("Owner")) {
         this.ownerUUID = pCompound.getUUID("Owner");
      }

      if (pCompound.contains("Particle", 8)) {
         try {
            this.setParticle(ParticleArgument.readParticle(new StringReader(pCompound.getString("Particle")), BuiltInRegistries.PARTICLE_TYPE.asLookup()));
         } catch (CommandSyntaxException commandsyntaxexception) {
            LOGGER.warn("Couldn't load custom particle {}", pCompound.getString("Particle"), commandsyntaxexception);
         }
      }

      if (pCompound.contains("Color", 99)) {
         this.setFixedColor(pCompound.getInt("Color"));
      }

      if (pCompound.contains("Potion", 8)) {
         this.setPotion(PotionUtils.getPotion(pCompound));
      }

      if (pCompound.contains("Effects", 9)) {
         ListTag listtag = pCompound.getList("Effects", 10);
         this.effects.clear();

         for(int i = 0; i < listtag.size(); ++i) {
            MobEffectInstance mobeffectinstance = MobEffectInstance.load(listtag.getCompound(i));
            if (mobeffectinstance != null) {
               this.addEffect(mobeffectinstance);
            }
         }
      }

   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putInt("Age", this.tickCount);
      pCompound.putInt("Duration", this.duration);
      pCompound.putInt("WaitTime", this.waitTime);
      pCompound.putInt("ReapplicationDelay", this.reapplicationDelay);
      pCompound.putInt("DurationOnUse", this.durationOnUse);
      pCompound.putFloat("RadiusOnUse", this.radiusOnUse);
      pCompound.putFloat("RadiusPerTick", this.radiusPerTick);
      pCompound.putFloat("Radius", this.getRadius());
      pCompound.putString("Particle", this.getParticle().writeToString());
      if (this.ownerUUID != null) {
         pCompound.putUUID("Owner", this.ownerUUID);
      }

      if (this.fixedColor) {
         pCompound.putInt("Color", this.getColor());
      }

      if (this.potion != Potions.EMPTY) {
         pCompound.putString("Potion", BuiltInRegistries.POTION.getKey(this.potion).toString());
      }

      if (!this.effects.isEmpty()) {
         ListTag listtag = new ListTag();

         for(MobEffectInstance mobeffectinstance : this.effects) {
            listtag.add(mobeffectinstance.save(new CompoundTag()));
         }

         pCompound.put("Effects", listtag);
      }

   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      if (DATA_RADIUS.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   public Potion getPotion() {
      return this.potion;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   public EntityDimensions getDimensions(Pose pPose) {
      return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
   }
}