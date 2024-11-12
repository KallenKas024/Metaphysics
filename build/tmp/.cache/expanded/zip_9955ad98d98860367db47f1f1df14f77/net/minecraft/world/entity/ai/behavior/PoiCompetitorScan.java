package net.minecraft.world.entity.ai.behavior;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class PoiCompetitorScan {
   public static BehaviorControl<Villager> create() {
      return BehaviorBuilder.create((p_258576_) -> {
         return p_258576_.group(p_258576_.present(MemoryModuleType.JOB_SITE), p_258576_.present(MemoryModuleType.NEAREST_LIVING_ENTITIES)).apply(p_258576_, (p_258590_, p_258591_) -> {
            return (p_258580_, p_258581_, p_258582_) -> {
               GlobalPos globalpos = p_258576_.get(p_258590_);
               p_258580_.getPoiManager().getType(globalpos.pos()).ifPresent((p_258588_) -> {
                  p_258576_.<List<LivingEntity>>get(p_258591_).stream().filter((p_258593_) -> {
                     return p_258593_ instanceof Villager && p_258593_ != p_258581_;
                  }).map((p_258583_) -> {
                     return (Villager)p_258583_;
                  }).filter(LivingEntity::isAlive).filter((p_258596_) -> {
                     return competesForSameJobsite(globalpos, p_258588_, p_258596_);
                  }).reduce(p_258581_, PoiCompetitorScan::selectWinner);
               });
               return true;
            };
         });
      });
   }

   private static Villager selectWinner(Villager p_23725_, Villager p_23726_) {
      Villager villager;
      Villager villager1;
      if (p_23725_.getVillagerXp() > p_23726_.getVillagerXp()) {
         villager = p_23725_;
         villager1 = p_23726_;
      } else {
         villager = p_23726_;
         villager1 = p_23725_;
      }

      villager1.getBrain().eraseMemory(MemoryModuleType.JOB_SITE);
      return villager;
   }

   private static boolean competesForSameJobsite(GlobalPos pJobSitePos, Holder<PoiType> pPoi, Villager pPoiType) {
      Optional<GlobalPos> optional = pPoiType.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      return optional.isPresent() && pJobSitePos.equals(optional.get()) && hasMatchingProfession(pPoi, pPoiType.getVillagerData().getProfession());
   }

   private static boolean hasMatchingProfession(Holder<PoiType> pPoi, VillagerProfession pPoiType) {
      return pPoiType.heldJobSite().test(pPoi);
   }
}