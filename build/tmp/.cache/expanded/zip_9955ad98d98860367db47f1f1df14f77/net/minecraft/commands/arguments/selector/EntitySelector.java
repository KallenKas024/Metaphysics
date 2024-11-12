package net.minecraft.commands.arguments.selector;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelector {
   public static final int INFINITE = Integer.MAX_VALUE;
   public static final BiConsumer<Vec3, List<? extends Entity>> ORDER_ARBITRARY = (p_261404_, p_261405_) -> {
   };
   private static final EntityTypeTest<Entity, ?> ANY_TYPE = new EntityTypeTest<Entity, Entity>() {
      public Entity tryCast(Entity p_175109_) {
         return p_175109_;
      }

      public Class<? extends Entity> getBaseClass() {
         return Entity.class;
      }
   };
   private final int maxResults;
   private final boolean includesEntities;
   private final boolean worldLimited;
   private final Predicate<Entity> predicate;
   private final MinMaxBounds.Doubles range;
   private final Function<Vec3, Vec3> position;
   @Nullable
   private final AABB aabb;
   private final BiConsumer<Vec3, List<? extends Entity>> order;
   private final boolean currentEntity;
   @Nullable
   private final String playerName;
   @Nullable
   private final UUID entityUUID;
   private final EntityTypeTest<Entity, ?> type;
   private final boolean usesSelector;

   public EntitySelector(int pMaxResults, boolean pIncludeEntities, boolean pWorldLimited, Predicate<Entity> pPredicate, MinMaxBounds.Doubles pRange, Function<Vec3, Vec3> pPositions, @Nullable AABB pAabb, BiConsumer<Vec3, List<? extends Entity>> pOrder, boolean pCurrentEntity, @Nullable String pPlayerName, @Nullable UUID pEntityUUID, @Nullable EntityType<?> pType, boolean pUsesSelector) {
      this.maxResults = pMaxResults;
      this.includesEntities = pIncludeEntities;
      this.worldLimited = pWorldLimited;
      this.predicate = pPredicate;
      this.range = pRange;
      this.position = pPositions;
      this.aabb = pAabb;
      this.order = pOrder;
      this.currentEntity = pCurrentEntity;
      this.playerName = pPlayerName;
      this.entityUUID = pEntityUUID;
      this.type = (EntityTypeTest<Entity, ?>)(pType == null ? ANY_TYPE : pType);
      this.usesSelector = pUsesSelector;
   }

   public int getMaxResults() {
      return this.maxResults;
   }

   public boolean includesEntities() {
      return this.includesEntities;
   }

   public boolean isSelfSelector() {
      return this.currentEntity;
   }

   public boolean isWorldLimited() {
      return this.worldLimited;
   }

   public boolean usesSelector() {
      return this.usesSelector;
   }

   private void checkPermissions(CommandSourceStack pSource) throws CommandSyntaxException {
      if (this.usesSelector && !net.minecraftforge.common.ForgeHooks.canUseEntitySelectors(pSource)) {
         throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
      }
   }

   public Entity findSingleEntity(CommandSourceStack pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      List<? extends Entity> list = this.findEntities(pSource);
      if (list.isEmpty()) {
         throw EntityArgument.NO_ENTITIES_FOUND.create();
      } else if (list.size() > 1) {
         throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
      } else {
         return list.get(0);
      }
   }

   public List<? extends Entity> findEntities(CommandSourceStack pSource) throws CommandSyntaxException {
      return this.findEntitiesRaw(pSource).stream().filter((p_247981_) -> {
         return p_247981_.getType().isEnabled(pSource.enabledFeatures());
      }).toList();
   }

   private List<? extends Entity> findEntitiesRaw(CommandSourceStack pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      if (!this.includesEntities) {
         return this.findPlayers(pSource);
      } else if (this.playerName != null) {
         ServerPlayer serverplayer = pSource.getServer().getPlayerList().getPlayerByName(this.playerName);
         return (List<? extends Entity>)(serverplayer == null ? Collections.emptyList() : Lists.newArrayList(serverplayer));
      } else if (this.entityUUID != null) {
         for(ServerLevel serverlevel1 : pSource.getServer().getAllLevels()) {
            Entity entity = serverlevel1.getEntity(this.entityUUID);
            if (entity != null) {
               return Lists.newArrayList(entity);
            }
         }

         return Collections.emptyList();
      } else {
         Vec3 vec3 = this.position.apply(pSource.getPosition());
         Predicate<Entity> predicate = this.getPredicate(vec3);
         if (this.currentEntity) {
            return (List<? extends Entity>)(pSource.getEntity() != null && predicate.test(pSource.getEntity()) ? Lists.newArrayList(pSource.getEntity()) : Collections.emptyList());
         } else {
            List<Entity> list = Lists.newArrayList();
            if (this.isWorldLimited()) {
               this.addEntities(list, pSource.getLevel(), vec3, predicate);
            } else {
               for(ServerLevel serverlevel : pSource.getServer().getAllLevels()) {
                  this.addEntities(list, serverlevel, vec3, predicate);
               }
            }

            return this.sortAndLimit(vec3, list);
         }
      }
   }

   /**
    * Gets all entities matching this selector, and adds them to the passed list.
    */
   private void addEntities(List<Entity> pResult, ServerLevel pLevel, Vec3 pPos, Predicate<Entity> pPredicate) {
      int i = this.getResultLimit();
      if (pResult.size() < i) {
         if (this.aabb != null) {
            pLevel.getEntities(this.type, this.aabb.move(pPos), pPredicate, pResult, i);
         } else {
            pLevel.getEntities(this.type, pPredicate, pResult, i);
         }

      }
   }

   private int getResultLimit() {
      return this.order == ORDER_ARBITRARY ? this.maxResults : Integer.MAX_VALUE;
   }

   public ServerPlayer findSinglePlayer(CommandSourceStack pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      List<ServerPlayer> list = this.findPlayers(pSource);
      if (list.size() != 1) {
         throw EntityArgument.NO_PLAYERS_FOUND.create();
      } else {
         return list.get(0);
      }
   }

   public List<ServerPlayer> findPlayers(CommandSourceStack pSource) throws CommandSyntaxException {
      this.checkPermissions(pSource);
      if (this.playerName != null) {
         ServerPlayer serverplayer2 = pSource.getServer().getPlayerList().getPlayerByName(this.playerName);
         return (List<ServerPlayer>)(serverplayer2 == null ? Collections.emptyList() : Lists.newArrayList(serverplayer2));
      } else if (this.entityUUID != null) {
         ServerPlayer serverplayer1 = pSource.getServer().getPlayerList().getPlayer(this.entityUUID);
         return (List<ServerPlayer>)(serverplayer1 == null ? Collections.emptyList() : Lists.newArrayList(serverplayer1));
      } else {
         Vec3 vec3 = this.position.apply(pSource.getPosition());
         Predicate<Entity> predicate = this.getPredicate(vec3);
         if (this.currentEntity) {
            if (pSource.getEntity() instanceof ServerPlayer) {
               ServerPlayer serverplayer3 = (ServerPlayer)pSource.getEntity();
               if (predicate.test(serverplayer3)) {
                  return Lists.newArrayList(serverplayer3);
               }
            }

            return Collections.emptyList();
         } else {
            int i = this.getResultLimit();
            List<ServerPlayer> list;
            if (this.isWorldLimited()) {
               list = pSource.getLevel().getPlayers(predicate, i);
            } else {
               list = Lists.newArrayList();

               for(ServerPlayer serverplayer : pSource.getServer().getPlayerList().getPlayers()) {
                  if (predicate.test(serverplayer)) {
                     list.add(serverplayer);
                     if (list.size() >= i) {
                        return list;
                     }
                  }
               }
            }

            return this.sortAndLimit(vec3, list);
         }
      }
   }

   /**
    * Returns a modified version of the predicate on this selector that also checks the AABB and distance.
    */
   private Predicate<Entity> getPredicate(Vec3 pPos) {
      Predicate<Entity> predicate = this.predicate;
      if (this.aabb != null) {
         AABB aabb = this.aabb.move(pPos);
         predicate = predicate.and((p_121143_) -> {
            return aabb.intersects(p_121143_.getBoundingBox());
         });
      }

      if (!this.range.isAny()) {
         predicate = predicate.and((p_121148_) -> {
            return this.range.matchesSqr(p_121148_.distanceToSqr(pPos));
         });
      }

      return predicate;
   }

   private <T extends Entity> List<T> sortAndLimit(Vec3 pPos, List<T> pEntities) {
      if (pEntities.size() > 1) {
         this.order.accept(pPos, pEntities);
      }

      return pEntities.subList(0, Math.min(this.maxResults, pEntities.size()));
   }

   public static Component joinNames(List<? extends Entity> pNames) {
      return ComponentUtils.formatList(pNames, Entity::getDisplayName);
   }
}
