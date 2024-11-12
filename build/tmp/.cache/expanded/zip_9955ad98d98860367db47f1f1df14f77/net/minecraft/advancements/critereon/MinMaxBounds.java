package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public abstract class MinMaxBounds<T extends Number> {
   public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
   public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));
   @Nullable
   protected final T min;
   @Nullable
   protected final T max;

   protected MinMaxBounds(@Nullable T pMin, @Nullable T pMax) {
      this.min = pMin;
      this.max = pMax;
   }

   @Nullable
   public T getMin() {
      return this.min;
   }

   @Nullable
   public T getMax() {
      return this.max;
   }

   public boolean isAny() {
      return this.min == null && this.max == null;
   }

   public JsonElement serializeToJson() {
      if (this.isAny()) {
         return JsonNull.INSTANCE;
      } else if (this.min != null && this.min.equals(this.max)) {
         return new JsonPrimitive(this.min);
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.min != null) {
            jsonobject.addProperty("min", this.min);
         }

         if (this.max != null) {
            jsonobject.addProperty("max", this.max);
         }

         return jsonobject;
      }
   }

   protected static <T extends Number, R extends MinMaxBounds<T>> R fromJson(@Nullable JsonElement pJson, R pDefaultValue, BiFunction<JsonElement, String, T> pValueFactory, MinMaxBounds.BoundsFactory<T, R> pBoundedFactory) {
      if (pJson != null && !pJson.isJsonNull()) {
         if (GsonHelper.isNumberValue(pJson)) {
            T t2 = pValueFactory.apply(pJson, "value");
            return pBoundedFactory.create(t2, t2);
         } else {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "value");
            T t = jsonobject.has("min") ? pValueFactory.apply(jsonobject.get("min"), "min") : null;
            T t1 = jsonobject.has("max") ? pValueFactory.apply(jsonobject.get("max"), "max") : null;
            return pBoundedFactory.create(t, t1);
         }
      } else {
         return pDefaultValue;
      }
   }

   protected static <T extends Number, R extends MinMaxBounds<T>> R fromReader(StringReader pReader, MinMaxBounds.BoundsFromReaderFactory<T, R> pBoundedFactory, Function<String, T> pValueFactory, Supplier<DynamicCommandExceptionType> pCommandExceptionSupplier, Function<T, T> pFormatter) throws CommandSyntaxException {
      if (!pReader.canRead()) {
         throw ERROR_EMPTY.createWithContext(pReader);
      } else {
         int i = pReader.getCursor();

         try {
            T t = optionallyFormat(readNumber(pReader, pValueFactory, pCommandExceptionSupplier), pFormatter);
            T t1;
            if (pReader.canRead(2) && pReader.peek() == '.' && pReader.peek(1) == '.') {
               pReader.skip();
               pReader.skip();
               t1 = optionallyFormat(readNumber(pReader, pValueFactory, pCommandExceptionSupplier), pFormatter);
               if (t == null && t1 == null) {
                  throw ERROR_EMPTY.createWithContext(pReader);
               }
            } else {
               t1 = t;
            }

            if (t == null && t1 == null) {
               throw ERROR_EMPTY.createWithContext(pReader);
            } else {
               return pBoundedFactory.create(pReader, t, t1);
            }
         } catch (CommandSyntaxException commandsyntaxexception) {
            pReader.setCursor(i);
            throw new CommandSyntaxException(commandsyntaxexception.getType(), commandsyntaxexception.getRawMessage(), commandsyntaxexception.getInput(), i);
         }
      }
   }

   @Nullable
   private static <T extends Number> T readNumber(StringReader pReader, Function<String, T> pStringToValueFunction, Supplier<DynamicCommandExceptionType> pCommandExceptionSupplier) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedInputChat(pReader)) {
         pReader.skip();
      }

      String s = pReader.getString().substring(i, pReader.getCursor());
      if (s.isEmpty()) {
         return (T)null;
      } else {
         try {
            return pStringToValueFunction.apply(s);
         } catch (NumberFormatException numberformatexception) {
            throw pCommandExceptionSupplier.get().createWithContext(pReader, s);
         }
      }
   }

   private static boolean isAllowedInputChat(StringReader pReader) {
      char c0 = pReader.peek();
      if ((c0 < '0' || c0 > '9') && c0 != '-') {
         if (c0 != '.') {
            return false;
         } else {
            return !pReader.canRead(2) || pReader.peek(1) != '.';
         }
      } else {
         return true;
      }
   }

   @Nullable
   private static <T> T optionallyFormat(@Nullable T pValue, Function<T, T> pFormatter) {
      return (T)(pValue == null ? null : pFormatter.apply(pValue));
   }

   @FunctionalInterface
   protected interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
      R create(@Nullable T pMin, @Nullable T pMax);
   }

   @FunctionalInterface
   protected interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
      R create(StringReader pReader, @Nullable T pMin, @Nullable T pMax) throws CommandSyntaxException;
   }

   public static class Doubles extends MinMaxBounds<Double> {
      public static final MinMaxBounds.Doubles ANY = new MinMaxBounds.Doubles((Double)null, (Double)null);
      @Nullable
      private final Double minSq;
      @Nullable
      private final Double maxSq;

      private static MinMaxBounds.Doubles create(StringReader p_154796_, @Nullable Double p_154797_, @Nullable Double p_154798_) throws CommandSyntaxException {
         if (p_154797_ != null && p_154798_ != null && p_154797_ > p_154798_) {
            throw ERROR_SWAPPED.createWithContext(p_154796_);
         } else {
            return new MinMaxBounds.Doubles(p_154797_, p_154798_);
         }
      }

      @Nullable
      private static Double squareOpt(@Nullable Double pValue) {
         return pValue == null ? null : pValue * pValue;
      }

      private Doubles(@Nullable Double p_154784_, @Nullable Double p_154785_) {
         super(p_154784_, p_154785_);
         this.minSq = squareOpt(p_154784_);
         this.maxSq = squareOpt(p_154785_);
      }

      public static MinMaxBounds.Doubles exactly(double pValue) {
         return new MinMaxBounds.Doubles(pValue, pValue);
      }

      public static MinMaxBounds.Doubles between(double pMin, double pMax) {
         return new MinMaxBounds.Doubles(pMin, pMax);
      }

      public static MinMaxBounds.Doubles atLeast(double pMin) {
         return new MinMaxBounds.Doubles(pMin, (Double)null);
      }

      public static MinMaxBounds.Doubles atMost(double pMax) {
         return new MinMaxBounds.Doubles((Double)null, pMax);
      }

      public boolean matches(double pValue) {
         if (this.min != null && this.min > pValue) {
            return false;
         } else {
            return this.max == null || !(this.max < pValue);
         }
      }

      public boolean matchesSqr(double pValue) {
         if (this.minSq != null && this.minSq > pValue) {
            return false;
         } else {
            return this.maxSq == null || !(this.maxSq < pValue);
         }
      }

      public static MinMaxBounds.Doubles fromJson(@Nullable JsonElement pJson) {
         return fromJson(pJson, ANY, GsonHelper::convertToDouble, MinMaxBounds.Doubles::new);
      }

      public static MinMaxBounds.Doubles fromReader(StringReader pReader) throws CommandSyntaxException {
         return fromReader(pReader, (p_154807_) -> {
            return p_154807_;
         });
      }

      public static MinMaxBounds.Doubles fromReader(StringReader pReader, Function<Double, Double> pFormatter) throws CommandSyntaxException {
         return fromReader(pReader, MinMaxBounds.Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, pFormatter);
      }
   }

   public static class Ints extends MinMaxBounds<Integer> {
      public static final MinMaxBounds.Ints ANY = new MinMaxBounds.Ints((Integer)null, (Integer)null);
      @Nullable
      private final Long minSq;
      @Nullable
      private final Long maxSq;

      private static MinMaxBounds.Ints create(StringReader p_55378_, @Nullable Integer p_55379_, @Nullable Integer p_55380_) throws CommandSyntaxException {
         if (p_55379_ != null && p_55380_ != null && p_55379_ > p_55380_) {
            throw ERROR_SWAPPED.createWithContext(p_55378_);
         } else {
            return new MinMaxBounds.Ints(p_55379_, p_55380_);
         }
      }

      @Nullable
      private static Long squareOpt(@Nullable Integer pValue) {
         return pValue == null ? null : pValue.longValue() * pValue.longValue();
      }

      private Ints(@Nullable Integer p_55369_, @Nullable Integer p_55370_) {
         super(p_55369_, p_55370_);
         this.minSq = squareOpt(p_55369_);
         this.maxSq = squareOpt(p_55370_);
      }

      public static MinMaxBounds.Ints exactly(int pValue) {
         return new MinMaxBounds.Ints(pValue, pValue);
      }

      public static MinMaxBounds.Ints between(int pMin, int pMax) {
         return new MinMaxBounds.Ints(pMin, pMax);
      }

      public static MinMaxBounds.Ints atLeast(int pMin) {
         return new MinMaxBounds.Ints(pMin, (Integer)null);
      }

      public static MinMaxBounds.Ints atMost(int pMax) {
         return new MinMaxBounds.Ints((Integer)null, pMax);
      }

      public boolean matches(int pValue) {
         if (this.min != null && this.min > pValue) {
            return false;
         } else {
            return this.max == null || this.max >= pValue;
         }
      }

      public boolean matchesSqr(long pValue) {
         if (this.minSq != null && this.minSq > pValue) {
            return false;
         } else {
            return this.maxSq == null || this.maxSq >= pValue;
         }
      }

      public static MinMaxBounds.Ints fromJson(@Nullable JsonElement pJson) {
         return fromJson(pJson, ANY, GsonHelper::convertToInt, MinMaxBounds.Ints::new);
      }

      public static MinMaxBounds.Ints fromReader(StringReader pReader) throws CommandSyntaxException {
         return fromReader(pReader, (p_55389_) -> {
            return p_55389_;
         });
      }

      public static MinMaxBounds.Ints fromReader(StringReader pReader, Function<Integer, Integer> pValueFunction) throws CommandSyntaxException {
         return fromReader(pReader, MinMaxBounds.Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, pValueFunction);
      }
   }
}