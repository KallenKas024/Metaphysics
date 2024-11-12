package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Digging<E extends Warden> extends Behavior<E> {
   public Digging(int pDuration) {
      super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), pDuration);
   }

   protected boolean canStillUse(ServerLevel pLevel, E pEntity, long pGameTime) {
      return pEntity.getRemovalReason() == null;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, E pOwner) {
      return pOwner.onGround() || pOwner.isInWater() || pOwner.isInLava();
   }

   protected void start(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (pEntity.onGround()) {
         pEntity.setPose(Pose.DIGGING);
         pEntity.playSound(SoundEvents.WARDEN_DIG, 5.0F, 1.0F);
      } else {
         pEntity.playSound(SoundEvents.WARDEN_AGITATED, 5.0F, 1.0F);
         this.stop(pLevel, pEntity, pGameTime);
      }

   }

   protected void stop(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (pEntity.getRemovalReason() == null) {
         pEntity.remove(Entity.RemovalReason.DISCARDED);
      }

   }
}