package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;

public class SleepInBed extends Behavior<LivingEntity> {
   public static final int COOLDOWN_AFTER_BEING_WOKEN = 100;
   private long nextOkStartTime;

   public SleepInBed() {
      super(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LAST_WOKEN, MemoryStatus.REGISTERED));
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, LivingEntity pOwner) {
      if (pOwner.isPassenger()) {
         return false;
      } else {
         Brain<?> brain = pOwner.getBrain();
         GlobalPos globalpos = brain.getMemory(MemoryModuleType.HOME).get();
         if (pLevel.dimension() != globalpos.dimension()) {
            return false;
         } else {
            Optional<Long> optional = brain.getMemory(MemoryModuleType.LAST_WOKEN);
            if (optional.isPresent()) {
               long i = pLevel.getGameTime() - optional.get();
               if (i > 0L && i < 100L) {
                  return false;
               }
            }

            BlockState blockstate = pLevel.getBlockState(globalpos.pos());
            return globalpos.pos().closerToCenterThan(pOwner.position(), 2.0D) && blockstate.is(BlockTags.BEDS) && !blockstate.getValue(BedBlock.OCCUPIED);
         }
      }
   }

   protected boolean canStillUse(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      Optional<GlobalPos> optional = pEntity.getBrain().getMemory(MemoryModuleType.HOME);
      if (!optional.isPresent()) {
         return false;
      } else {
         BlockPos blockpos = optional.get().pos();
         return pEntity.getBrain().isActive(Activity.REST) && pEntity.getY() > (double)blockpos.getY() + 0.4D && blockpos.closerToCenterThan(pEntity.position(), 1.14D);
      }
   }

   protected void start(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      if (pGameTime > this.nextOkStartTime) {
         Brain<?> brain = pEntity.getBrain();
         if (brain.hasMemoryValue(MemoryModuleType.DOORS_TO_CLOSE)) {
            Set<GlobalPos> set = brain.getMemory(MemoryModuleType.DOORS_TO_CLOSE).get();
            Optional<List<LivingEntity>> optional;
            if (brain.hasMemoryValue(MemoryModuleType.NEAREST_LIVING_ENTITIES)) {
               optional = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
            } else {
               optional = Optional.empty();
            }

            InteractWithDoor.closeDoorsThatIHaveOpenedOrPassedThrough(pLevel, pEntity, (Node)null, (Node)null, set, optional);
         }

         pEntity.startSleeping(pEntity.getBrain().getMemory(MemoryModuleType.HOME).get().pos());
      }

   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   protected void stop(ServerLevel pLevel, LivingEntity pEntity, long pGameTime) {
      if (pEntity.isSleeping()) {
         pEntity.stopSleeping();
         this.nextOkStartTime = pGameTime + 40L;
      }

   }
}