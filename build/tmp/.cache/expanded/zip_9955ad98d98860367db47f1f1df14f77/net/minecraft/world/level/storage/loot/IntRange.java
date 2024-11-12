package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * A possibly unbounded range of integers based on {@link LootContext}. Minimum and maximum are given in the form of
 * {@link NumberProvider}s.
 * Minimum and maximum are both optional. If given, they are both inclusive.
 */
public class IntRange {
   @Nullable
   final NumberProvider min;
   @Nullable
   final NumberProvider max;
   private final IntRange.IntLimiter limiter;
   private final IntRange.IntChecker predicate;

   /**
    * The LootContextParams required for this IntRange.
    */
   public Set<LootContextParam<?>> getReferencedContextParams() {
      ImmutableSet.Builder<LootContextParam<?>> builder = ImmutableSet.builder();
      if (this.min != null) {
         builder.addAll(this.min.getReferencedContextParams());
      }

      if (this.max != null) {
         builder.addAll(this.max.getReferencedContextParams());
      }

      return builder.build();
   }

   IntRange(@Nullable NumberProvider pMin, @Nullable NumberProvider pMax) {
      this.min = pMin;
      this.max = pMax;
      if (pMin == null) {
         if (pMax == null) {
            this.limiter = (p_165050_, p_165051_) -> {
               return p_165051_;
            };
            this.predicate = (p_165043_, p_165044_) -> {
               return true;
            };
         } else {
            this.limiter = (p_165054_, p_165055_) -> {
               return Math.min(pMax.getInt(p_165054_), p_165055_);
            };
            this.predicate = (p_165047_, p_165048_) -> {
               return p_165048_ <= pMax.getInt(p_165047_);
            };
         }
      } else if (pMax == null) {
         this.limiter = (p_165033_, p_165034_) -> {
            return Math.max(pMin.getInt(p_165033_), p_165034_);
         };
         this.predicate = (p_165019_, p_165020_) -> {
            return p_165020_ >= pMin.getInt(p_165019_);
         };
      } else {
         this.limiter = (p_165038_, p_165039_) -> {
            return Mth.clamp(p_165039_, pMin.getInt(p_165038_), pMax.getInt(p_165038_));
         };
         this.predicate = (p_165024_, p_165025_) -> {
            return p_165025_ >= pMin.getInt(p_165024_) && p_165025_ <= pMax.getInt(p_165024_);
         };
      }

   }

   /**
    * Create an IntRange that contains only exactly the given value.
    */
   public static IntRange exact(int pExactValue) {
      ConstantValue constantvalue = ConstantValue.exactly((float)pExactValue);
      return new IntRange(constantvalue, constantvalue);
   }

   /**
    * Create an IntRange that ranges from {@code min} to {@code max}, both inclusive.
    */
   public static IntRange range(int pMin, int pMax) {
      return new IntRange(ConstantValue.exactly((float)pMin), ConstantValue.exactly((float)pMax));
   }

   /**
    * Create an IntRange with the given minimum (inclusive) and no upper bound.
    */
   public static IntRange lowerBound(int pMin) {
      return new IntRange(ConstantValue.exactly((float)pMin), (NumberProvider)null);
   }

   /**
    * Create an IntRange with the given maximum (inclusive) and no lower bound.
    */
   public static IntRange upperBound(int pMax) {
      return new IntRange((NumberProvider)null, ConstantValue.exactly((float)pMax));
   }

   /**
    * Clamp the given value so that it falls within this IntRange.
    */
   public int clamp(LootContext pLootContext, int pValue) {
      return this.limiter.apply(pLootContext, pValue);
   }

   /**
    * Check whether the given value falls within this IntRange.
    */
   public boolean test(LootContext pLootContext, int pValue) {
      return this.predicate.test(pLootContext, pValue);
   }

   @FunctionalInterface
   interface IntChecker {
      boolean test(LootContext pLootContext, int pValue);
   }

   @FunctionalInterface
   interface IntLimiter {
      int apply(LootContext pLootContext, int pValue);
   }

   public static class Serializer implements JsonDeserializer<IntRange>, JsonSerializer<IntRange> {
      public IntRange deserialize(JsonElement pJson, Type pTypeOfT, JsonDeserializationContext pContext) {
         if (pJson.isJsonPrimitive()) {
            return IntRange.exact(pJson.getAsInt());
         } else {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "value");
            NumberProvider numberprovider = jsonobject.has("min") ? GsonHelper.getAsObject(jsonobject, "min", pContext, NumberProvider.class) : null;
            NumberProvider numberprovider1 = jsonobject.has("max") ? GsonHelper.getAsObject(jsonobject, "max", pContext, NumberProvider.class) : null;
            return new IntRange(numberprovider, numberprovider1);
         }
      }

      public JsonElement serialize(IntRange pSrc, Type pTypeOfSrc, JsonSerializationContext pContext) {
         JsonObject jsonobject = new JsonObject();
         if (Objects.equals(pSrc.max, pSrc.min)) {
            return pContext.serialize(pSrc.min);
         } else {
            if (pSrc.max != null) {
               jsonobject.add("max", pContext.serialize(pSrc.max));
            }

            if (pSrc.min != null) {
               jsonobject.add("min", pContext.serialize(pSrc.min));
            }

            return jsonobject;
         }
      }
   }
}