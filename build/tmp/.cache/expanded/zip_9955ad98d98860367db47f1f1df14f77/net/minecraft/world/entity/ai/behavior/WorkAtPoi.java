package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;

public class WorkAtPoi extends Behavior<Villager> {
   private static final int CHECK_COOLDOWN = 300;
   private static final double DISTANCE = 1.73D;
   private long lastCheck;

   public WorkAtPoi() {
      super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      if (pLevel.getGameTime() - this.lastCheck < 300L) {
         return false;
      } else if (pLevel.random.nextInt(2) != 0) {
         return false;
      } else {
         this.lastCheck = pLevel.getGameTime();
         GlobalPos globalpos = pOwner.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
         return globalpos.dimension() == pLevel.dimension() && globalpos.pos().closerToCenterThan(pOwner.position(), 1.73D);
      }
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      Brain<Villager> brain = pEntity.getBrain();
      brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, pGameTime);
      brain.getMemory(MemoryModuleType.JOB_SITE).ifPresent((p_24821_) -> {
         brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(p_24821_.pos()));
      });
      pEntity.playWorkSound();
      this.useWorkstation(pLevel, pEntity);
      if (pEntity.shouldRestock()) {
         pEntity.restock();
      }

   }

   protected void useWorkstation(ServerLevel pLevel, Villager pVillager) {
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      Optional<GlobalPos> optional = pEntity.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      if (!optional.isPresent()) {
         return false;
      } else {
         GlobalPos globalpos = optional.get();
         return globalpos.dimension() == pLevel.dimension() && globalpos.pos().closerToCenterThan(pEntity.position(), 1.73D);
      }
   }
}