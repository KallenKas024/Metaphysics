package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Variant implements Supplier<JsonElement> {
   private final Map<VariantProperty<?>, VariantProperty<?>.Value> values = Maps.newLinkedHashMap();

   public <T> Variant with(VariantProperty<T> pProperty, T pValue) {
      VariantProperty<?>.Value variantproperty = this.values.put(pProperty, pProperty.withValue(pValue));
      if (variantproperty != null) {
         throw new IllegalStateException("Replacing value of " + variantproperty + " with " + pValue);
      } else {
         return this;
      }
   }

   public static Variant variant() {
      return new Variant();
   }

   public static Variant merge(Variant pDefinition1, Variant pDefinition2) {
      Variant variant = new Variant();
      variant.values.putAll(pDefinition1.values);
      variant.values.putAll(pDefinition2.values);
      return variant;
   }

   public JsonElement get() {
      JsonObject jsonobject = new JsonObject();
      this.values.values().forEach((p_125507_) -> {
         p_125507_.addToVariant(jsonobject);
      });
      return jsonobject;
   }

   public static JsonElement convertList(List<Variant> pDefinitions) {
      if (pDefinitions.size() == 1) {
         return pDefinitions.get(0).get();
      } else {
         JsonArray jsonarray = new JsonArray();
         pDefinitions.forEach((p_125504_) -> {
            jsonarray.add(p_125504_.get());
         });
         return jsonarray;
      }
   }
}