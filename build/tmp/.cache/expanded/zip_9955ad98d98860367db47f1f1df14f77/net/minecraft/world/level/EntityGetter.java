package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter {
   /**
    * Gets all entities within the specified AABB excluding the one passed into it.
    */
   List<Entity> getEntities(@Nullable Entity pEntity, AABB pArea, Predicate<? super Entity> pPredicate);

   <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntityTypeTest, AABB pBounds, Predicate<? super T> pPredicate);

   default <T extends Entity> List<T> getEntitiesOfClass(Class<T> pClazz, AABB pArea, Predicate<? super T> pFilter) {
      return this.getEntities(EntityTypeTest.forClass(pClazz), pArea, pFilter);
   }

   List<? extends Player> players();

   /**
    * Will get all entities within the specified AABB excluding the one passed into it. Args: entityToExclude, aabb
    */
   default List<Entity> getEntities(@Nullable Entity pEntity, AABB pArea) {
      return this.getEntities(pEntity, pArea, EntitySelector.NO_SPECTATORS);
   }

   default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
      if (pShape.isEmpty()) {
         return true;
      } else {
         for(Entity entity : this.getEntities(pEntity, pShape.bounds())) {
            if (!entity.isRemoved() && entity.blocksBuilding && (pEntity == null || !entity.isPassengerOfSameVehicle(pEntity)) && Shapes.joinIsNotEmpty(pShape, Shapes.create(entity.getBoundingBox()), BooleanOp.AND)) {
               return false;
            }
         }

         return true;
      }
   }

   default <T extends Entity> List<T> getEntitiesOfClass(Class<T> pEntityClass, AABB pArea) {
      return this.getEntitiesOfClass(pEntityClass, pArea, EntitySelector.NO_SPECTATORS);
   }

   default List<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AABB pCollisionBox) {
      if (pCollisionBox.getSize() < 1.0E-7D) {
         return List.of();
      } else {
         Predicate<Entity> predicate = pEntity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(pEntity::canCollideWith);
         List<Entity> list = this.getEntities(pEntity, pCollisionBox.inflate(1.0E-7D), predicate);
         if (list.isEmpty()) {
            return List.of();
         } else {
            ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(list.size());

            for(Entity entity : list) {
               builder.add(Shapes.create(entity.getBoundingBox()));
            }

            return builder.build();
         }
      }
   }

   @Nullable
   default Player getNearestPlayer(double pX, double pY, double pZ, double pDistance, @Nullable Predicate<Entity> pPredicate) {
      double d0 = -1.0D;
      Player player = null;

      for(Player player1 : this.players()) {
         if (pPredicate == null || pPredicate.test(player1)) {
            double d1 = player1.distanceToSqr(pX, pY, pZ);
            if ((pDistance < 0.0D || d1 < pDistance * pDistance) && (d0 == -1.0D || d1 < d0)) {
               d0 = d1;
               player = player1;
            }
         }
      }

      return player;
   }

   @Nullable
   default Player getNearestPlayer(Entity pEntity, double pDistance) {
      return this.getNearestPlayer(pEntity.getX(), pEntity.getY(), pEntity.getZ(), pDistance, false);
   }

   @Nullable
   default Player getNearestPlayer(double pX, double pY, double pZ, double pDistance, boolean pCreativePlayers) {
      Predicate<Entity> predicate = pCreativePlayers ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
      return this.getNearestPlayer(pX, pY, pZ, pDistance, predicate);
   }

   default boolean hasNearbyAlivePlayer(double pX, double pY, double pZ, double pDistance) {
      for(Player player : this.players()) {
         if (EntitySelector.NO_SPECTATORS.test(player) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player)) {
            double d0 = player.distanceToSqr(pX, pY, pZ);
            if (pDistance < 0.0D || d0 < pDistance * pDistance) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   default Player getNearestPlayer(TargetingConditions pPredicate, LivingEntity pTarget) {
      return this.getNearestEntity(this.players(), pPredicate, pTarget, pTarget.getX(), pTarget.getY(), pTarget.getZ());
   }

   @Nullable
   default Player getNearestPlayer(TargetingConditions pPredicate, LivingEntity pTarget, double pX, double pY, double pZ) {
      return this.getNearestEntity(this.players(), pPredicate, pTarget, pX, pY, pZ);
   }

   @Nullable
   default Player getNearestPlayer(TargetingConditions pPredicate, double pX, double pY, double pZ) {
      return this.getNearestEntity(this.players(), pPredicate, (LivingEntity)null, pX, pY, pZ);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestEntity(Class<? extends T> pEntityClazz, TargetingConditions pConditions, @Nullable LivingEntity pTarget, double pX, double pY, double pZ, AABB pBoundingBox) {
      return this.getNearestEntity(this.getEntitiesOfClass(pEntityClazz, pBoundingBox, (p_186454_) -> {
         return true;
      }), pConditions, pTarget, pX, pY, pZ);
   }

   @Nullable
   default <T extends LivingEntity> T getNearestEntity(List<? extends T> pEntities, TargetingConditions pPredicate, @Nullable LivingEntity pTarget, double pX, double pY, double pZ) {
      double d0 = -1.0D;
      T t = null;

      for(T t1 : pEntities) {
         if (pPredicate.test(pTarget, t1)) {
            double d1 = t1.distanceToSqr(pX, pY, pZ);
            if (d0 == -1.0D || d1 < d0) {
               d0 = d1;
               t = t1;
            }
         }
      }

      return t;
   }

   default List<Player> getNearbyPlayers(TargetingConditions pPredicate, LivingEntity pTarget, AABB pArea) {
      List<Player> list = Lists.newArrayList();

      for(Player player : this.players()) {
         if (pArea.contains(player.getX(), player.getY(), player.getZ()) && pPredicate.test(pTarget, player)) {
            list.add(player);
         }
      }

      return list;
   }

   default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> pEntityClazz, TargetingConditions pEntityPredicate, LivingEntity pEntity, AABB pArea) {
      List<T> list = this.getEntitiesOfClass(pEntityClazz, pArea, (p_186450_) -> {
         return true;
      });
      List<T> list1 = Lists.newArrayList();

      for(T t : list) {
         if (pEntityPredicate.test(pEntity, t)) {
            list1.add(t);
         }
      }

      return list1;
   }

   @Nullable
   default Player getPlayerByUUID(UUID pUniqueId) {
      for(int i = 0; i < this.players().size(); ++i) {
         Player player = this.players().get(i);
         if (pUniqueId.equals(player.getUUID())) {
            return player;
         }
      }

      return null;
   }
}