package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

public class RandomLookAround extends Behavior<Mob> {
   private final IntProvider interval;
   private final float maxYaw;
   private final float minPitch;
   private final float pitchRange;

   public RandomLookAround(IntProvider pInterval, float pMaxYaw, float pMinPitch, float pMaxPitch) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT));
      if (pMinPitch > pMaxPitch) {
         throw new IllegalArgumentException("Minimum pitch is larger than maximum pitch! " + pMinPitch + " > " + pMaxPitch);
      } else {
         this.interval = pInterval;
         this.maxYaw = pMaxYaw;
         this.minPitch = pMinPitch;
         this.pitchRange = pMaxPitch - pMinPitch;
      }
   }

   protected void start(ServerLevel pLevel, Mob pEntity, long pGameTime) {
      RandomSource randomsource = pEntity.getRandom();
      float f = Mth.clamp(randomsource.nextFloat() * this.pitchRange + this.minPitch, -90.0F, 90.0F);
      float f1 = Mth.wrapDegrees(pEntity.getYRot() + 2.0F * randomsource.nextFloat() * this.maxYaw - this.maxYaw);
      Vec3 vec3 = Vec3.directionFromRotation(f, f1);
      pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pEntity.getEyePosition().add(vec3)));
      pEntity.getBrain().setMemory(MemoryModuleType.GAZE_COOLDOWN_TICKS, this.interval.sample(randomsource));
   }
}