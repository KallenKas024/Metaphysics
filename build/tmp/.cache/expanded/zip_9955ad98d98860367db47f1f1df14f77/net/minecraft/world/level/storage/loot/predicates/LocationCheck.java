package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * LootItemCondition that checks the {@link LootContextParams.ORIGIN} position against a {@link LocationPredicate} after
 * applying an offset to the origin position.
 */
public class LocationCheck implements LootItemCondition {
   final LocationPredicate predicate;
   final BlockPos offset;

   LocationCheck(LocationPredicate pLocationPredicate, BlockPos pOffset) {
      this.predicate = pLocationPredicate;
      this.offset = pOffset;
   }

   public LootItemConditionType getType() {
      return LootItemConditions.LOCATION_CHECK;
   }

   public boolean test(LootContext pContext) {
      Vec3 vec3 = pContext.getParamOrNull(LootContextParams.ORIGIN);
      return vec3 != null && this.predicate.matches(pContext.getLevel(), vec3.x() + (double)this.offset.getX(), vec3.y() + (double)this.offset.getY(), vec3.z() + (double)this.offset.getZ());
   }

   public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder pLocationPredicateBuilder) {
      return () -> {
         return new LocationCheck(pLocationPredicateBuilder.build(), BlockPos.ZERO);
      };
   }

   public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder pLocationPredicateBuilder, BlockPos pOffset) {
      return () -> {
         return new LocationCheck(pLocationPredicateBuilder.build(), pOffset);
      };
   }

   public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LocationCheck> {
      /**
       * Serialize the {@link CopyNbtFunction} by putting its data into the JsonObject.
       */
      public void serialize(JsonObject p_81749_, LocationCheck p_81750_, JsonSerializationContext p_81751_) {
         p_81749_.add("predicate", p_81750_.predicate.serializeToJson());
         if (p_81750_.offset.getX() != 0) {
            p_81749_.addProperty("offsetX", p_81750_.offset.getX());
         }

         if (p_81750_.offset.getY() != 0) {
            p_81749_.addProperty("offsetY", p_81750_.offset.getY());
         }

         if (p_81750_.offset.getZ() != 0) {
            p_81749_.addProperty("offsetZ", p_81750_.offset.getZ());
         }

      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public LocationCheck deserialize(JsonObject p_81757_, JsonDeserializationContext p_81758_) {
         LocationPredicate locationpredicate = LocationPredicate.fromJson(p_81757_.get("predicate"));
         int i = GsonHelper.getAsInt(p_81757_, "offsetX", 0);
         int j = GsonHelper.getAsInt(p_81757_, "offsetY", 0);
         int k = GsonHelper.getAsInt(p_81757_, "offsetZ", 0);
         return new LocationCheck(locationpredicate, new BlockPos(i, j, k));
      }
   }
}