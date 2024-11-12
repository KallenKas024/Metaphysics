package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GoToTargetLocation {
   private static BlockPos getNearbyPos(Mob pMob, BlockPos pPos) {
      RandomSource randomsource = pMob.level().random;
      return pPos.offset(getRandomOffset(randomsource), 0, getRandomOffset(randomsource));
   }

   private static int getRandomOffset(RandomSource pRandom) {
      return pRandom.nextInt(3) - 1;
   }

   public static <E extends Mob> OneShot<E> create(MemoryModuleType<BlockPos> pLocationMemory, int pCloseEnoughDist, float pSpeedModifier) {
      return BehaviorBuilder.create((p_259997_) -> {
         return p_259997_.group(p_259997_.present(pLocationMemory), p_259997_.absent(MemoryModuleType.ATTACK_TARGET), p_259997_.absent(MemoryModuleType.WALK_TARGET), p_259997_.registered(MemoryModuleType.LOOK_TARGET)).apply(p_259997_, (p_259831_, p_259115_, p_259521_, p_259223_) -> {
            return (p_289322_, p_289323_, p_289324_) -> {
               BlockPos blockpos = p_259997_.get(p_259831_);
               boolean flag = blockpos.closerThan(p_289323_.blockPosition(), (double)pCloseEnoughDist);
               if (!flag) {
                  BehaviorUtils.setWalkAndLookTargetMemories(p_289323_, getNearbyPos(p_289323_, blockpos), pSpeedModifier, pCloseEnoughDist);
               }

               return true;
            };
         });
      });
   }
}