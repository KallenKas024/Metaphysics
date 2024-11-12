package net.minecraft.world.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

public class Interaction extends Entity implements Attackable, Targeting {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final EntityDataAccessor<Float> DATA_WIDTH_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Float> DATA_HEIGHT_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Boolean> DATA_RESPONSE_ID = SynchedEntityData.defineId(Interaction.class, EntityDataSerializers.BOOLEAN);
   private static final String TAG_WIDTH = "width";
   private static final String TAG_HEIGHT = "height";
   private static final String TAG_ATTACK = "attack";
   private static final String TAG_INTERACTION = "interaction";
   private static final String TAG_RESPONSE = "response";
   @Nullable
   private Interaction.PlayerAction attack;
   @Nullable
   private Interaction.PlayerAction interaction;

   public Interaction(EntityType<?> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.noPhysics = true;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_WIDTH_ID, 1.0F);
      this.entityData.define(DATA_HEIGHT_ID, 1.0F);
      this.entityData.define(DATA_RESPONSE_ID, false);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundTag pCompound) {
      if (pCompound.contains("width", 99)) {
         this.setWidth(pCompound.getFloat("width"));
      }

      if (pCompound.contains("height", 99)) {
         this.setHeight(pCompound.getFloat("height"));
      }

      if (pCompound.contains("attack")) {
         Interaction.PlayerAction.CODEC.decode(NbtOps.INSTANCE, pCompound.get("attack")).resultOrPartial(Util.prefix("Interaction entity", LOGGER::error)).ifPresent((p_273699_) -> {
            this.attack = p_273699_.getFirst();
         });
      } else {
         this.attack = null;
      }

      if (pCompound.contains("interaction")) {
         Interaction.PlayerAction.CODEC.decode(NbtOps.INSTANCE, pCompound.get("interaction")).resultOrPartial(Util.prefix("Interaction entity", LOGGER::error)).ifPresent((p_273686_) -> {
            this.interaction = p_273686_.getFirst();
         });
      } else {
         this.interaction = null;
      }

      this.setResponse(pCompound.getBoolean("response"));
      this.setBoundingBox(this.makeBoundingBox());
   }

   protected void addAdditionalSaveData(CompoundTag pCompound) {
      pCompound.putFloat("width", this.getWidth());
      pCompound.putFloat("height", this.getHeight());
      if (this.attack != null) {
         Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.attack).result().ifPresent((p_272806_) -> {
            pCompound.put("attack", p_272806_);
         });
      }

      if (this.interaction != null) {
         Interaction.PlayerAction.CODEC.encodeStart(NbtOps.INSTANCE, this.interaction).result().ifPresent((p_272925_) -> {
            pCompound.put("interaction", p_272925_);
         });
      }

      pCompound.putBoolean("response", this.getResponse());
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_HEIGHT_ID.equals(pKey) || DATA_WIDTH_ID.equals(pKey)) {
         this.setBoundingBox(this.makeBoundingBox());
      }

   }

   public boolean canBeHitByProjectile() {
      return false;
   }

   /**
    * Returns {@code true} if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return true;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   /**
    * Return whether this entity should NOT trigger a pressure plate or a tripwire.
    */
   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   /**
    * Called when a player attacks an entity. If this returns true the attack will not happen.
    */
   public boolean skipAttackInteraction(Entity pEntity) {
      if (pEntity instanceof Player player) {
         this.attack = new Interaction.PlayerAction(player.getUUID(), this.level().getGameTime());
         if (player instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger(serverplayer, this, player.damageSources().generic(), 1.0F, 1.0F, false);
         }

         return !this.getResponse();
      } else {
         return false;
      }
   }

   public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
      if (this.level().isClientSide) {
         return this.getResponse() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
      } else {
         this.interaction = new Interaction.PlayerAction(pPlayer.getUUID(), this.level().getGameTime());
         return InteractionResult.CONSUME;
      }
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
   }

   @Nullable
   public LivingEntity getLastAttacker() {
      return this.attack != null ? this.level().getPlayerByUUID(this.attack.player()) : null;
   }

   /**
    * Gets the active target the Goal system uses for tracking
    */
   @Nullable
   public LivingEntity getTarget() {
      return this.interaction != null ? this.level().getPlayerByUUID(this.interaction.player()) : null;
   }

   private void setWidth(float pWidth) {
      this.entityData.set(DATA_WIDTH_ID, pWidth);
   }

   private float getWidth() {
      return this.entityData.get(DATA_WIDTH_ID);
   }

   private void setHeight(float pHeight) {
      this.entityData.set(DATA_HEIGHT_ID, pHeight);
   }

   private float getHeight() {
      return this.entityData.get(DATA_HEIGHT_ID);
   }

   private void setResponse(boolean pResponse) {
      this.entityData.set(DATA_RESPONSE_ID, pResponse);
   }

   private boolean getResponse() {
      return this.entityData.get(DATA_RESPONSE_ID);
   }

   private EntityDimensions getDimensions() {
      return EntityDimensions.scalable(this.getWidth(), this.getHeight());
   }

   public EntityDimensions getDimensions(Pose pPose) {
      return this.getDimensions();
   }

   protected AABB makeBoundingBox() {
      return this.getDimensions().makeBoundingBox(this.position());
   }

   static record PlayerAction(UUID player, long timestamp) {
      public static final Codec<Interaction.PlayerAction> CODEC = RecordCodecBuilder.create((p_273237_) -> {
         return p_273237_.group(UUIDUtil.CODEC.fieldOf("player").forGetter(Interaction.PlayerAction::player), Codec.LONG.fieldOf("timestamp").forGetter(Interaction.PlayerAction::timestamp)).apply(p_273237_, Interaction.PlayerAction::new);
      });
   }
}