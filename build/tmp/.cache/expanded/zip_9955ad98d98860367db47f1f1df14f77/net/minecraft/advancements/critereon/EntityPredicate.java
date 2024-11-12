package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;

public class EntityPredicate {
   public static final EntityPredicate ANY = new EntityPredicate(EntityTypePredicate.ANY, DistancePredicate.ANY, LocationPredicate.ANY, LocationPredicate.ANY, MobEffectsPredicate.ANY, NbtPredicate.ANY, EntityFlagsPredicate.ANY, EntityEquipmentPredicate.ANY, EntitySubPredicate.ANY, (String)null);
   private final EntityTypePredicate entityType;
   private final DistancePredicate distanceToPlayer;
   private final LocationPredicate location;
   private final LocationPredicate steppingOnLocation;
   private final MobEffectsPredicate effects;
   private final NbtPredicate nbt;
   private final EntityFlagsPredicate flags;
   private final EntityEquipmentPredicate equipment;
   private final EntitySubPredicate subPredicate;
   private final EntityPredicate vehicle;
   private final EntityPredicate passenger;
   private final EntityPredicate targetedEntity;
   @Nullable
   private final String team;

   private EntityPredicate(EntityTypePredicate pEntityType, DistancePredicate pDistanceToPlayer, LocationPredicate pLocation, LocationPredicate pSteppingOnLocation, MobEffectsPredicate pEffects, NbtPredicate pNbt, EntityFlagsPredicate pFlags, EntityEquipmentPredicate pEquipment, EntitySubPredicate pSubPredicate, @Nullable String pTeam) {
      this.entityType = pEntityType;
      this.distanceToPlayer = pDistanceToPlayer;
      this.location = pLocation;
      this.steppingOnLocation = pSteppingOnLocation;
      this.effects = pEffects;
      this.nbt = pNbt;
      this.flags = pFlags;
      this.equipment = pEquipment;
      this.subPredicate = pSubPredicate;
      this.passenger = this;
      this.vehicle = this;
      this.targetedEntity = this;
      this.team = pTeam;
   }

   EntityPredicate(EntityTypePredicate pEntityType, DistancePredicate pDistanceToPlayer, LocationPredicate pLocation, LocationPredicate pSteppingOnLocation, MobEffectsPredicate pEffects, NbtPredicate pNbt, EntityFlagsPredicate pFlags, EntityEquipmentPredicate pEquipment, EntitySubPredicate pSubPredicate, EntityPredicate pVehicle, EntityPredicate pPassenger, EntityPredicate pTargetedEntity, @Nullable String pTeam) {
      this.entityType = pEntityType;
      this.distanceToPlayer = pDistanceToPlayer;
      this.location = pLocation;
      this.steppingOnLocation = pSteppingOnLocation;
      this.effects = pEffects;
      this.nbt = pNbt;
      this.flags = pFlags;
      this.equipment = pEquipment;
      this.subPredicate = pSubPredicate;
      this.vehicle = pVehicle;
      this.passenger = pPassenger;
      this.targetedEntity = pTargetedEntity;
      this.team = pTeam;
   }

   public static ContextAwarePredicate fromJson(JsonObject pJson, String pId, DeserializationContext pContext) {
      JsonElement jsonelement = pJson.get(pId);
      return fromElement(pId, pContext, jsonelement);
   }

   public static ContextAwarePredicate[] fromJsonArray(JsonObject pJson, String pId, DeserializationContext pContext) {
      JsonElement jsonelement = pJson.get(pId);
      if (jsonelement != null && !jsonelement.isJsonNull()) {
         JsonArray jsonarray = GsonHelper.convertToJsonArray(jsonelement, pId);
         ContextAwarePredicate[] acontextawarepredicate = new ContextAwarePredicate[jsonarray.size()];

         for(int i = 0; i < jsonarray.size(); ++i) {
            acontextawarepredicate[i] = fromElement(pId + "[" + i + "]", pContext, jsonarray.get(i));
         }

         return acontextawarepredicate;
      } else {
         return new ContextAwarePredicate[0];
      }
   }

   private static ContextAwarePredicate fromElement(String pId, DeserializationContext pContext, @Nullable JsonElement pElement) {
      ContextAwarePredicate contextawarepredicate = ContextAwarePredicate.fromElement(pId, pContext, pElement, LootContextParamSets.ADVANCEMENT_ENTITY);
      if (contextawarepredicate != null) {
         return contextawarepredicate;
      } else {
         EntityPredicate entitypredicate = fromJson(pElement);
         return wrap(entitypredicate);
      }
   }

