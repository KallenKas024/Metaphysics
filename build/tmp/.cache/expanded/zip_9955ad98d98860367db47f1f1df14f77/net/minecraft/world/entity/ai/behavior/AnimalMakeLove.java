package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.Animal;

public class AnimalMakeLove extends Behavior<Animal> {
   private static final int BREED_RANGE = 3;
   private static final int MIN_DURATION = 60;
   private static final int MAX_DURATION = 110;
   private final EntityType<? extends Animal> partnerType;
   private final float speedModifier;
   private long spawnChildAtTime;

   public AnimalMakeLove(EntityType<? extends Animal> pPartnerType, float pSpeedModifier) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT, MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED), 110);
      this.partnerType = pPartnerType;
      this.speedModifier = pSpeedModifier;
   }

   protected boolean checkExtraStartConditions(ServerLevel pLevel, Animal pOwner) {
      return pOwner.isInLove() && this.findValidBreedPartner(pOwner).isPresent();
   }

   protected void start(ServerLevel pLevel, Animal pEntity, long pGameTime) {
      Animal animal = this.findValidBreedPartner(pEntity).get();
      pEntity.getBrain().setMemory(MemoryModuleType.BREED_TARGET, animal);
      animal.getBrain().setMemory(MemoryModuleType.BREED_TARGET, pEntity);
      BehaviorUtils.lockGazeAndWalkToEachOther(pEntity, animal, this.speedModifier);
      int i = 60 + pEntity.getRandom().nextInt(50);
      this.spawnChildAtTime = pGameTime + (long)i;
   }

   protected boolean canStillUse(ServerLevel pLevel, Animal pEntity, long pGameTime) {
      if (!this.hasBreedTargetOfRightType(pEntity)) {
         return false;
      } else {
         Animal animal = this.getBreedTarget(pEntity);
         return animal.isAlive() && pEntity.canMate(animal) && BehaviorUtils.entityIsVisible(pEntity.getBrain(), animal) && pGameTime <= this.spawnChildAtTime;
      }
   }

   protected void tick(ServerLevel pLevel, Animal pOwner, long pGameTime) {
      Animal animal = this.getBreedTarget(pOwner);
      BehaviorUtils.lockGazeAndWalkToEachOther(pOwner, animal, this.speedModifier);
      if (pOwner.closerThan(animal, 3.0D)) {
         if (pGameTime >= this.spawnChildAtTime) {
            pOwner.spawnChildFromBreeding(pLevel, animal);
            pOwner.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
            animal.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
         }

      }
   }

   protected void stop(ServerLevel pLevel, Animal pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.BREED_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      this.spawnChildAtTime = 0L;
   }

   private Animal getBreedTarget(Animal pAnimal) {
      return (Animal)pAnimal.getBrain().getMemory(MemoryModuleType.BREED_TARGET).get();
   }

   private boolean hasBreedTargetOfRightType(Animal pAnimal) {
      Brain<?> brain = pAnimal.getBrain();
      return brain.hasMemoryValue(MemoryModuleType.BREED_TARGET) && brain.getMemory(MemoryModuleType.BREED_TARGET).get().getType() == this.partnerType;
   }

   private Optional<? extends Animal> findValidBreedPartner(Animal pAnimal) {
      return pAnimal.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().findClosest((p_289312_) -> {
         if (p_289312_.getType() == this.partnerType && p_289312_ instanceof Animal animal) {
            if (pAnimal.canMate(animal)) {
               return true;
            }
         }

         return false;
      }).map(Animal.class::cast);
   }
}