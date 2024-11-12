package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class WrappedMinMaxBounds {
   public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds((Float)null, (Float)null);
   public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(Component.translatable("argument.range.ints"));
   @Nullable
   private final Float min;
   @Nullable
   private final Float max;

   public WrappedMinMaxBounds(@Nullable Float pMin, @Nullable Float pMax) {
      this.min = pMin;
      this.max = pMax;
   }

   public static WrappedMinMaxBounds exactly(float pValue) {
      return new WrappedMinMaxBounds(pValue, pValue);
   }

   public static WrappedMinMaxBounds between(float pMin, float pMax) {
      return new WrappedMinMaxBounds(pMin, pMax);
   }

   public static WrappedMinMaxBounds atLeast(float pMin) {
      return new WrappedMinMaxBounds(pMin, (Float)null);
   }

   public static WrappedMinMaxBounds atMost(float pMax) {
      return new WrappedMinMaxBounds((Float)null, pMax);
   }

   public boolean matches(float pValue) {
      if (this.min != null && this.max != null && this.min > this.max && this.min > pValue && this.max < pValue) {
         return false;
      } else if (this.min != null && this.min > pValue) {
         return false;
      } else {
         return this.max == null || !(this.max < pValue);
      }
   }

   public boolean matchesSqr(double pValue) {
      if (this.min != null && this.max != null && this.min > this.max && (double)(this.min * this.min) > pValue && (double)(this.max * this.max) < pValue) {
         return false;
      } else if (this.min != null && (double)(this.min * this.min) > pValue) {
         return false;
      } else {
         return this.max == null || !((double)(this.max * this.max) < pValue);
      }
   }

   @Nullable
   public Float getMin() {
      return this.min;
   }

   @Nullable
   public Float getMax() {
      return this.max;
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else if (this.min != null && this.max != null && this.min.equals(this.max)) {
         return new JsonPrimitive(this.min);
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.min != null) {
            jsonobject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonobject.addProperty("max", this.min);
         }

         return jsonobject;
      }
   }

   public static WrappedMinMaxBounds fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         if (GsonHelper.isNumberValue(pJson)) {
            float f2 = GsonHelper.convertToFloat(pJson, "value");
            return new WrappedMinMaxBounds(f2, f2);
         } else {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "value");
            Float f = jsonobject.has("min") ? GsonHelper.getAsFloat(jsonobject, "min") : null;
            Float f1 = jsonobject.has("max") ? GsonHelper.getAsFloat(jsonobject, "max") : null;
            return new WrappedMinMaxBounds(f, f1);
         }
      } else {
         return ANY;
      }
   }

   public static WrappedMinMaxBounds fromReader(StringReader pReader, boolean pIsFloatingPoint) throws CommandSyntaxException {
      return fromReader(pReader, pIsFloatingPoint, (p_164413_) -> {
         return p_164413_;
      });
   }

   public static WrappedMinMaxBounds fromReader(StringReader pReader, boolean pIsFloatingPoint, Function<Float, Float> pValueFactory) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
      } else {
         int i = pReader.getCursor();
         Float f = optionallyFormat(readNumber(pReader, pIsFloatingPoint), pValueFactory);
         Float f1;
         if (pReader.canRead(2) && pReader.peek() == '.' && pReader.peek(1) == '.') {
            pReader.skip();
            pReader.skip();
            f1 = optionallyFormat(readNumber(pReader, pIsFloatingPoint), pValueFactory);
            if (f == null && f1 == null) {
               pReader.setCursor(i);
               throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
            }
         } else {
            if (!pIsFloatingPoint && pReader.canRead() && pReader.peek() == '.') {
               pReader.setCursor(i);
               throw ERROR_INTS_ONLY.createWithContext(pReader);
            }

            f1 = f;
         }

         if (f == null && f1 == null) {
            pReader.setCursor(i);
            throw MinMaxBounds.ERROR_EMPTY.createWithContext(pReader);
         } else {
            return new WrappedMinMaxBounds(f, f1);
         }
      }
   }

   @Nullable
   private static Float readNumber(StringReader pReader, boolean pIsFloatingPoint) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedNumber(pReader, pIsFloatingPoint)) {
         pReader.skip();
      }

      String s = pReader.getString().substring(i, pReader.getCursor());
      if (s.isEmpty()) {
         return null;
      } else {
         try {
            return Float.parseFloat(s);
         } catch (NumberFormatException numberformatexception) {
            if (pIsFloatingPoint) {
               throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(pReader, s);
            } else {
               throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(pReader, s);
            }
         }
      }
   }

   private static boolean isAllowedNumber(StringReader pReader, boolean pIsFloatingPoint) {
      char c0 = pReader.peek();
      if ((c0 < '0' || c0 > '9') && c0 != '-') {
         if (pIsFloatingPoint && c0 == '.') {
            return !pReader.canRead(2) || pReader.peek(1) != '.';
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static Float optionallyFormat(@Nullable Float pValue, Function<Float, Float> pValueFactory) {
      return pValue == null ? null : pValueFactory.apply(pValue);
   }
}