   public static ContextAwarePredicate wrap(EntityPredicate pPredicate) {
      if (pPredicate == ANY) {
         return ContextAwarePredicate.ANY;
      } else {
         LootItemCondition lootitemcondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, pPredicate).build();
         return new ContextAwarePredicate(new LootItemCondition[]{lootitemcondition});
      }
   }

   public boolean matches(ServerPlayer pPlayer, @Nullable Entity pEntity) {
      return this.matches(pPlayer.serverLevel(), pPlayer.position(), pEntity);
   }

   public boolean matches(ServerLevel pLevel, @Nullable Vec3 pPosition, @Nullable Entity pEntity) {
      if (this == ANY) {
         return true;
      } else if (pEntity == null) {
         return false;
      } else if (!this.entityType.matches(pEntity.getType())) {
         return false;
      } else {
         if (pPosition == null) {
            if (this.distanceToPlayer != DistancePredicate.ANY) {
               return false;
            }
         } else if (!this.distanceToPlayer.matches(pPosition.x, pPosition.y, pPosition.z, pEntity.getX(), pEntity.getY(), pEntity.getZ())) {
            return false;
         }

         if (!this.location.matches(pLevel, pEntity.getX(), pEntity.getY(), pEntity.getZ())) {
            return false;
         } else {
            if (this.steppingOnLocation != LocationPredicate.ANY) {
               Vec3 vec3 = Vec3.atCenterOf(pEntity.getOnPos());
               if (!this.steppingOnLocation.matches(pLevel, vec3.x(), vec3.y(), vec3.z())) {
                  return false;
               }
            }

            if (!this.effects.matches(pEntity)) {
               return false;
            } else if (!this.nbt.matches(pEntity)) {
               return false;
            } else if (!this.flags.matches(pEntity)) {
               return false;
            } else if (!this.equipment.matches(pEntity)) {
               return false;
            } else if (!this.subPredicate.matches(pEntity, pLevel, pPosition)) {
               return false;
            } else if (!this.vehicle.matches(pLevel, pPosition, pEntity.getVehicle())) {
               return false;
            } else if (this.passenger != ANY && pEntity.getPassengers().stream().noneMatch((p_150322_) -> {
               return this.passenger.matches(pLevel, pPosition, p_150322_);
            })) {
               return false;
            } else if (!this.targetedEntity.matches(pLevel, pPosition, pEntity instanceof Mob ? ((Mob)pEntity).getTarget() : null)) {
               return false;
            } else {
               if (this.team != null) {
                  Team team = pEntity.getTeam();
                  if (team == null || !this.team.equals(team.getName())) {
                     return false;
                  }
               }

               return true;
            }
         }
      }
   }

   public static EntityPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "entity");
         EntityTypePredicate entitytypepredicate = EntityTypePredicate.fromJson(jsonobject.get("type"));
         DistancePredicate distancepredicate = DistancePredicate.fromJson(jsonobject.get("distance"));
         LocationPredicate locationpredicate = LocationPredicate.fromJson(jsonobject.get("location"));
         LocationPredicate locationpredicate1 = LocationPredicate.fromJson(jsonobject.get("stepping_on"));
         MobEffectsPredicate mobeffectspredicate = MobEffectsPredicate.fromJson(jsonobject.get("effects"));
         NbtPredicate nbtpredicate = NbtPredicate.fromJson(jsonobject.get("nbt"));
         EntityFlagsPredicate entityflagspredicate = EntityFlagsPredicate.fromJson(jsonobject.get("flags"));
         EntityEquipmentPredicate entityequipmentpredicate = EntityEquipmentPredicate.fromJson(jsonobject.get("equipment"));
         EntitySubPredicate entitysubpredicate = EntitySubPredicate.fromJson(jsonobject.get("type_specific"));
         EntityPredicate entitypredicate = fromJson(jsonobject.get("vehicle"));
         EntityPredicate entitypredicate1 = fromJson(jsonobject.get("passenger"));
         EntityPredicate entitypredicate2 = fromJson(jsonobject.get("targeted_entity"));
         String s = GsonHelper.getAsString(jsonobject, "team", (String)null);
         return (new EntityPredicate.Builder()).entityType(entitytypepredicate).distance(distancepredicate).located(locationpredicate).steppingOn(locationpredicate1).effects(mobeffectspredicate).nbt(nbtpredicate).flags(entityflagspredicate).equipment(entityequipmentpredicate).subPredicate(entitysubpredicate).team(s).vehicle(entitypredicate).passenger(entitypredicate1).targetedEntity(entitypredicate2).build();
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("type", this.entityType.serializeToJson());
         jsonobject.add("distance", this.distanceToPlayer.serializeToJson());
         jsonobject.add("location", this.location.serializeToJson());
         jsonobject.add("stepping_on", this.steppingOnLocation.serializeToJson());
         jsonobject.add("effects", this.effects.serializeToJson());
         jsonobject.add("nbt", this.nbt.serializeToJson());
         jsonobject.add("flags", this.flags.serializeToJson());
         jsonobject.add("equipment", this.equipment.serializeToJson());
         jsonobject.add("type_specific", this.subPredicate.serialize());
         jsonobject.add("vehicle", this.vehicle.serializeToJson());
         jsonobject.add("passenger", this.passenger.serializeToJson());
         jsonobject.add("targeted_entity", this.targetedEntity.serializeToJson());
         jsonobject.addProperty("team", this.team);
         return jsonobject;
      }
   }

   public static LootContext createContext(ServerPlayer pPlayer, Entity pEntity) {
      LootParams lootparams = (new LootParams.Builder(pPlayer.serverLevel())).withParameter(LootContextParams.THIS_ENTITY, pEntity).withParameter(LootContextParams.ORIGIN, pPlayer.position()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
      return (new LootContext.Builder(lootparams)).create((ResourceLocation)null);
   }

   public static class Builder {
      private EntityTypePredicate entityType = EntityTypePredicate.ANY;
      private DistancePredicate distanceToPlayer = DistancePredicate.ANY;
      private LocationPredicate location = LocationPredicate.ANY;
      private LocationPredicate steppingOnLocation = LocationPredicate.ANY;
      private MobEffectsPredicate effects = MobEffectsPredicate.ANY;
      private NbtPredicate nbt = NbtPredicate.ANY;
      private EntityFlagsPredicate flags = EntityFlagsPredicate.ANY;
      private EntityEquipmentPredicate equipment = EntityEquipmentPredicate.ANY;
      private EntitySubPredicate subPredicate = EntitySubPredicate.ANY;
      private EntityPredicate vehicle = EntityPredicate.ANY;
      private EntityPredicate passenger = EntityPredicate.ANY;
      private EntityPredicate targetedEntity = EntityPredicate.ANY;
      @Nullable
      private String team;

      public static EntityPredicate.Builder entity() {
         return new EntityPredicate.Builder();
      }

      public EntityPredicate.Builder of(EntityType<?> pEntityType) {
         this.entityType = EntityTypePredicate.of(pEntityType);
         return this;
      }

      public EntityPredicate.Builder of(TagKey<EntityType<?>> pEntityTypeTag) {
         this.entityType = EntityTypePredicate.of(pEntityTypeTag);
         return this;
      }

      public EntityPredicate.Builder entityType(EntityTypePredicate pEntityType) {
         this.entityType = pEntityType;
         return this;
      }

      public EntityPredicate.Builder distance(DistancePredicate pDistanceToPlayer) {
         this.distanceToPlayer = pDistanceToPlayer;
         return this;
      }

      public EntityPredicate.Builder located(LocationPredicate pLocation) {
         this.location = pLocation;
         return this;
      }

      public EntityPredicate.Builder steppingOn(LocationPredicate pSteppingOnLocation) {
         this.steppingOnLocation = pSteppingOnLocation;
         return this;
      }

      public EntityPredicate.Builder effects(MobEffectsPredicate pEffects) {
         this.effects = pEffects;
         return this;
      }

      public EntityPredicate.Builder nbt(NbtPredicate pNbt) {
         this.nbt = pNbt;
         return this;
      }

      public EntityPredicate.Builder flags(EntityFlagsPredicate pFlags) {
         this.flags = pFlags;
         return this;
      }

      public EntityPredicate.Builder equipment(EntityEquipmentPredicate pEquipment) {
         this.equipment = pEquipment;
         return this;
      }

      public EntityPredicate.Builder subPredicate(EntitySubPredicate pSubPredicate) {
         this.subPredicate = pSubPredicate;
         return this;
      }

      public EntityPredicate.Builder vehicle(EntityPredicate pVehicle) {
         this.vehicle = pVehicle;
         return this;
      }

      public EntityPredicate.Builder passenger(EntityPredicate pPassenger) {
         this.passenger = pPassenger;
         return this;
      }

      public EntityPredicate.Builder targetedEntity(EntityPredicate pTargetedEntity) {
         this.targetedEntity = pTargetedEntity;
         return this;
      }

      public EntityPredicate.Builder team(@Nullable String pTeam) {
         this.team = pTeam;
         return this;
      }

      public EntityPredicate build() {
         return new EntityPredicate(this.entityType, this.distanceToPlayer, this.location, this.steppingOnLocation, this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.vehicle, this.passenger, this.targetedEntity, this.team);
      }
   }
}