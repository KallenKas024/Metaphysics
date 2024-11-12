package net.minecraft.world.entity.ai.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class BehaviorUtils {
   private BehaviorUtils() {
   }

   public static void lockGazeAndWalkToEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity, float pSpeed) {
      lookAtEachOther(pFirstEntity, pSecondEntity);
      setWalkAndLookTargetMemoriesToEachOther(pFirstEntity, pSecondEntity, pSpeed);
   }

   public static boolean entityIsVisible(Brain<?> pBrain, LivingEntity pTarget) {
      Optional<NearestVisibleLivingEntities> optional = pBrain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
      return optional.isPresent() && optional.get().contains(pTarget);
   }

   public static boolean targetIsValid(Brain<?> pBrains, MemoryModuleType<? extends LivingEntity> pMemorymodule, EntityType<?> pEntityType) {
      return targetIsValid(pBrains, pMemorymodule, (p_289317_) -> {
         return p_289317_.getType() == pEntityType;
      });
   }

   private static boolean targetIsValid(Brain<?> pBrain, MemoryModuleType<? extends LivingEntity> pMemoryType, Predicate<LivingEntity> pLivingPredicate) {
      return pBrain.getMemory(pMemoryType).filter(pLivingPredicate).filter(LivingEntity::isAlive).filter((p_186037_) -> {
         return entityIsVisible(pBrain, p_186037_);
      }).isPresent();
   }

   private static void lookAtEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity) {
      lookAtEntity(pFirstEntity, pSecondEntity);
      lookAtEntity(pSecondEntity, pFirstEntity);
   }

   public static void lookAtEntity(LivingEntity pEntity, LivingEntity pTarget) {
      pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(pTarget, true));
   }

   private static void setWalkAndLookTargetMemoriesToEachOther(LivingEntity pFirstEntity, LivingEntity pSecondEntity, float pSpeed) {
      int i = 2;
      setWalkAndLookTargetMemories(pFirstEntity, pSecondEntity, pSpeed, 2);
      setWalkAndLookTargetMemories(pSecondEntity, pFirstEntity, pSpeed, 2);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, Entity pTarget, float pSpeed, int pDistance) {
      setWalkAndLookTargetMemories(pLivingEntity, new EntityTracker(pTarget, true), pSpeed, pDistance);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity pLivingEntity, BlockPos pPos, float pSpeed, int pDistance) {
      setWalkAndLookTargetMemories(pLivingEntity, new BlockPosTracker(pPos), pSpeed, pDistance);
   }

   public static void setWalkAndLookTargetMemories(LivingEntity pEntity, PositionTracker pPositionTracker, float pSpeedModifier, int pCloseEnoughDist) {
      WalkTarget walktarget = new WalkTarget(pPositionTracker, pSpeedModifier, pCloseEnoughDist);
      pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, pPositionTracker);
      pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, walktarget);
   }

   public static void throwItem(LivingEntity pLivingEntity, ItemStack pStack, Vec3 pOffset) {
      Vec3 vec3 = new Vec3((double)0.3F, (double)0.3F, (double)0.3F);
      throwItem(pLivingEntity, pStack, pOffset, vec3, 0.3F);
   }

   public static void throwItem(LivingEntity pEntity, ItemStack pStack, Vec3 pOffset, Vec3 pSpeedMultiplier, float pYOffset) {
      double d0 = pEntity.getEyeY() - (double)pYOffset;
      ItemEntity itementity = new ItemEntity(pEntity.level(), pEntity.getX(), d0, pEntity.getZ(), pStack);
      itementity.setThrower(pEntity.getUUID());
      Vec3 vec3 = pOffset.subtract(pEntity.position());
      vec3 = vec3.normalize().multiply(pSpeedMultiplier.x, pSpeedMultiplier.y, pSpeedMultiplier.z);
      itementity.setDeltaMovement(vec3);
      itementity.setDefaultPickUpDelay();
      pEntity.level().addFreshEntity(itementity);
   }

   public static SectionPos findSectionClosestToVillage(ServerLevel pServerLevel, SectionPos pSectionPos, int pRadius) {
      int i = pServerLevel.sectionsToVillage(pSectionPos);
      return SectionPos.cube(pSectionPos, pRadius).filter((p_186017_) -> {
         return pServerLevel.sectionsToVillage(p_186017_) < i;
      }).min(Comparator.comparingInt(pServerLevel::sectionsToVillage)).orElse(pSectionPos);
   }

   public static boolean isWithinAttackRange(Mob pMob, LivingEntity pTarget, int pCooldown) {
      Item $$4 = pMob.getMainHandItem().getItem();
      if ($$4 instanceof ProjectileWeaponItem projectileweaponitem) {
         if (pMob.canFireProjectileWeapon(projectileweaponitem)) {
            int i = projectileweaponitem.getDefaultProjectileRange() - pCooldown;
            return pMob.closerThan(pTarget, (double)i);
         }
      }

      return pMob.isWithinMeleeAttackRange(pTarget);
   }

   public static boolean isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(LivingEntity pLivingEntity, LivingEntity pTarget, double pDistance) {
      Optional<LivingEntity> optional = pLivingEntity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
      if (optional.isEmpty()) {
         return false;
      } else {
         double d0 = pLivingEntity.distanceToSqr(optional.get().position());
         double d1 = pLivingEntity.distanceToSqr(pTarget.position());
         return d1 > d0 + pDistance * pDistance;
      }
   }

   public static boolean canSee(LivingEntity pLivingEntity, LivingEntity pTarget) {
      Brain<?> brain = pLivingEntity.getBrain();
      return !brain.hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES) ? false : brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get().contains(pTarget);
   }

   public static LivingEntity getNearestTarget(LivingEntity pCenterEntity, Optional<LivingEntity> pOptionalEntity, LivingEntity pLivingEntity) {
      return pOptionalEntity.isEmpty() ? pLivingEntity : getTargetNearestMe(pCenterEntity, pOptionalEntity.get(), pLivingEntity);
   }

   public static LivingEntity getTargetNearestMe(LivingEntity pCenterEntity, LivingEntity pLivingEntity1, LivingEntity pLivingEntity2) {
      Vec3 vec3 = pLivingEntity1.position();
      Vec3 vec31 = pLivingEntity2.position();
      return pCenterEntity.distanceToSqr(vec3) < pCenterEntity.distanceToSqr(vec31) ? pLivingEntity1 : pLivingEntity2;
   }

   public static Optional<LivingEntity> getLivingEntityFromUUIDMemory(LivingEntity pLivingEntity, MemoryModuleType<UUID> pTargetMemory) {
      Optional<UUID> optional = pLivingEntity.getBrain().getMemory(pTargetMemory);
      return optional.map((p_289315_) -> {
         return ((ServerLevel)pLivingEntity.level()).getEntity(p_289315_);
      }).map((p_186019_) -> {
         LivingEntity livingentity1;
         if (p_186019_ instanceof LivingEntity livingentity) {
            livingentity1 = livingentity;
         } else {
            livingentity1 = null;
         }

         return livingentity1;
      });
   }

   @Nullable
   public static Vec3 getRandomSwimmablePos(PathfinderMob pPathfinder, int pRadius, int pVerticalDistance) {
      Vec3 vec3 = DefaultRandomPos.getPos(pPathfinder, pRadius, pVerticalDistance);

      for(int i = 0; vec3 != null && !pPathfinder.level().getBlockState(BlockPos.containing(vec3)).isPathfindable(pPathfinder.level(), BlockPos.containing(vec3), PathComputationType.WATER) && i++ < 10; vec3 = DefaultRandomPos.getPos(pPathfinder, pRadius, pVerticalDistance)) {
      }

      return vec3;
   }

   public static boolean isBreeding(LivingEntity pEntity) {
      return pEntity.getBrain().hasMemoryValue(MemoryModuleType.BREED_TARGET);
   }
}