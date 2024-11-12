package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.Vec3;

public class FishingHookPredicate implements EntitySubPredicate {
   public static final FishingHookPredicate ANY = new FishingHookPredicate(false);
   private static final String IN_OPEN_WATER_KEY = "in_open_water";
   private final boolean inOpenWater;

   private FishingHookPredicate(boolean pInOpenWater) {
      this.inOpenWater = pInOpenWater;
   }

   public static FishingHookPredicate inOpenWater(boolean pInOpenWater) {
      return new FishingHookPredicate(pInOpenWater);
   }

   public static FishingHookPredicate fromJson(JsonObject pJson) {
      JsonElement jsonelement = pJson.get("in_open_water");
      return jsonelement != null ? new FishingHookPredicate(GsonHelper.convertToBoolean(jsonelement, "in_open_water")) : ANY;
   }

   public JsonObject serializeCustomData() {
      if (this == ANY) {
         return new JsonObject();
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("in_open_water", new JsonPrimitive(this.inOpenWater));
         return jsonobject;
      }
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.FISHING_HOOK;
   }

   public boolean matches(Entity pEntity, ServerLevel pLevel, @Nullable Vec3 pPosition) {
      if (this == ANY) {
         return true;
      } else if (!(pEntity instanceof FishingHook)) {
         return false;
      } else {
         FishingHook fishinghook = (FishingHook)pEntity;
         return this.inOpenWater == fishinghook.isOpenWaterFishing();
      }
   }
}