package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class StatePropertiesPredicate {
   public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(ImmutableList.of());
   private final List<StatePropertiesPredicate.PropertyMatcher> properties;

   private static StatePropertiesPredicate.PropertyMatcher fromJson(String pName, JsonElement pJson) {
      if (pJson.isJsonPrimitive()) {
         String s2 = pJson.getAsString();
         return new StatePropertiesPredicate.ExactPropertyMatcher(pName, s2);
      } else {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "value");
         String s = jsonobject.has("min") ? getStringOrNull(jsonobject.get("min")) : null;
         String s1 = jsonobject.has("max") ? getStringOrNull(jsonobject.get("max")) : null;
         return (StatePropertiesPredicate.PropertyMatcher)(s != null && s.equals(s1) ? new StatePropertiesPredicate.ExactPropertyMatcher(pName, s) : new StatePropertiesPredicate.RangedPropertyMatcher(pName, s, s1));
      }
   }

   @Nullable
   private static String getStringOrNull(JsonElement pJson) {
      return pJson.isJsonNull() ? null : pJson.getAsString();
   }

   StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> pProperties) {
      this.properties = ImmutableList.copyOf(pProperties);
   }

   public <S extends StateHolder<?, S>> boolean matches(StateDefinition<?, S> pProperties, S pTargetProperty) {
      for(StatePropertiesPredicate.PropertyMatcher statepropertiespredicate$propertymatcher : this.properties) {
         if (!statepropertiespredicate$propertymatcher.match(pProperties, pTargetProperty)) {
            return false;
         }
      }

      return true;
   }

   public boolean matches(BlockState pState) {
      return this.matches(pState.getBlock().getStateDefinition(), pState);
   }

   public boolean matches(FluidState pState) {
      return this.matches(pState.getType().getStateDefinition(), pState);
   }

   public void checkState(StateDefinition<?, ?> pProperties, Consumer<String> pPropertyConsumer) {
      this.properties.forEach((p_67678_) -> {
         p_67678_.checkState(pProperties, pPropertyConsumer);
      });
   }

   public static StatePropertiesPredicate fromJson(@Nullable JsonElement pJson) {
      if (pJson != null && !pJson.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(pJson, "properties");
         List<StatePropertiesPredicate.PropertyMatcher> list = Lists.newArrayList();

         for(Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            list.add(fromJson(entry.getKey(), entry.getValue()));
         }

         return new StatePropertiesPredicate(list);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.properties.isEmpty()) {
            this.properties.forEach((p_67683_) -> {
               jsonobject.add(p_67683_.getName(), p_67683_.toJson());
            });
         }

         return jsonobject;
      }
   }

   public static class Builder {
      private final List<StatePropertiesPredicate.PropertyMatcher> matchers = Lists.newArrayList();

      private Builder() {
      }

      public static StatePropertiesPredicate.Builder properties() {
         return new StatePropertiesPredicate.Builder();
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<?> pProperty, String pValue) {
         this.matchers.add(new StatePropertiesPredicate.ExactPropertyMatcher(pProperty.getName(), pValue));
         return this;
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Integer> pProperty, int pValue) {
         return this.hasProperty(pProperty, Integer.toString(pValue));
      }

      public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> pProperty, boolean pValue) {
         return this.hasProperty(pProperty, Boolean.toString(pValue));
      }

      public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> pProperty, T pValue) {
         return this.hasProperty(pProperty, pValue.getSerializedName());
      }

      public StatePropertiesPredicate build() {
         return new StatePropertiesPredicate(this.matchers);
      }
   }

   static class ExactPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
      private final String value;

      public ExactPropertyMatcher(String pName, String pValue) {
         super(pName);
         this.value = pValue;
      }

      protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pPropertyTarget) {
         T t = pProperties.getValue(pPropertyTarget);
         Optional<T> optional = pPropertyTarget.getValue(this.value);
         return optional.isPresent() && t.compareTo(optional.get()) == 0;
      }

      public JsonElement toJson() {
         return new JsonPrimitive(this.value);
      }
   }

   abstract static class PropertyMatcher {
      private final String name;

      public PropertyMatcher(String pName) {
         this.name = pName;
      }

      public <S extends StateHolder<?, S>> boolean match(StateDefinition<?, S> pProperties, S pPropertyToMatch) {
         Property<?> property = pProperties.getProperty(this.name);
         return property == null ? false : this.match(pPropertyToMatch, property);
      }

      protected abstract <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pProperty);

      public abstract JsonElement toJson();

      public String getName() {
         return this.name;
      }

      public void checkState(StateDefinition<?, ?> pProperties, Consumer<String> pPropertyConsumer) {
         Property<?> property = pProperties.getProperty(this.name);
         if (property == null) {
            pPropertyConsumer.accept(this.name);
         }

      }
   }

   static class RangedPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
      @Nullable
      private final String minValue;
      @Nullable
      private final String maxValue;

      public RangedPropertyMatcher(String pName, @Nullable String pMinValue, @Nullable String pMaxValue) {
         super(pName);
         this.minValue = pMinValue;
         this.maxValue = pMaxValue;
      }

      protected <T extends Comparable<T>> boolean match(StateHolder<?, ?> pProperties, Property<T> pPropertyTarget) {
         T t = pProperties.getValue(pPropertyTarget);
         if (this.minValue != null) {
            Optional<T> optional = pPropertyTarget.getValue(this.minValue);
            if (!optional.isPresent() || t.compareTo(optional.get()) < 0) {
               return false;
            }
         }

         if (this.maxValue != null) {
            Optional<T> optional1 = pPropertyTarget.getValue(this.maxValue);
            if (!optional1.isPresent() || t.compareTo(optional1.get()) > 0) {
               return false;
            }
         }

         return true;
      }

      public JsonElement toJson() {
         JsonObject jsonobject = new JsonObject();
         if (this.minValue != null) {
            jsonobject.addProperty("min", this.minValue);
         }

         if (this.maxValue != null) {
            jsonobject.addProperty("max", this.maxValue);
         }

         return jsonobject;
      }
   }
}