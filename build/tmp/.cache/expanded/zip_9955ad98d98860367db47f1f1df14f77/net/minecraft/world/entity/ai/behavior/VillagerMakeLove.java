package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.pathfinder.Path;

public class VillagerMakeLove extends Behavior<Villager> {
   private static final int INTERACT_DIST_SQR = 5;
   private static final float SPEED_MODIFIER = 0.5F;
   private long birthTimestamp;

   public VillagerMakeLove() {
      super(ImmutableMap.of(MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT), 350, 350);
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
      return this.isBreedingPossible(pOwner);
   }

   protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      return pGameTime <= this.birthTimestamp && this.isBreedingPossible(pEntity);
   }

   protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      AgeableMob ageablemob = pEntity.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      BehaviorUtils.lockGazeAndWalkToEachOther(pEntity, ageablemob, 0.5F);
      pLevel.broadcastEntityEvent(ageablemob, (byte)18);
      pLevel.broadcastEntityEvent(pEntity, (byte)18);
      int i = 275 + pEntity.getRandom().nextInt(50);
      this.birthTimestamp = pGameTime + (long)i;
   }

   protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
      Villager villager = (Villager)pOwner.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
      if (!(pOwner.distanceToSqr(villager) > 5.0D)) {
         BehaviorUtils.lockGazeAndWalkToEachOther(pOwner, villager, 0.5F);
         if (pGameTime >= this.birthTimestamp) {
            pOwner.eatAndDigestFood();
            villager.eatAndDigestFood();
            this.tryToGiveBirth(pLevel, pOwner, villager);
         } else if (pOwner.getRandom().nextInt(35) == 0) {
            pLevel.broadcastEntityEvent(villager, (byte)12);
            pLevel.broadcastEntityEvent(pOwner, (byte)12);
         }

      }
   }

   private void tryToGiveBirth(ServerLevel pLevel, Villager pParent, Villager pPartner) {
      Optional<BlockPos> optional = this.takeVacantBed(pLevel, pParent);
      if (!optional.isPresent()) {
         pLevel.broadcastEntityEvent(pPartner, (byte)13);
         pLevel.broadcastEntityEvent(pParent, (byte)13);
      } else {
         Optional<Villager> optional1 = this.breed(pLevel, pParent, pPartner);
         if (optional1.isPresent()) {
            this.giveBedToChild(pLevel, optional1.get(), optional.get());
         } else {
            pLevel.getPoiManager().release(optional.get());
            DebugPackets.sendPoiTicketCountPacket(pLevel, optional.get());
         }
      }

   }

   protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
   }

   private boolean isBreedingPossible(Villager pVillager) {
      Brain<Villager> brain = pVillager.getBrain();
      Optional<AgeableMob> optional = brain.getMemory(MemoryModuleType.BREED_TARGET).filter((p_289389_) -> {
         return p_289389_.getType() == EntityType.VILLAGER;
      });
      if (!optional.isPresent()) {
         return false;
      } else {
         return BehaviorUtils.targetIsValid(brain, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && pVillager.canBreed() && optional.get().canBreed();
      }
   }

   private Optional<BlockPos> takeVacantBed(ServerLevel pLevel, Villager pVillager) {
      return pLevel.getPoiManager().take((p_217509_) -> {
         return p_217509_.is(PoiTypes.HOME);
      }, (p_217506_, p_217507_) -> {
         return this.canReach(pVillager, p_217507_, p_217506_);
      }, pVillager.blockPosition(), 48);
   }

   private boolean canReach(Villager pVillager, BlockPos pPos, Holder<PoiType> pPoiType) {
      Path path = pVillager.getNavigation().createPath(pPos, pPoiType.value().validRange());
      return path != null && path.canReach();
   }

   private Optional<Villager> breed(ServerLevel pLevel, Villager pParent, Villager pPartner) {
      Villager villager = pParent.getBreedOffspring(pLevel, pPartner);
      if (villager == null) {
         return Optional.empty();
      } else {
         pParent.setAge(6000);
         pPartner.setAge(6000);
         villager.setAge(-24000);
         villager.moveTo(pParent.getX(), pParent.getY(), pParent.getZ(), 0.0F, 0.0F);
         pLevel.addFreshEntityWithPassengers(villager);
         pLevel.broadcastEntityEvent(villager, (byte)12);
         return Optional.of(villager);
      }
   }

   private void giveBedToChild(ServerLevel pLevel, Villager pVillager, BlockPos pPos) {
      GlobalPos globalpos = GlobalPos.of(pLevel.dimension(), pPos);
      pVillager.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
   }
}