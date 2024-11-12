package net.minecraft.data.models.blockstates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Function;

public class VariantProperty<T> {
   final String key;
   final Function<T, JsonElement> serializer;

   public VariantProperty(String pKey, Function<T, JsonElement> pSerializer) {
      this.key = pKey;
      this.serializer = pSerializer;
   }

   public VariantProperty<T>.Value withValue(T pValue) {
      return new VariantProperty.Value(pValue);
   }

   public String toString() {
      return this.key;
   }

   public class Value {
      private final T value;

      public Value(T pValue) {
         this.value = pValue;
      }

      public VariantProperty<T> getKey() {
         return VariantProperty.this;
      }

      public void addToVariant(JsonObject pJsonObject) {
         pJsonObject.add(VariantProperty.this.key, VariantProperty.this.serializer.apply(this.value));
      }

      public String toString() {
         return VariantProperty.this.key + "=" + this.value;
      }
   }
}