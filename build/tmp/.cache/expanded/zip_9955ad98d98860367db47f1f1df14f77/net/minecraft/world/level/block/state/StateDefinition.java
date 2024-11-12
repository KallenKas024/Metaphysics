package net.minecraft.world.level.block.state;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public class StateDefinition<O, S extends StateHolder<O, S>> {
   static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$");
   private final O owner;
   private final ImmutableSortedMap<String, Property<?>> propertiesByName;
   private final ImmutableList<S> states;

   protected StateDefinition(Function<O, S> pStateValueFunction, O pOwner, StateDefinition.Factory<O, S> pValueFunction, Map<String, Property<?>> pPropertiesByName) {
      this.owner = pOwner;
      this.propertiesByName = ImmutableSortedMap.copyOf(pPropertiesByName);
      Supplier<S> supplier = () -> {
         return pStateValueFunction.apply(pOwner);
      };
      MapCodec<S> mapcodec = MapCodec.of(Encoder.empty(), Decoder.unit(supplier));

      for(Map.Entry<String, Property<?>> entry : this.propertiesByName.entrySet()) {
         mapcodec = appendPropertyCodec(mapcodec, supplier, entry.getKey(), entry.getValue());
      }

      MapCodec<S> mapcodec1 = mapcodec;
      Map<Map<Property<?>, Comparable<?>>, S> map = Maps.newLinkedHashMap();
      List<S> list = Lists.newArrayList();
      Stream<List<Pair<Property<?>, Comparable<?>>>> stream = Stream.of(Collections.emptyList());

      for(Property<?> property : this.propertiesByName.values()) {
         stream = stream.flatMap((p_61072_) -> {
            return property.getPossibleValues().stream().map((p_155961_) -> {
               List<Pair<Property<?>, Comparable<?>>> list1 = Lists.newArrayList(p_61072_);
               list1.add(Pair.of(property, p_155961_));
               return list1;
            });
         });
      }

      stream.forEach((p_61063_) -> {
         ImmutableMap<Property<?>, Comparable<?>> immutablemap = p_61063_.stream().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
         S s1 = pValueFunction.create(pOwner, immutablemap, mapcodec1);
         map.put(immutablemap, s1);
         list.add(s1);
      });

      for(S s : list) {
         s.populateNeighbours(map);
      }

      this.states = ImmutableList.copyOf(list);
   }

   private static <S extends StateHolder<?, S>, T extends Comparable<T>> MapCodec<S> appendPropertyCodec(MapCodec<S> pPropertyCodec, Supplier<S> pHolderSupplier, String pValue, Property<T> pProperty) {
      return Codec.mapPair(pPropertyCodec, pProperty.valueCodec().fieldOf(pValue).orElseGet((p_187541_) -> {
      }, () -> {
         return pProperty.value(pHolderSupplier.get());
      })).xmap((p_187536_) -> {
         return p_187536_.getFirst().setValue(pProperty, p_187536_.getSecond().value());
      }, (p_187533_) -> {
         return Pair.of(p_187533_, pProperty.value(p_187533_));
      });
   }

   public ImmutableList<S> getPossibleStates() {
      return this.states;
   }

   public S any() {
      return this.states.get(0);
   }

   public O getOwner() {
      return this.owner;
   }

   public Collection<Property<?>> getProperties() {
      return this.propertiesByName.values();
   }

   public String toString() {
      return MoreObjects.toStringHelper(this).add("block", this.owner).add("properties", this.propertiesByName.values().stream().map(Property::getName).collect(Collectors.toList())).toString();
   }

   @Nullable
   public Property<?> getProperty(String pPropertyName) {
      return this.propertiesByName.get(pPropertyName);
   }

   public static class Builder<O, S extends StateHolder<O, S>> {
      private final O owner;
      private final Map<String, Property<?>> properties = Maps.newHashMap();

      public Builder(O pOwner) {
         this.owner = pOwner;
      }

      public StateDefinition.Builder<O, S> add(Property<?>... pProperties) {
         for(Property<?> property : pProperties) {
            this.validateProperty(property);
            this.properties.put(property.getName(), property);
         }

         return this;
      }

      private <T extends Comparable<T>> void validateProperty(Property<T> pProperty) {
         String s = pProperty.getName();
         if (!StateDefinition.NAME_PATTERN.matcher(s).matches()) {
            throw new IllegalArgumentException(this.owner + " has invalidly named property: " + s);
         } else {
            Collection<T> collection = pProperty.getPossibleValues();
            if (collection.size() <= 1) {
               throw new IllegalArgumentException(this.owner + " attempted use property " + s + " with <= 1 possible values");
            } else {
               for(T t : collection) {
                  String s1 = pProperty.getName(t);
                  if (!StateDefinition.NAME_PATTERN.matcher(s1).matches()) {
                     throw new IllegalArgumentException(this.owner + " has property: " + s + " with invalidly named value: " + s1);
                  }
               }

               if (this.properties.containsKey(s)) {
                  throw new IllegalArgumentException(this.owner + " has duplicate property: " + s);
               }
            }
         }
      }

      public StateDefinition<O, S> create(Function<O, S> pStateValueFunction, StateDefinition.Factory<O, S> pStateFunction) {
         return new StateDefinition<>(pStateValueFunction, this.owner, pStateFunction, this.properties);
      }
   }

   public interface Factory<O, S> {
      S create(O pProperty, ImmutableMap<Property<?>, Comparable<?>> pPropToValueMap, MapCodec<S> pPropCodec);
   }
}