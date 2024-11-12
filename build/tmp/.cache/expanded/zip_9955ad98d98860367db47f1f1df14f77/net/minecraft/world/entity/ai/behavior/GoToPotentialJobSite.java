package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;

public class GoToPotentialJobSite extends Behavior<Villager> {
   private static final int TICKS_UNTIL_TIMEOUT = 1200;
   final float speedModifier;

   public GoToPotentialJobSite(float pSpeedModifier) {
      super(ImmutableMap.of(MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_PRESENT), 1200);
      this.speedModifier = pSpeedModifier;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      return pOwner.getBrain().getActiveNonCoreActivity().map((p_23115_) -> {
         return p_23115_ == Activity.IDLE || p_23115_ == Activity.WORK || p_23115_ == Activity.PLAY;
      }).orElse(true);
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE);
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      BehaviorUtils.setWalkAndLookTargetMemories(pOwner, pOwner.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get().pos(), this.speedModifier, 1);
   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      Optional<GlobalPos> optional = pEntity.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
      optional.ifPresent((p_23111_) -> {
         BlockPos blockpos = p_23111_.pos();
         ServerLevel serverlevel = pLevel.getServer().getLevel(p_23111_.dimension());
         if (serverlevel != null) {
            PoiManager poimanager = serverlevel.getPoiManager();
            if (poimanager.exists(blockpos, (p_217230_) -> {
               return true;
            })) {
               poimanager.release(blockpos);
            }

            DebugPackets.sendPoiTicketCountPacket(pLevel, blockpos);
         }
      });
      pEntity.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
   }
}