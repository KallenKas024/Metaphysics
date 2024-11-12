package net.minecraft.world.level.block.state;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class StateHolder<O, S> {
   public static final String NAME_TAG = "Name";
   public static final String PROPERTIES_TAG = "Properties";
   private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>() {
      public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> p_61155_) {
         if (p_61155_ == null) {
            return "<NULL>";
         } else {
            Property<?> property = p_61155_.getKey();
            return property.getName() + "=" + this.getName(property, p_61155_.getValue());
         }
      }

      private <T extends Comparable<T>> String getName(Property<T> p_61152_, Comparable<?> p_61153_) {
         return p_61152_.getName((T)p_61153_);
      }
   };
   protected final O owner;
   private final ImmutableMap<Property<?>, Comparable<?>> values;
   private Table<Property<?>, Comparable<?>, S> neighbours;
   protected final MapCodec<S> propertiesCodec;

   protected StateHolder(O pOwner, ImmutableMap<Property<?>, Comparable<?>> pValues, MapCodec<S> pPropertiesCodec) {
      this.owner = pOwner;
      this.values = pValues;
      this.propertiesCodec = pPropertiesCodec;
   }

   public <T extends Comparable<T>> S cycle(Property<T> pProperty) {
      return this.setValue(pProperty, findNextInCollection(pProperty.getPossibleValues(), this.getValue(pProperty)));
   }

   protected static <T> T findNextInCollection(Collection<T> pCollection, T pValue) {
      Iterator<T> iterator = pCollection.iterator();

      while(iterator.hasNext()) {
         if (iterator.next().equals(pValue)) {
            if (iterator.hasNext()) {
               return iterator.next();
            }

            return pCollection.iterator().next();
         }
      }

      return iterator.next();
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append(this.owner);
      if (!this.getValues().isEmpty()) {
         stringbuilder.append('[');
         stringbuilder.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
         stringbuilder.append(']');
      }

      return stringbuilder.toString();
   }

   /**
    * @return an unmodifiable collection of all possible properties.
    */
   public Collection<Property<?>> getProperties() {
      return Collections.unmodifiableCollection(this.values.keySet());
   }

   public <T extends Comparable<T>> boolean hasProperty(Property<T> pProperty) {
      return this.values.containsKey(pProperty);
   }

   /**
    * @return the value of the given Property for this state
    */
   public <T extends Comparable<T>> T getValue(Property<T> pProperty) {
      Comparable<?> comparable = this.values.get(pProperty);
      if (comparable == null) {
         throw new IllegalArgumentException("Cannot get property " + pProperty + " as it does not exist in " + this.owner);
      } else {
         return pProperty.getValueClass().cast(comparable);
      }
   }

   public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> pProperty) {
      Comparable<?> comparable = this.values.get(pProperty);
      return comparable == null ? Optional.empty() : Optional.of(pProperty.getValueClass().cast(comparable));
   }

   public <T extends Comparable<T>, V extends T> S setValue(Property<T> pProperty, V pValue) {
      Comparable<?> comparable = this.values.get(pProperty);
      if (comparable == null) {
         throw new IllegalArgumentException("Cannot set property " + pProperty + " as it does not exist in " + this.owner);
      } else if (comparable.equals(pValue)) {
         return (S)this;
      } else {
         S s = this.neighbours.get(pProperty, pValue);
         if (s == null) {
            throw new IllegalArgumentException("Cannot set property " + pProperty + " to " + pValue + " on " + this.owner + ", it is not an allowed value");
         } else {
            return s;
         }
      }
   }

   public <T extends Comparable<T>, V extends T> S trySetValue(Property<T> pProperty, V pValue) {
      Comparable<?> comparable = this.values.get(pProperty);
      if (comparable != null && !comparable.equals(pValue)) {
         S s = this.neighbours.get(pProperty, pValue);
         if (s == null) {
            throw new IllegalArgumentException("Cannot set property " + pProperty + " to " + pValue + " on " + this.owner + ", it is not an allowed value");
         } else {
            return s;
         }
      } else {
         return (S)this;
      }
   }

   public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> pPossibleStateMap) {
      if (this.neighbours != null) {
         throw new IllegalStateException();
      } else {
         Table<Property<?>, Comparable<?>, S> table = HashBasedTable.create();

         for(Map.Entry<Property<?>, Comparable<?>> entry : this.values.entrySet()) {
            Property<?> property = entry.getKey();

            for(Comparable<?> comparable : property.getPossibleValues()) {
               if (!comparable.equals(entry.getValue())) {
                  table.put(property, comparable, pPossibleStateMap.get(this.makeNeighbourValues(property, comparable)));
               }
            }
         }

         this.neighbours = (Table<Property<?>, Comparable<?>, S>)(table.isEmpty() ? table : ArrayTable.create(table));
      }
   }

   private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> pProperty, Comparable<?> pValue) {
      Map<Property<?>, Comparable<?>> map = Maps.newHashMap(this.values);
      map.put(pProperty, pValue);
      return map;
   }

   public ImmutableMap<Property<?>, Comparable<?>> getValues() {
      return this.values;
   }

   protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> pPropertyMap, Function<O, S> pHolderFunction) {
      return pPropertyMap.dispatch("Name", (p_61121_) -> {
         return p_61121_.owner;
      }, (p_187547_) -> {
         S s = pHolderFunction.apply(p_187547_);
         return s.getValues().isEmpty() ? Codec.unit(s) : s.propertiesCodec.codec().optionalFieldOf("Properties").xmap((p_187544_) -> {
            return p_187544_.orElse(s);
         }, Optional::of).codec();
      });
   }
}