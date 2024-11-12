package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

public class DistancePredicate {
   public static final DistancePredicate ANY = new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
   private final MinMaxBounds.Doubles x;
   private final MinMaxBounds.Doubles y;
   private final MinMaxBounds.Doubles z;
   private final MinMaxBounds.Doubles horizontal;
   private final MinMaxBounds.Doubles absolute;

   public DistancePredicate(MinMaxBounds.Doubles pX, MinMaxBounds.Doubles pY, MinMaxBounds.Doubles pZ, MinMaxBounds.Doubles pHorizontal, MinMaxBounds.Doubles pAbsolute) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.horizontal = pHorizontal;
      this.absolute = pAbsolute;
   }

   public static DistancePredicate horizontal(MinMaxBounds.Doubles pHorizontal) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, pHorizontal, MinMaxBounds.Doubles.ANY);
   }

   public static DistancePredicate vertical(MinMaxBounds.Doubles pVertical) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, pVertical, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
   }

   public static DistancePredicate absolute(MinMaxBounds.Doubles pAbsolute) {
      return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, pAbsolute);
   }

   public boolean matches(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      float f = (float)(pX1 - pX2);
      float f1 = (float)(pY1 - pY2);
      float f2 = (float)(pZ1 - pZ2);
      if (this.x.matches((double)Mth.abs(f)) && this.y.matches((double)Mth.abs(f1)) && this.z.matches((double)Mth.abs(f2))) {
         if (!this.horizontal.matchesSqr((double)(f * f + f2 * f2))) {
            return false;
         } else {
            return this.absolute.matchesSqr((double)(f * f + f1 * f1 + f2 * f2));
         }
      } else {
         return false;
      }
   }

   public static DistancePredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "distance");
         MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(jsonobject.get("x"));
         MinMaxBounds.Doubles minmaxbounds$doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject.get("y"));
         MinMaxBounds.Doubles minmaxbounds$doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject.get("z"));
         MinMaxBounds.Doubles minmaxbounds$doubles3 = MinMaxBounds.Doubles.fromJson(jsonobject.get("horizontal"));
         MinMaxBounds.Doubles minmaxbounds$doubles4 = MinMaxBounds.Doubles.fromJson(jsonobject.get("absolute"));
         return new DistancePredicate(minmaxbounds$doubles, minmaxbounds$doubles1, minmaxbounds$doubles2, minmaxbounds$doubles3, minmaxbounds$doubles4);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("x", this.x.serializeToJson());
         jsonobject.add("y", this.y.serializeToJson());
         jsonobject.add("z", this.z.serializeToJson());
         jsonobject.add("horizontal", this.horizontal.serializeToJson());
         jsonobject.add("absolute", this.absolute.serializeToJson());
         return jsonobject;
      }
   }
}