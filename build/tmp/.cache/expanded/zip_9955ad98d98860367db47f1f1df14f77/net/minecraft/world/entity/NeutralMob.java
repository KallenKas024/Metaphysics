package net.minecraft.world.entity;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface NeutralMob {
   String TAG_ANGER_TIME = "AngerTime";
   String TAG_ANGRY_AT = "AngryAt";

   int getRemainingPersistentAngerTime();

   void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime);

   @Nullable
   UUID getPersistentAngerTarget();

   void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget);

   void startPersistentAngerTimer();

   default void addPersistentAngerSaveData(CompoundTag pNbt) {
      pNbt.putInt("AngerTime", this.getRemainingPersistentAngerTime());
      if (this.getPersistentAngerTarget() != null) {
         pNbt.putUUID("AngryAt", this.getPersistentAngerTarget());
      }

   }

   default void readPersistentAngerSaveData(Level pLevel, CompoundTag pTag) {
      this.setRemainingPersistentAngerTime(pTag.getInt("AngerTime"));
      if (pLevel instanceof ServerLevel) {
         if (!pTag.hasUUID("AngryAt")) {
            this.setPersistentAngerTarget((UUID)null);
         } else {
            UUID uuid = pTag.getUUID("AngryAt");
            this.setPersistentAngerTarget(uuid);
            Entity entity = ((ServerLevel)pLevel).getEntity(uuid);
            if (entity != null) {
               if (entity instanceof Mob) {
                  this.setLastHurtByMob((Mob)entity);
               }

               if (entity.getType() == EntityType.PLAYER) {
                  this.setLastHurtByPlayer((Player)entity);
               }

            }
         }
      }
   }

   default void updatePersistentAnger(ServerLevel pServerLevel, boolean pUpdateAnger) {
      LivingEntity livingentity = this.getTarget();
      UUID uuid = this.getPersistentAngerTarget();
      if ((livingentity == null || livingentity.isDeadOrDying()) && uuid != null && pServerLevel.getEntity(uuid) instanceof Mob) {
         this.stopBeingAngry();
      } else {
         if (livingentity != null && !Objects.equals(uuid, livingentity.getUUID())) {
            this.setPersistentAngerTarget(livingentity.getUUID());
            this.startPersistentAngerTimer();
         }

         if (this.getRemainingPersistentAngerTime() > 0 && (livingentity == null || livingentity.getType() != EntityType.PLAYER || !pUpdateAnger)) {
            this.setRemainingPersistentAngerTime(this.getRemainingPersistentAngerTime() - 1);
            if (this.getRemainingPersistentAngerTime() == 0) {
               this.stopBeingAngry();
            }
         }

      }
   }

   default boolean isAngryAt(LivingEntity pTarget) {
      if (!this.canAttack(pTarget)) {
         return false;
      } else {
         return pTarget.getType() == EntityType.PLAYER && this.isAngryAtAllPlayers(pTarget.level()) ? true : pTarget.getUUID().equals(this.getPersistentAngerTarget());
      }
   }

   default boolean isAngryAtAllPlayers(Level pLevel) {
      return pLevel.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.isAngry() && this.getPersistentAngerTarget() == null;
   }

   default boolean isAngry() {
      return this.getRemainingPersistentAngerTime() > 0;
   }

   default void playerDied(Player pPlayer) {
      if (pPlayer.level().getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
         if (pPlayer.getUUID().equals(this.getPersistentAngerTarget())) {
            this.stopBeingAngry();
         }
      }
   }

   default void forgetCurrentTargetAndRefreshUniversalAnger() {
      this.stopBeingAngry();
      this.startPersistentAngerTimer();
   }

   default void stopBeingAngry() {
      this.setLastHurtByMob((LivingEntity)null);
      this.setPersistentAngerTarget((UUID)null);
      this.setTarget((LivingEntity)null);
      this.setRemainingPersistentAngerTime(0);
   }

   @Nullable
   LivingEntity getLastHurtByMob();

   /**
    * Hint to AI tasks that we were attacked by the passed EntityLivingBase and should retaliate. Is not guaranteed to
    * change our actual active target (for example if we are currently busy attacking someone else)
    */
   void setLastHurtByMob(@Nullable LivingEntity pLivingEntity);

   void setLastHurtByPlayer(@Nullable Player pPlayer);

   /**
    * Sets the active target the Goal system uses for tracking
    */
   void setTarget(@Nullable LivingEntity pLivingEntity);

   boolean canAttack(LivingEntity pEntity);

   /**
    * Gets the active target the Goal system uses for tracking
    */
   @Nullable
   LivingEntity getTarget();
}