package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.pathfinder.Path;

public class YieldJobSite {
   public static BehaviorControl<Villager> create(float pSpeedModifier) {
      return BehaviorBuilder.create((p_258916_) -> {
         return p_258916_.group(p_258916_.present(MemoryModuleType.POTENTIAL_JOB_SITE), p_258916_.absent(MemoryModuleType.JOB_SITE), p_258916_.present(MemoryModuleType.NEAREST_LIVING_ENTITIES), p_258916_.registered(MemoryModuleType.WALK_TARGET), p_258916_.registered(MemoryModuleType.LOOK_TARGET)).apply(p_258916_, (p_258901_, p_258902_, p_258903_, p_258904_, p_258905_) -> {
            return (p_258912_, p_258913_, p_258914_) -> {
               if (p_258913_.isBaby()) {
                  return false;
               } else if (p_258913_.getVillagerData().getProfession() != VillagerProfession.NONE) {
                  return false;
               } else {
                  BlockPos blockpos = p_258916_.<GlobalPos>get(p_258901_).pos();
                  Optional<Holder<PoiType>> optional = p_258912_.getPoiManager().getType(blockpos);
                  if (optional.isEmpty()) {
                     return true;
                  } else {
                     p_258916_.<List<LivingEntity>>get(p_258903_).stream().filter((p_258898_) -> {
                        return p_258898_ instanceof Villager && p_258898_ != p_258913_;
                     }).map((p_258896_) -> {
                        return (Villager)p_258896_;
                     }).filter(LivingEntity::isAlive).filter((p_258919_) -> {
                        return nearbyWantsJobsite(optional.get(), p_258919_, blockpos);
                     }).findFirst().ifPresent((p_288865_) -> {
                        p_258904_.erase();
                        p_258905_.erase();
                        p_258901_.erase();
                        if (p_288865_.getBrain().getMemory(MemoryModuleType.JOB_SITE).isEmpty()) {
                           BehaviorUtils.setWalkAndLookTargetMemories(p_288865_, blockpos, pSpeedModifier, 1);
                           p_288865_.getBrain().setMemory(MemoryModuleType.POTENTIAL_JOB_SITE, GlobalPos.of(p_258912_.dimension(), blockpos));
                           DebugPackets.sendPoiTicketCountPacket(p_258912_, blockpos);
                        }

                     });
                     return true;
                  }
               }
            };
         });
      });
   }

   private static boolean nearbyWantsJobsite(Holder<PoiType> pPoi, Villager pVillager, BlockPos pPos) {
      boolean flag = pVillager.getBrain().getMemory(MemoryModuleType.POTENTIAL_JOB_SITE).isPresent();
      if (flag) {
         return false;
      } else {
         Optional<GlobalPos> optional = pVillager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
         VillagerProfession villagerprofession = pVillager.getVillagerData().getProfession();
         if (villagerprofession.heldJobSite().test(pPoi)) {
            return optional.isEmpty() ? canReachPos(pVillager, pPos, pPoi.value()) : optional.get().pos().equals(pPos);
         } else {
            return false;
         }
      }
   }

   private static boolean canReachPos(PathfinderMob pMob, BlockPos pPos, PoiType pPoi) {
      Path path = pMob.getNavigation().createPath(pPos, pPoi.validRange());
      return path != null && path.canReach();
   }
}