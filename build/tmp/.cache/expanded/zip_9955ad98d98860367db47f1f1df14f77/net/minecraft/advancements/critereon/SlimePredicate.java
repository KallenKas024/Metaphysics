package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.phys.Vec3;

public class SlimePredicate implements EntitySubPredicate {
   private final MinMaxBounds.Ints size;

   private SlimePredicate(MinMaxBounds.Ints pSize) {
      this.size = pSize;
   }

   public static SlimePredicate sized(MinMaxBounds.Ints pSize) {
      return new SlimePredicate(pSize);
   }

   public static SlimePredicate fromJson(JsonObject pJson) {
      MinMaxBounds.Ints minmaxbounds$ints = MinMaxBounds.Ints.fromJson(pJson.get("size"));
      return new SlimePredicate(minmaxbounds$ints);
   }

   public JsonObject serializeCustomData() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("size", this.size.serializeToJson());
      return jsonobject;
   }

   public boolean matches(Entity pEntity, ServerLevel pLevel, @Nullable Vec3 pPosition) {
      if (pEntity instanceof Slime slime) {
         return this.size.matches(slime.getSize());
      } else {
         return false;
      }
   }

   public EntitySubPredicate.Type type() {
      return EntitySubPredicate.Types.SLIME;
   }
}