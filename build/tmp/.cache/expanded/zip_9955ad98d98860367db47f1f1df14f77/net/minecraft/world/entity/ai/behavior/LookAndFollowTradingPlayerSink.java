package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public class LookAndFollowTradingPlayerSink extends Behavior<Villager> {
   private final float speedModifier;

   public LookAndFollowTradingPlayerSink(float pSpeedModifier) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), Integer.MAX_VALUE);
      this.speedModifier = pSpeedModifier;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      Player player = pOwner.getTradingPlayer();
      return pOwner.isAlive() && player != null && !pOwner.isInWater() && !pOwner.hurtMarked && pOwner.distanceToSqr(player) <= 16.0D && player.containerMenu != null;
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      this.followPlayer(pEntity);
   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      Brain<?> brain = pEntity.getBrain();
      brain.eraseMemory(MemoryModuleType.WALK_TARGET);
      brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      this.followPlayer(pOwner);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   private void followPlayer(Villager pOwner) {
      Brain<?> brain = pOwner.getBrain();
      brain.setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityTracker(pOwner.getTradingPlayer(), false), this.speedModifier, 2));
      brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(pOwner.getTradingPlayer(), true));
   }
}