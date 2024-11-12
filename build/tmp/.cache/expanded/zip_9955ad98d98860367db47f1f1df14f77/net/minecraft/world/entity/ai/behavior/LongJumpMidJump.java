package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;

public class LongJumpMidJump extends Behavior<Mob> {
   public static final int TIME_OUT_DURATION = 100;
   private final UniformInt timeBetweenLongJumps;
   private final SoundEvent landingSound;

   public LongJumpMidJump(UniformInt pTimeBetweenLongJumps, SoundEvent pLandingSound) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryStatus.VALUE_PRESENT), 100);
      this.timeBetweenLongJumps = pTimeBetweenLongJumps;
      this.landingSound = pLandingSound;
   }

   protected boolean canStillUse(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      return !pEntity.onGround();
   }

   protected void start(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      pEntity.setDiscardFriction(true);
      pEntity.setPose(Pose.LONG_JUMPING);
   }

   protected void stop(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      if (pEntity.onGround()) {
         pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply((double)0.1F, 1.0D, (double)0.1F));
         pLevel.playSound((Player)null, pEntity, this.landingSound, SoundSource.NEUTRAL, 2.0F, 1.0F);
      }

      pEntity.setDiscardFriction(false);
      pEntity.setPose(Pose.STANDING);
      pEntity.getBrain().eraseMemory(MemoryModuleType.LONG_JUMP_MID_JUMP);
      pEntity.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(pLevel.random));
   }
}