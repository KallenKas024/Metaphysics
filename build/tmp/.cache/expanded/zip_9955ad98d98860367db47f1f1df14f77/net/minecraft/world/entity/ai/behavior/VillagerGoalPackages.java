package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.raid.Raid;

public class VillagerGoalPackages {
   private static final float STROLL_SPEED_MODIFIER = 0.4F;

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getCorePackage(VillagerProfession pProfession, float pSpeedModifier) {
      return ImmutableList.of(Pair.of(0, new Swim(0.8F)), Pair.of(0, InteractWithDoor.create()), Pair.of(0, new LookAtTargetSink(45, 90)), Pair.of(0, new VillagerPanicTrigger()), Pair.of(0, WakeUp.create()), Pair.of(0, ReactToBell.create()), Pair.of(0, SetRaidStatus.create()), Pair.of(0, ValidateNearbyPoi.create(pProfession.heldJobSite(), MemoryModuleType.JOB_SITE)), Pair.of(0, ValidateNearbyPoi.create(pProfession.acquirableJobSite(), MemoryModuleType.POTENTIAL_JOB_SITE)), Pair.of(1, new MoveToTargetSink()), Pair.of(2, PoiCompetitorScan.create()), Pair.of(3, new LookAndFollowTradingPlayerSink(pSpeedModifier)), Pair.of(5, GoToWantedItem.create(pSpeedModifier, false, 4)), Pair.of(6, AcquirePoi.create(pProfession.acquirableJobSite(), MemoryModuleType.JOB_SITE, MemoryModuleType.POTENTIAL_JOB_SITE, true, Optional.empty())), Pair.of(7, new GoToPotentialJobSite(pSpeedModifier)), Pair.of(8, YieldJobSite.create(pSpeedModifier)), Pair.of(10, AcquirePoi.create((p_217499_) -> {
         return p_217499_.is(PoiTypes.HOME);
      }, MemoryModuleType.HOME, false, Optional.of((byte)14))), Pair.of(10, AcquirePoi.create((p_217497_) -> {
         return p_217497_.is(PoiTypes.MEETING);
      }, MemoryModuleType.MEETING_POINT, true, Optional.of((byte)14))), Pair.of(10, AssignProfessionFromJobSite.create()), Pair.of(10, ResetProfession.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getWorkPackage(VillagerProfession pProfession, float pSpeedModifier) {
      WorkAtPoi workatpoi;
      if (pProfession == VillagerProfession.FARMER) {
         workatpoi = new WorkAtComposter();
      } else {
         workatpoi = new WorkAtPoi();
      }

      return ImmutableList.of(getMinimalLookBehavior(), Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(workatpoi, 7), Pair.of(StrollAroundPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 4), 2), Pair.of(StrollToPoi.create(MemoryModuleType.JOB_SITE, 0.4F, 1, 10), 5), Pair.of(StrollToPoiList.create(MemoryModuleType.SECONDARY_JOB_SITE, pSpeedModifier, 1, 6, MemoryModuleType.JOB_SITE), 5), Pair.of(new HarvestFarmland(), pProfession == VillagerProfession.FARMER ? 2 : 5), Pair.of(new UseBonemeal(), pProfession == VillagerProfession.FARMER ? 4 : 7)))), Pair.of(10, new ShowTradesToPlayer(400, 1600)), Pair.of(10, SetLookAndInteract.create(EntityType.PLAYER, 4)), Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.JOB_SITE, pSpeedModifier, 9, 100, 1200)), Pair.of(3, new GiveGiftToHero(100)), Pair.of(99, UpdateActivityFromSchedule.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPlayPackage(float pSpeedModifier) {
      return ImmutableList.of(Pair.of(0, new MoveToTargetSink(80, 120)), getFullLookBehavior(), Pair.of(5, PlayTagWithOtherKids.create()), Pair.of(5, new RunOne<>(ImmutableMap.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, pSpeedModifier, 2), 2), Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, pSpeedModifier, 2), 1), Pair.of(VillageBoundRandomStroll.create(pSpeedModifier), 1), Pair.of(SetWalkTargetFromLookTarget.create(pSpeedModifier, 2), 1), Pair.of(new JumpOnBed(pSpeedModifier), 2), Pair.of(new DoNothing(20, 40), 2)))), Pair.of(99, UpdateActivityFromSchedule.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getRestPackage(VillagerProfession pProfession, float pSpeedModifier) {
      return ImmutableList.of(Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.HOME, pSpeedModifier, 1, 150, 1200)), Pair.of(3, ValidateNearbyPoi.create((p_217495_) -> {
         return p_217495_.is(PoiTypes.HOME);
      }, MemoryModuleType.HOME)), Pair.of(3, new SleepInBed()), Pair.of(5, new RunOne<>(ImmutableMap.of(MemoryModuleType.HOME, MemoryStatus.VALUE_ABSENT), ImmutableList.of(Pair.of(SetClosestHomeAsWalkTarget.create(pSpeedModifier), 1), Pair.of(InsideBrownianWalk.create(pSpeedModifier), 4), Pair.of(GoToClosestVillage.create(pSpeedModifier, 4), 2), Pair.of(new DoNothing(20, 40), 2)))), getMinimalLookBehavior(), Pair.of(99, UpdateActivityFromSchedule.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getMeetPackage(VillagerProfession pProfession, float pSpeedModifier) {
      return ImmutableList.of(Pair.of(2, TriggerGate.triggerOneShuffled(ImmutableList.of(Pair.of(StrollAroundPoi.create(MemoryModuleType.MEETING_POINT, 0.4F, 40), 2), Pair.of(SocializeAtBell.create(), 2)))), Pair.of(10, new ShowTradesToPlayer(400, 1600)), Pair.of(10, SetLookAndInteract.create(EntityType.PLAYER, 4)), Pair.of(2, SetWalkTargetFromBlockMemory.create(MemoryModuleType.MEETING_POINT, pSpeedModifier, 6, 100, 200)), Pair.of(3, new GiveGiftToHero(100)), Pair.of(3, ValidateNearbyPoi.create((p_217493_) -> {
         return p_217493_.is(PoiTypes.MEETING);
      }, MemoryModuleType.MEETING_POINT)), Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))), getFullLookBehavior(), Pair.of(99, UpdateActivityFromSchedule.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getIdlePackage(VillagerProfession pProfession, float pSpeedModifier) {
      return ImmutableList.of(Pair.of(2, new RunOne<>(ImmutableList.of(Pair.of(InteractWith.of(EntityType.VILLAGER, 8, MemoryModuleType.INTERACTION_TARGET, pSpeedModifier, 2), 2), Pair.of(InteractWith.of(EntityType.VILLAGER, 8, AgeableMob::canBreed, AgeableMob::canBreed, MemoryModuleType.BREED_TARGET, pSpeedModifier, 2), 1), Pair.of(InteractWith.of(EntityType.CAT, 8, MemoryModuleType.INTERACTION_TARGET, pSpeedModifier, 2), 1), Pair.of(VillageBoundRandomStroll.create(pSpeedModifier), 1), Pair.of(SetWalkTargetFromLookTarget.create(pSpeedModifier, 2), 1), Pair.of(new JumpOnBed(pSpeedModifier), 1), Pair.of(new DoNothing(30, 60), 1)))), Pair.of(3, new GiveGiftToHero(100)), Pair.of(3, SetLookAndInteract.create(EntityType.PLAYER, 4)), Pair.of(3, new ShowTradesToPlayer(400, 1600)), Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.INTERACTION_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new TradeWithVillager(), 1)))), Pair.of(3, new GateBehavior<>(ImmutableMap.of(), ImmutableSet.of(MemoryModuleType.BREED_TARGET), GateBehavior.OrderPolicy.ORDERED, GateBehavior.RunningPolicy.RUN_ONE, ImmutableList.of(Pair.of(new VillagerMakeLove(), 1)))), getFullLookBehavior(), Pair.of(99, UpdateActivityFromSchedule.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPanicPackage(VillagerProfession pProfession, float pSpeedModifier) {
      float f = pSpeedModifier * 1.5F;
      return ImmutableList.of(Pair.of(0, VillagerCalmDown.create()), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_HOSTILE, f, 6, false)), Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.HURT_BY_ENTITY, f, 6, false)), Pair.of(3, VillageBoundRandomStroll.create(f, 2, 2)), getMinimalLookBehavior());
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getPreRaidPackage(VillagerProfession pProfession, float pSpeedModifier) {
      return ImmutableList.of(Pair.of(0, RingBell.create()), Pair.of(0, TriggerGate.triggerOneShuffled(ImmutableList.of(Pair.of(SetWalkTargetFromBlockMemory.create(MemoryModuleType.MEETING_POINT, pSpeedModifier * 1.5F, 2, 150, 200), 6), Pair.of(VillageBoundRandomStroll.create(pSpeedModifier * 1.5F), 2)))), getMinimalLookBehavior(), Pair.of(99, ResetRaidStatus.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getRaidPackage(VillagerProfession pProfession, float pSpeedModifier) {
      return ImmutableList.of(Pair.of(0, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(VillagerGoalPackages::raidExistsAndNotVictory), TriggerGate.triggerOneShuffled(ImmutableList.of(Pair.of(MoveToSkySeeingSpot.create(pSpeedModifier), 5), Pair.of(VillageBoundRandomStroll.create(pSpeedModifier * 1.1F), 2))))), Pair.of(0, new CelebrateVillagersSurvivedRaid(600, 600)), Pair.of(2, BehaviorBuilder.sequence(BehaviorBuilder.triggerIf(VillagerGoalPackages::raidExistsAndActive), LocateHidingPlace.create(24, pSpeedModifier * 1.4F, 1))), getMinimalLookBehavior(), Pair.of(99, ResetRaidStatus.create()));
   }

   public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> getHidePackage(VillagerProfession pProfession, float pSpeedModifier) {
      int i = 2;
      return ImmutableList.of(Pair.of(0, SetHiddenState.create(15, 3)), Pair.of(1, LocateHidingPlace.create(32, pSpeedModifier * 1.25F, 2)), getMinimalLookBehavior());
   }

   private static Pair<Integer, BehaviorControl<LivingEntity>> getFullLookBehavior() {
      return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.CAT, 8.0F), 8), Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(MobCategory.CREATURE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.WATER_CREATURE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.AXOLOTLS, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.UNDERGROUND_WATER_CREATURE, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.WATER_AMBIENT, 8.0F), 1), Pair.of(SetEntityLookTarget.create(MobCategory.MONSTER, 8.0F), 1), Pair.of(new DoNothing(30, 60), 2))));
   }

   private static Pair<Integer, BehaviorControl<LivingEntity>> getMinimalLookBehavior() {
      return Pair.of(5, new RunOne<>(ImmutableList.of(Pair.of(SetEntityLookTarget.create(EntityType.VILLAGER, 8.0F), 2), Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 8.0F), 2), Pair.of(new DoNothing(30, 60), 8))));
   }

   private static boolean raidExistsAndActive(ServerLevel p_260274_, LivingEntity p_260163_) {
      Raid raid = p_260274_.getRaidAt(p_260163_.blockPosition());
      return raid != null && raid.isActive() && !raid.isVictory() && !raid.isLoss();
   }

   private static boolean raidExistsAndNotVictory(ServerLevel p_259939_, LivingEntity p_259384_) {
      Raid raid = p_259939_.getRaidAt(p_259384_.blockPosition());
      return raid != null && raid.isVictory();
   }
}