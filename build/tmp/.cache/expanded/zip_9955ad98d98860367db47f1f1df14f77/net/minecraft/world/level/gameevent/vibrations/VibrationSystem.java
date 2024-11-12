package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Optional;
import java.util.function.ToIntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface VibrationSystem {
   GameEvent[] RESONANCE_EVENTS = new GameEvent[]{GameEvent.RESONATE_1, GameEvent.RESONATE_2, GameEvent.RESONATE_3, GameEvent.RESONATE_4, GameEvent.RESONATE_5, GameEvent.RESONATE_6, GameEvent.RESONATE_7, GameEvent.RESONATE_8, GameEvent.RESONATE_9, GameEvent.RESONATE_10, GameEvent.RESONATE_11, GameEvent.RESONATE_12, GameEvent.RESONATE_13, GameEvent.RESONATE_14, GameEvent.RESONATE_15};
   ToIntFunction<GameEvent> VIBRATION_FREQUENCY_FOR_EVENT = Util.make(new Object2IntOpenHashMap<>(), (p_282267_) -> {
      p_282267_.defaultReturnValue(0);
      p_282267_.put(GameEvent.STEP, 1);
      p_282267_.put(GameEvent.SWIM, 1);
      p_282267_.put(GameEvent.FLAP, 1);
      p_282267_.put(GameEvent.PROJECTILE_LAND, 2);
      p_282267_.put(GameEvent.HIT_GROUND, 2);
      p_282267_.put(GameEvent.SPLASH, 2);
      p_282267_.put(GameEvent.ITEM_INTERACT_FINISH, 3);
      p_282267_.put(GameEvent.PROJECTILE_SHOOT, 3);
      p_282267_.put(GameEvent.INSTRUMENT_PLAY, 3);
      p_282267_.put(GameEvent.ENTITY_ROAR, 4);
      p_282267_.put(GameEvent.ENTITY_SHAKE, 4);
      p_282267_.put(GameEvent.ELYTRA_GLIDE, 4);
      p_282267_.put(GameEvent.ENTITY_DISMOUNT, 5);
      p_282267_.put(GameEvent.EQUIP, 5);
      p_282267_.put(GameEvent.ENTITY_INTERACT, 6);
      p_282267_.put(GameEvent.SHEAR, 6);
      p_282267_.put(GameEvent.ENTITY_MOUNT, 6);
      p_282267_.put(GameEvent.ENTITY_DAMAGE, 7);
      p_282267_.put(GameEvent.DRINK, 8);
      p_282267_.put(GameEvent.EAT, 8);
      p_282267_.put(GameEvent.CONTAINER_CLOSE, 9);
      p_282267_.put(GameEvent.BLOCK_CLOSE, 9);
      p_282267_.put(GameEvent.BLOCK_DEACTIVATE, 9);
      p_282267_.put(GameEvent.BLOCK_DETACH, 9);
      p_282267_.put(GameEvent.CONTAINER_OPEN, 10);
      p_282267_.put(GameEvent.BLOCK_OPEN, 10);
      p_282267_.put(GameEvent.BLOCK_ACTIVATE, 10);
      p_282267_.put(GameEvent.BLOCK_ATTACH, 10);
      p_282267_.put(GameEvent.PRIME_FUSE, 10);
      p_282267_.put(GameEvent.NOTE_BLOCK_PLAY, 10);
      p_282267_.put(GameEvent.BLOCK_CHANGE, 11);
      p_282267_.put(GameEvent.BLOCK_DESTROY, 12);
      p_282267_.put(GameEvent.FLUID_PICKUP, 12);
      p_282267_.put(GameEvent.BLOCK_PLACE, 13);
      p_282267_.put(GameEvent.FLUID_PLACE, 13);
      p_282267_.put(GameEvent.ENTITY_PLACE, 14);
      p_282267_.put(GameEvent.LIGHTNING_STRIKE, 14);
      p_282267_.put(GameEvent.TELEPORT, 14);
      p_282267_.put(GameEvent.ENTITY_DIE, 15);
      p_282267_.put(GameEvent.EXPLODE, 15);

      for(int i = 1; i <= 15; ++i) {
         p_282267_.put(getResonanceEventByFrequency(i), i);
      }

   });

   VibrationSystem.Data getVibrationData();

   VibrationSystem.User getVibrationUser();

   static int getGameEventFrequency(GameEvent pGameEvent) {
      return VIBRATION_FREQUENCY_FOR_EVENT.applyAsInt(pGameEvent);
   }

   static GameEvent getResonanceEventByFrequency(int pFrequency) {
      return RESONANCE_EVENTS[pFrequency - 1];
   }

   static int getRedstoneStrengthForDistance(float pDistance, int pMaxDistance) {
      double d0 = 15.0D / (double)pMaxDistance;
      return Math.max(1, 15 - Mth.floor(d0 * (double)pDistance));
   }

   public static final class Data {
      public static Codec<VibrationSystem.Data> CODEC = RecordCodecBuilder.create((p_283387_) -> {
         return p_283387_.group(VibrationInfo.CODEC.optionalFieldOf("event").forGetter((p_281665_) -> {
            return Optional.ofNullable(p_281665_.currentVibration);
         }), VibrationSelector.CODEC.fieldOf("selector").forGetter(VibrationSystem.Data::getSelectionStrategy), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("event_delay").orElse(0).forGetter(VibrationSystem.Data::getTravelTimeInTicks)).apply(p_283387_, (p_281934_, p_282381_, p_282931_) -> {
            return new VibrationSystem.Data(p_281934_.orElse((VibrationInfo)null), p_282381_, p_282931_, true);
         });
      });
      public static final String NBT_TAG_KEY = "listener";
      @Nullable
      VibrationInfo currentVibration;
      private int travelTimeInTicks;
      final VibrationSelector selectionStrategy;
      private boolean reloadVibrationParticle;

      private Data(@Nullable VibrationInfo pCurrentVibration, VibrationSelector pSelectionStrategy, int pTravelTimeInTicks, boolean pReloadVibrationParticle) {
         this.currentVibration = pCurrentVibration;
         this.travelTimeInTicks = pTravelTimeInTicks;
         this.selectionStrategy = pSelectionStrategy;
         this.reloadVibrationParticle = pReloadVibrationParticle;
      }

      public Data() {
         this((VibrationInfo)null, new VibrationSelector(), 0, false);
      }

      public VibrationSelector getSelectionStrategy() {
         return this.selectionStrategy;
      }

      @Nullable
      public VibrationInfo getCurrentVibration() {
         return this.currentVibration;
      }

      public void setCurrentVibration(@Nullable VibrationInfo pCurrentVibration) {
         this.currentVibration = pCurrentVibration;
      }

      public int getTravelTimeInTicks() {
         return this.travelTimeInTicks;
      }

      public void setTravelTimeInTicks(int pTravelTimeInTicks) {
         this.travelTimeInTicks = pTravelTimeInTicks;
      }

      public void decrementTravelTime() {
         this.travelTimeInTicks = Math.max(0, this.travelTimeInTicks - 1);
      }

      public boolean shouldReloadVibrationParticle() {
         return this.reloadVibrationParticle;
      }

      public void setReloadVibrationParticle(boolean pReloadVibrationParticle) {
         this.reloadVibrationParticle = pReloadVibrationParticle;
      }
   }

   public static class Listener implements GameEventListener {
      private final VibrationSystem system;

      public Listener(VibrationSystem pSystem) {
         this.system = pSystem;
      }

      /**
       * Gets the position of the listener itself.
       */
      public PositionSource getListenerSource() {
         return this.system.getVibrationUser().getPositionSource();
      }

      /**
       * Gets the listening radius of the listener. Events within this radius will notify the listener when broadcasted.
       */
      public int getListenerRadius() {
         return this.system.getVibrationUser().getListenerRadius();
      }

      public boolean handleGameEvent(ServerLevel pLevel, GameEvent pGameEvent, GameEvent.Context pContext, Vec3 pPos) {
         VibrationSystem.Data vibrationsystem$data = this.system.getVibrationData();
         VibrationSystem.User vibrationsystem$user = this.system.getVibrationUser();
         if (vibrationsystem$data.getCurrentVibration() != null) {
            return false;
         } else if (!vibrationsystem$user.isValidVibration(pGameEvent, pContext)) {
            return false;
         } else {
            Optional<Vec3> optional = vibrationsystem$user.getPositionSource().getPosition(pLevel);
            if (optional.isEmpty()) {
               return false;
            } else {
               Vec3 vec3 = optional.get();
               if (!vibrationsystem$user.canReceiveVibration(pLevel, BlockPos.containing(pPos), pGameEvent, pContext)) {
                  return false;
               } else if (isOccluded(pLevel, pPos, vec3)) {
                  return false;
               } else {
                  this.scheduleVibration(pLevel, vibrationsystem$data, pGameEvent, pContext, pPos, vec3);
                  return true;
               }
            }
         }
      }

      public void forceScheduleVibration(ServerLevel pLevel, GameEvent pGameEvent, GameEvent.Context pContext, Vec3 pEventPos) {
         this.system.getVibrationUser().getPositionSource().getPosition(pLevel).ifPresent((p_281936_) -> {
            this.scheduleVibration(pLevel, this.system.getVibrationData(), pGameEvent, pContext, pEventPos, p_281936_);
         });
      }

      private void scheduleVibration(ServerLevel pLevel, VibrationSystem.Data pData, GameEvent pGameEvent, GameEvent.Context pContext, Vec3 pPos, Vec3 pVibrationUserPos) {
         pData.selectionStrategy.addCandidate(new VibrationInfo(pGameEvent, (float)pPos.distanceTo(pVibrationUserPos), pPos, pContext.sourceEntity()), pLevel.getGameTime());
      }

      public static float distanceBetweenInBlocks(BlockPos pPos1, BlockPos pPos2) {
         return (float)Math.sqrt(pPos1.distSqr(pPos2));
      }

      private static boolean isOccluded(Level pLevel, Vec3 pEventPos, Vec3 pVibrationUserPos) {
         Vec3 vec3 = new Vec3((double)Mth.floor(pEventPos.x) + 0.5D, (double)Mth.floor(pEventPos.y) + 0.5D, (double)Mth.floor(pEventPos.z) + 0.5D);
         Vec3 vec31 = new Vec3((double)Mth.floor(pVibrationUserPos.x) + 0.5D, (double)Mth.floor(pVibrationUserPos.y) + 0.5D, (double)Mth.floor(pVibrationUserPos.z) + 0.5D);

         for(Direction direction : Direction.values()) {
            Vec3 vec32 = vec3.relative(direction, (double)1.0E-5F);
            if (pLevel.isBlockInLine(new ClipBlockStateContext(vec32, vec31, (p_283608_) -> {
               return p_283608_.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS);
            })).getType() != HitResult.Type.BLOCK) {
               return false;
            }
         }

         return true;
      }
   }

   public interface Ticker {
      static void tick(Level pLevel, VibrationSystem.Data pData, VibrationSystem.User pUser) {
         if (pLevel instanceof ServerLevel serverlevel) {
            if (pData.currentVibration == null) {
               trySelectAndScheduleVibration(serverlevel, pData, pUser);
            }

            if (pData.currentVibration != null) {
               boolean flag = pData.getTravelTimeInTicks() > 0;
               tryReloadVibrationParticle(serverlevel, pData, pUser);
               pData.decrementTravelTime();
               if (pData.getTravelTimeInTicks() <= 0) {
                  flag = receiveVibration(serverlevel, pData, pUser, pData.currentVibration);
               }

               if (flag) {
                  pUser.onDataChanged();
               }

            }
         }
      }

      private static void trySelectAndScheduleVibration(ServerLevel pLevel, VibrationSystem.Data pData, VibrationSystem.User pUser) {
         pData.getSelectionStrategy().chosenCandidate(pLevel.getGameTime()).ifPresent((p_282059_) -> {
            pData.setCurrentVibration(p_282059_);
            Vec3 vec3 = p_282059_.pos();
            pData.setTravelTimeInTicks(pUser.calculateTravelTimeInTicks(p_282059_.distance()));
            pLevel.sendParticles(new VibrationParticleOption(pUser.getPositionSource(), pData.getTravelTimeInTicks()), vec3.x, vec3.y, vec3.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            pUser.onDataChanged();
            pData.getSelectionStrategy().startOver();
         });
      }

      private static void tryReloadVibrationParticle(ServerLevel pLevel, VibrationSystem.Data pData, VibrationSystem.User pUser) {
         if (pData.shouldReloadVibrationParticle()) {
            if (pData.currentVibration == null) {
               pData.setReloadVibrationParticle(false);
            } else {
               Vec3 vec3 = pData.currentVibration.pos();
               PositionSource positionsource = pUser.getPositionSource();
               Vec3 vec31 = positionsource.getPosition(pLevel).orElse(vec3);
               int i = pData.getTravelTimeInTicks();
               int j = pUser.calculateTravelTimeInTicks(pData.currentVibration.distance());
               double d0 = 1.0D - (double)i / (double)j;
               double d1 = Mth.lerp(d0, vec3.x, vec31.x);
               double d2 = Mth.lerp(d0, vec3.y, vec31.y);
               double d3 = Mth.lerp(d0, vec3.z, vec31.z);
               boolean flag = pLevel.sendParticles(new VibrationParticleOption(positionsource, i), d1, d2, d3, 1, 0.0D, 0.0D, 0.0D, 0.0D) > 0;
               if (flag) {
                  pData.setReloadVibrationParticle(false);
               }

            }
         }
      }

      private static boolean receiveVibration(ServerLevel pLevel, VibrationSystem.Data pData, VibrationSystem.User pUser, VibrationInfo pVibrationInfo) {
         BlockPos blockpos = BlockPos.containing(pVibrationInfo.pos());
         BlockPos blockpos1 = pUser.getPositionSource().getPosition(pLevel).map(BlockPos::containing).orElse(blockpos);
         if (pUser.requiresAdjacentChunksToBeTicking() && !areAdjacentChunksTicking(pLevel, blockpos1)) {
            return false;
         } else {
            pUser.onReceiveVibration(pLevel, blockpos, pVibrationInfo.gameEvent(), pVibrationInfo.getEntity(pLevel).orElse((Entity)null), pVibrationInfo.getProjectileOwner(pLevel).orElse((Entity)null), VibrationSystem.Listener.distanceBetweenInBlocks(blockpos, blockpos1));
            pData.setCurrentVibration((VibrationInfo)null);
            return true;
         }
      }

      private static boolean areAdjacentChunksTicking(Level pLevel, BlockPos pPos) {
         ChunkPos chunkpos = new ChunkPos(pPos);

         for(int i = chunkpos.x - 1; i < chunkpos.x + 1; ++i) {
            for(int j = chunkpos.z - 1; j < chunkpos.z + 1; ++j) {
               ChunkAccess chunkaccess = pLevel.getChunkSource().getChunkNow(i, j);
               if (chunkaccess == null || !pLevel.shouldTickBlocksAt(chunkaccess.getPos().toLong())) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public interface User {
      int getListenerRadius();

      PositionSource getPositionSource();

      boolean canReceiveVibration(ServerLevel pLevel, BlockPos pPos, GameEvent pGameEvent, GameEvent.Context pContext);

      void onReceiveVibration(ServerLevel pLevel, BlockPos pPos, GameEvent pGameEvent, @Nullable Entity pEntity, @Nullable Entity pPlayerEntity, float pDistance);

      default TagKey<GameEvent> getListenableEvents() {
         return GameEventTags.VIBRATIONS;
      }

      default boolean canTriggerAvoidVibration() {
         return false;
      }

      default boolean requiresAdjacentChunksToBeTicking() {
         return false;
      }

      default int calculateTravelTimeInTicks(float pDistance) {
         return Mth.floor(pDistance);
      }

      default boolean isValidVibration(GameEvent pGameEvent, GameEvent.Context pContext) {
         if (!pGameEvent.is(this.getListenableEvents())) {
            return false;
         } else {
            Entity entity = pContext.sourceEntity();
            if (entity != null) {
               if (entity.isSpectator()) {
                  return false;
               }

               if (entity.isSteppingCarefully() && pGameEvent.is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)) {
                  if (this.canTriggerAvoidVibration() && entity instanceof ServerPlayer) {
                     ServerPlayer serverplayer = (ServerPlayer)entity;
                     CriteriaTriggers.AVOID_VIBRATION.trigger(serverplayer);
                  }

                  return false;
               }

               if (entity.dampensVibrations()) {
                  return false;
               }
            }

            if (pContext.affectedState() != null) {
               return !pContext.affectedState().is(BlockTags.DAMPENS_VIBRATIONS);
            } else {
               return true;
            }
         }
      }

      default void onDataChanged() {
      }
   }
}