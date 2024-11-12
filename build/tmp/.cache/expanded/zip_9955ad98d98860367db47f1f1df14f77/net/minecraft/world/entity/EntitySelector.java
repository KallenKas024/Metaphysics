package net.minecraft.world.entity;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

public final class EntitySelector {
   /** Selects only entities which are alive */
   public static final Predicate<Entity> ENTITY_STILL_ALIVE = Entity::isAlive;
   /** Selects only entities which are LivingEntities and alive */
   public static final Predicate<Entity> LIVING_ENTITY_STILL_ALIVE = (p_20442_) -> {
      return p_20442_.isAlive() && p_20442_ instanceof LivingEntity;
   };
   /** Selects only entities which are neither ridden by anything nor ride on anything */
   public static final Predicate<Entity> ENTITY_NOT_BEING_RIDDEN = (p_20440_) -> {
      return p_20440_.isAlive() && !p_20440_.isVehicle() && !p_20440_.isPassenger();
   };
   /** Selects only entities which are container entities */
   public static final Predicate<Entity> CONTAINER_ENTITY_SELECTOR = (p_20438_) -> {
      return p_20438_ instanceof Container && p_20438_.isAlive();
   };
   /** Selects entities which are neither creative-mode players nor spectator-players */
   public static final Predicate<Entity> NO_CREATIVE_OR_SPECTATOR = (p_20436_) -> {
      return !(p_20436_ instanceof Player) || !p_20436_.isSpectator() && !((Player)p_20436_).isCreative();
   };
   /** Selects entities which are either not players or players that are not spectating */
   public static final Predicate<Entity> NO_SPECTATORS = (p_20434_) -> {
      return !p_20434_.isSpectator();
   };
   /** Selects entities which are collidable with and aren't spectators */
   public static final Predicate<Entity> CAN_BE_COLLIDED_WITH = NO_SPECTATORS.and(Entity::canBeCollidedWith);

   private EntitySelector() {
   }

   public static Predicate<Entity> withinDistance(double pX, double pY, double pZ, double pRange) {
      double d0 = pRange * pRange;
      return (p_20420_) -> {
         return p_20420_ != null && p_20420_.distanceToSqr(pX, pY, pZ) <= d0;
      };
   }

   public static Predicate<Entity> pushableBy(Entity pEntity) {
      Team team = pEntity.getTeam();
      Team.CollisionRule team$collisionrule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
      return (Predicate<Entity>)(team$collisionrule == Team.CollisionRule.NEVER ? Predicates.alwaysFalse() : NO_SPECTATORS.and((p_20430_) -> {
         if (!p_20430_.isPushable()) {
            return false;
         } else if (!pEntity.level().isClientSide || p_20430_ instanceof Player && ((Player)p_20430_).isLocalPlayer()) {
            Team team1 = p_20430_.getTeam();
            Team.CollisionRule team$collisionrule1 = team1 == null ? Team.CollisionRule.ALWAYS : team1.getCollisionRule();
            if (team$collisionrule1 == Team.CollisionRule.NEVER) {
               return false;
            } else {
               boolean flag = team != null && team.isAlliedTo(team1);
               if ((team$collisionrule == Team.CollisionRule.PUSH_OWN_TEAM || team$collisionrule1 == Team.CollisionRule.PUSH_OWN_TEAM) && flag) {
                  return false;
               } else {
                  return team$collisionrule != Team.CollisionRule.PUSH_OTHER_TEAMS && team$collisionrule1 != Team.CollisionRule.PUSH_OTHER_TEAMS || flag;
               }
            }
         } else {
            return false;
         }
      }));
   }

   public static Predicate<Entity> notRiding(Entity pEntity) {
      return (p_20425_) -> {
         while(true) {
            if (p_20425_.isPassenger()) {
               p_20425_ = p_20425_.getVehicle();
               if (p_20425_ != pEntity) {
                  continue;
               }

               return false;
            }

            return true;
         }
      };
   }

   public static class MobCanWearArmorEntitySelector implements Predicate<Entity> {
      private final ItemStack itemStack;

      public MobCanWearArmorEntitySelector(ItemStack pStack) {
         this.itemStack = pStack;
      }

      public boolean test(@Nullable Entity pEntity) {
         if (!pEntity.isAlive()) {
            return false;
         } else if (!(pEntity instanceof LivingEntity)) {
            return false;
         } else {
            LivingEntity livingentity = (LivingEntity)pEntity;
            return livingentity.canTakeItem(this.itemStack);
         }
      }
   }
}