package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class VillagerPanicTrigger extends Behavior<Villager> {
   public VillagerPanicTrigger() {
      super(ImmutableMap.of());
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return isHurt(pEntity) || hasHostile(pEntity);
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      if (isHurt(pEntity) || hasHostile(pEntity)) {
         Brain<?> brain = pEntity.getBrain();
         if (!brain.isActive(Activity.PANIC)) {
            brain.eraseMemory(MemoryModuleType.PATH);
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
            brain.eraseMemory(MemoryModuleType.BREED_TARGET);
            brain.eraseMemory(MemoryModuleType.INTERACTION_TARGET);
         }

         brain.setActiveActivityIfPossible(Activity.PANIC);
      }

   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      if (pGameTime % 100L == 0L) {
         pOwner.spawnGolemIfNeeded(pLevel, pGameTime, 3);
      }

   }

   public static boolean hasHostile(LivingEntity pEntity) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_HOSTILE);
   }

   public static boolean isHurt(LivingEntity pEntity) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.HURT_BY);
   }
}