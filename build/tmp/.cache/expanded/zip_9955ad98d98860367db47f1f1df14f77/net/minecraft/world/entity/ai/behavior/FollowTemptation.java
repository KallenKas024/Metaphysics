package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;

public class FollowTemptation extends Behavior<PathfinderMob> {
   public static final int TEMPTATION_COOLDOWN = 100;
   public static final double CLOSE_ENOUGH_DIST = 2.5D;
   private final Function<LivingEntity, Float> speedModifier;
   private final Function<LivingEntity, Double> closeEnoughDistance;

   public FollowTemptation(Function<LivingEntity, Float> pSpeedModifier) {
      this(pSpeedModifier, (p_288784_) -> {
         return 2.5D;
      });
   }

   public FollowTemptation(Function<LivingEntity, Float> pSpeedModifier, Function<LivingEntity, Double> pCloseEnoughDistance) {
      super(Util.make(() -> {
         ImmutableMap.Builder<MemoryModuleType<?>, MemoryStatus> builder = ImmutableMap.builder();
         builder.put(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED);
         builder.put(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED);
         builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryStatus.VALUE_ABSENT);
         builder.put(MemoryModuleType.IS_TEMPTED, MemoryStatus.REGISTERED);
         builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryStatus.VALUE_PRESENT);
         builder.put(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT);
         builder.put(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT);
         return builder.build();
      }));
      this.speedModifier = pSpeedModifier;
      this.closeEnoughDistance = pCloseEnoughDistance;
   }

   protected float getSpeedModifier(PathfinderMob pPathfinder) {
      return this.speedModifier.apply(pPathfinder);
   }

   private Optional<Player> getTemptingPlayer(PathfinderMob pPathfinder) {
      return pPathfinder.getBrain().getMemory(MemoryModuleType.TEMPTING_PLAYER);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   protected boolean canStillUse(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      return this.getTemptingPlayer(pEntity).isPresent() && !pEntity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET) && !pEntity.getBrain().hasMemoryValue(MemoryModuleType.IS_PANICKING);
   }

   protected void start(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      pEntity.getBrain().setMemory(MemoryModuleType.IS_TEMPTED, true);
   }

   protected void stop(ServerLevel pLevel, PathfinderMob pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.setMemory(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
      brain.setMemory(MemoryModuleType.IS_TEMPTED, false);
      brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel pLevel, PathfinderMob pOwner, long pGameTime) {
      Player player = this.getTemptingPlayer(pOwner).get();
      Brain<?> brain = pOwner.getBrain();
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
      double d0 = this.closeEnoughDistance.apply(pOwner);
      if (pOwner.distanceToSqr(player) < Mth.square(d0)) {
         brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      } else {
         brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(player, false), this.getSpeedModifier(pOwner), 2));
      }

   }
}