package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class GoAndGiveItemsToTarget<E extends LivingEntity & InventoryCarrier> extends Behavior<E> {
   private static final int CLOSE_ENOUGH_DISTANCE_TO_TARGET = 3;
   private static final int ITEM_PICKUP_COOLDOWN_AFTER_THROWING = 60;
   private final Function<LivingEntity, Optional<PositionTracker>> targetPositionGetter;
   private final float speedModifier;

   public GoAndGiveItemsToTarget(Function<LivingEntity, Optional<PositionTracker>> pTargetPositionGetter, float pSpeedModifier, int pDuration) {
      super(Map.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryStatus.REGISTERED), pDuration);
      this.targetPositionGetter = pTargetPositionGetter;
      this.speedModifier = pSpeedModifier;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      return this.canThrowItemToTarget(pOwner);
   }

   protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
      return this.canThrowItemToTarget(pEntity);
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      this.targetPositionGetter.apply(pEntity).ifPresent((p_217206_) -> {
         BehaviorUtils.setWalkAndLookTargetMemories(pEntity, p_217206_, this.speedModifier, 3);
      });
   }

   protected void tick(ServerLevel pLevel, E pOwner, long pGameTime) {
      Optional<PositionTracker> optional = this.targetPositionGetter.apply(pOwner);
      if (!optional.isEmpty()) {
         PositionTracker positiontracker = optional.get();
         double d0 = positiontracker.currentPosition().distanceTo(pOwner.getEyePosition());
         if (d0 < 3.0D) {
            ItemStack itemstack = pOwner.getInventory().removeItem(0, 1);
            if (!itemstack.isEmpty()) {
               throwItem(pOwner, itemstack, getThrowPosition(positiontracker));
               if (pOwner instanceof Allay) {
                  Allay allay = (Allay)pOwner;
                  AllayAi.getLikedPlayer(allay).ifPresent((p_217224_) -> {
                     this.triggerDropItemOnBlock(positiontracker, itemstack, p_217224_);
                  });
               }

               pOwner.getBrain().setMemory(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, 60);
            }
         }

      }
   }

   private void triggerDropItemOnBlock(PositionTracker pPositionTracker, ItemStack pStack, ServerPlayer pPlayer) {
      BlockPos blockpos = pPositionTracker.currentBlockPosition().below();
      CriteriaTriggers.ALLAY_DROP_ITEM_ON_BLOCK.trigger(pPlayer, blockpos, pStack);
   }

   private boolean canThrowItemToTarget(E pTarget) {
      if (pTarget.getInventory().isEmpty()) {
         return false;
      } else {
         Optional<PositionTracker> optional = this.targetPositionGetter.apply(pTarget);
         return optional.isPresent();
      }
   }

   private static Vec3 getThrowPosition(PositionTracker pPositionTracker) {
      return pPositionTracker.currentPosition().add(0.0D, 1.0D, 0.0D);
   }

   public static void throwItem(LivingEntity pEntity, ItemStack pStack, Vec3 pThrowPos) {
      Vec3 vec3 = new Vec3((double)0.2F, (double)0.3F, (double)0.2F);
      BehaviorUtils.throwItem(pEntity, pStack, pThrowPos, vec3, 0.2F);
      Level level = pEntity.level();
      if (level.getGameTime() % 7L == 0L && level.random.nextDouble() < 0.9D) {
         float f = Util.getRandom(Allay.THROW_SOUND_PITCHES, level.getRandom());
         level.playSound((Player)null, pEntity, SoundEvents.ALLAY_THROW, SoundSource.NEUTRAL, 1.0F, f);
      }

   }
}