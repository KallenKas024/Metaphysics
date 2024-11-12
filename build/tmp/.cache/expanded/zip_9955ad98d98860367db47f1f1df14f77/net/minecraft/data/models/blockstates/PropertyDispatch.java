package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class PropertyDispatch {
   private final Map<Selector, List<Variant>> values = Maps.newHashMap();

   protected void putValue(Selector pSelector, List<Variant> pValues) {
      List<Variant> list = this.values.put(pSelector, pValues);
      if (list != null) {
         throw new IllegalStateException("Value " + pSelector + " is already defined");
      }
   }

   Map<Selector, List<Variant>> getEntries() {
      this.verifyComplete();
      return ImmutableMap.copyOf(this.values);
   }

   private void verifyComplete() {
      List<Property<?>> list = this.getDefinedProperties();
      Stream<Selector> stream = Stream.of(Selector.empty());

      for(Property<?> property : list) {
         stream = stream.flatMap((p_125316_) -> {
            return property.getAllValues().map(p_125316_::extend);
         });
      }

      List<Selector> list1 = stream.filter((p_125318_) -> {
         return !this.values.containsKey(p_125318_);
      }).collect(Collectors.toList());
      if (!list1.isEmpty()) {
         throw new IllegalStateException("Missing definition for properties: " + list1);
      }
   }

   abstract List<Property<?>> getDefinedProperties();

   public static <T1 extends Comparable<T1>> PropertyDispatch.C1<T1> property(Property<T1> pProperty1) {
      return new PropertyDispatch.C1<>(pProperty1);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>> PropertyDispatch.C2<T1, T2> properties(Property<T1> pProperty1, Property<T2> pProperty2) {
      return new PropertyDispatch.C2<>(pProperty1, pProperty2);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> PropertyDispatch.C3<T1, T2, T3> properties(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3) {
      return new PropertyDispatch.C3<>(pProperty1, pProperty2, pProperty3);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> PropertyDispatch.C4<T1, T2, T3, T4> properties(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4) {
      return new PropertyDispatch.C4<>(pProperty1, pProperty2, pProperty3, pProperty4);
   }

   public static <T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> PropertyDispatch.C5<T1, T2, T3, T4, T5> properties(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4, Property<T5> pProperty5) {
      return new PropertyDispatch.C5<>(pProperty1, pProperty2, pProperty3, pProperty4, pProperty5);
   }

   public static class C1<T1 extends Comparable<T1>> extends PropertyDispatch {
      private final Property<T1> property1;

      C1(Property<T1> pProperty1) {
         this.property1 = pProperty1;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1);
      }

      public PropertyDispatch.C1<T1> select(T1 pPropertyValue, List<Variant> pVariants) {
         Selector selector = Selector.of(this.property1.value(pPropertyValue));
         this.putValue(selector, pVariants);
         return this;
      }

      public PropertyDispatch.C1<T1> select(T1 pPropertyValue, Variant pVariant) {
         return this.select(pPropertyValue, Collections.singletonList(pVariant));
      }

      public PropertyDispatch generate(Function<T1, Variant> pPropertyValueToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_125340_) -> {
            this.select(p_125340_, pPropertyValueToVariantMapper.apply(p_125340_));
         });
         return this;
      }

      public PropertyDispatch generateList(Function<T1, List<Variant>> pPropertyValueToVariantsMapper) {
         this.property1.getPossibleValues().forEach((p_176312_) -> {
            this.select(p_176312_, pPropertyValueToVariantsMapper.apply(p_176312_));
         });
         return this;
      }
   }

   public static class C2<T1 extends Comparable<T1>, T2 extends Comparable<T2>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;

      C2(Property<T1> pProperty1, Property<T2> pProperty2) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2);
      }

      public PropertyDispatch.C2<T1, T2> select(T1 pProperty1Value, T2 pProperty2Value, List<Variant> pVariants) {
         Selector selector = Selector.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value));
         this.putValue(selector, pVariants);
         return this;
      }

      public PropertyDispatch.C2<T1, T2> select(T1 pProperty1Value, T2 pProperty2Value, Variant pVariant) {
         return this.select(pProperty1Value, pProperty2Value, Collections.singletonList(pVariant));
      }

      public PropertyDispatch generate(BiFunction<T1, T2, Variant> pPropertyValuesToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_125376_) -> {
            this.property2.getPossibleValues().forEach((p_176322_) -> {
               this.select((T1)p_125376_, p_176322_, pPropertyValuesToVariantMapper.apply((T1)p_125376_, p_176322_));
            });
         });
         return this;
      }

      public PropertyDispatch generateList(BiFunction<T1, T2, List<Variant>> pPropertyValuesToVariantsMapper) {
         this.property1.getPossibleValues().forEach((p_125366_) -> {
            this.property2.getPossibleValues().forEach((p_176318_) -> {
               this.select((T1)p_125366_, p_176318_, pPropertyValuesToVariantsMapper.apply((T1)p_125366_, p_176318_));
            });
         });
         return this;
      }
   }

   public static class C3<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;

      C3(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
         this.property3 = pProperty3;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3);
      }

      public PropertyDispatch.C3<T1, T2, T3> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, List<Variant> pVariants) {
         Selector selector = Selector.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value), this.property3.value(pProperty3Value));
         this.putValue(selector, pVariants);
         return this;
      }

      public PropertyDispatch.C3<T1, T2, T3> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, Variant pVariant) {
         return this.select(pProperty1Value, pProperty2Value, pProperty3Value, Collections.singletonList(pVariant));
      }

      public PropertyDispatch generate(PropertyDispatch.TriFunction<T1, T2, T3, Variant> pPropertyValuesToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_125404_) -> {
            this.property2.getPossibleValues().forEach((p_176343_) -> {
               this.property3.getPossibleValues().forEach((p_176339_) -> {
                  this.select((T1)p_125404_, (T2)p_176343_, p_176339_, pPropertyValuesToVariantMapper.apply((T1)p_125404_, (T2)p_176343_, p_176339_));
               });
            });
         });
         return this;
      }

      public PropertyDispatch generateList(PropertyDispatch.TriFunction<T1, T2, T3, List<Variant>> pPropertyValuesToVariantsMapper) {
         this.property1.getPossibleValues().forEach((p_176334_) -> {
            this.property2.getPossibleValues().forEach((p_176331_) -> {
               this.property3.getPossibleValues().forEach((p_176327_) -> {
                  this.select((T1)p_176334_, (T2)p_176331_, p_176327_, pPropertyValuesToVariantsMapper.apply((T1)p_176334_, (T2)p_176331_, p_176327_));
               });
            });
         });
         return this;
      }
   }

   public static class C4<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;
      private final Property<T4> property4;

      C4(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
         this.property3 = pProperty3;
         this.property4 = pProperty4;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3, this.property4);
      }

      public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, List<Variant> pVariants) {
         Selector selector = Selector.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value), this.property3.value(pProperty3Value), this.property4.value(pProperty4Value));
         this.putValue(selector, pVariants);
         return this;
      }

      public PropertyDispatch.C4<T1, T2, T3, T4> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, Variant pVariant) {
         return this.select(pProperty1Value, pProperty2Value, pProperty3Value, pProperty4Value, Collections.singletonList(pVariant));
      }

      public PropertyDispatch generate(PropertyDispatch.QuadFunction<T1, T2, T3, T4, Variant> pPropertyValuesToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_176385_) -> {
            this.property2.getPossibleValues().forEach((p_176380_) -> {
               this.property3.getPossibleValues().forEach((p_176376_) -> {
                  this.property4.getPossibleValues().forEach((p_176371_) -> {
                     this.select((T1)p_176385_, (T2)p_176380_, (T3)p_176376_, p_176371_, pPropertyValuesToVariantMapper.apply((T1)p_176385_, (T2)p_176380_, (T3)p_176376_, p_176371_));
                  });
               });
            });
         });
         return this;
      }

      public PropertyDispatch generateList(PropertyDispatch.QuadFunction<T1, T2, T3, T4, List<Variant>> pPropertyValuesToVariantsMapper) {
         this.property1.getPossibleValues().forEach((p_176365_) -> {
            this.property2.getPossibleValues().forEach((p_176360_) -> {
               this.property3.getPossibleValues().forEach((p_176356_) -> {
                  this.property4.getPossibleValues().forEach((p_176351_) -> {
                     this.select((T1)p_176365_, (T2)p_176360_, (T3)p_176356_, p_176351_, pPropertyValuesToVariantsMapper.apply((T1)p_176365_, (T2)p_176360_, (T3)p_176356_, p_176351_));
                  });
               });
            });
         });
         return this;
      }
   }

   public static class C5<T1 extends Comparable<T1>, T2 extends Comparable<T2>, T3 extends Comparable<T3>, T4 extends Comparable<T4>, T5 extends Comparable<T5>> extends PropertyDispatch {
      private final Property<T1> property1;
      private final Property<T2> property2;
      private final Property<T3> property3;
      private final Property<T4> property4;
      private final Property<T5> property5;

      C5(Property<T1> pProperty1, Property<T2> pProperty2, Property<T3> pProperty3, Property<T4> pProperty4, Property<T5> pProperty5) {
         this.property1 = pProperty1;
         this.property2 = pProperty2;
         this.property3 = pProperty3;
         this.property4 = pProperty4;
         this.property5 = pProperty5;
      }

      public List<Property<?>> getDefinedProperties() {
         return ImmutableList.of(this.property1, this.property2, this.property3, this.property4, this.property5);
      }

      public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, T5 pProperty5Value, List<Variant> pVariants) {
         Selector selector = Selector.of(this.property1.value(pProperty1Value), this.property2.value(pProperty2Value), this.property3.value(pProperty3Value), this.property4.value(pProperty4Value), this.property5.value(pProperty5Value));
         this.putValue(selector, pVariants);
         return this;
      }

      public PropertyDispatch.C5<T1, T2, T3, T4, T5> select(T1 pProperty1Value, T2 pProperty2Value, T3 pProperty3Value, T4 pProperty4Value, T5 pProperty5Value, Variant pVariant) {
         return this.select(pProperty1Value, pProperty2Value, pProperty3Value, pProperty4Value, pProperty5Value, Collections.singletonList(pVariant));
      }

      public PropertyDispatch generate(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, Variant> pPropertyValuesToVariantMapper) {
         this.property1.getPossibleValues().forEach((p_176439_) -> {
            this.property2.getPossibleValues().forEach((p_176434_) -> {
               this.property3.getPossibleValues().forEach((p_176430_) -> {
                  this.property4.getPossibleValues().forEach((p_176425_) -> {
                     this.property5.getPossibleValues().forEach((p_176419_) -> {
                        this.select((T1)p_176439_, (T2)p_176434_, (T3)p_176430_, (T4)p_176425_, p_176419_, pPropertyValuesToVariantMapper.apply((T1)p_176439_, (T2)p_176434_, (T3)p_176430_, (T4)p_176425_, p_176419_));
                     });
                  });
               });
            });
         });
         return this;
      }

      public PropertyDispatch generateList(PropertyDispatch.PentaFunction<T1, T2, T3, T4, T5, List<Variant>> pPropertyValuesToVariantsMapper) {
         this.property1.getPossibleValues().forEach((p_176412_) -> {
            this.property2.getPossibleValues().forEach((p_176407_) -> {
               this.property3.getPossibleValues().forEach((p_176403_) -> {
                  this.property4.getPossibleValues().forEach((p_176398_) -> {
                     this.property5.getPossibleValues().forEach((p_176392_) -> {
                        this.select((T1)p_176412_, (T2)p_176407_, (T3)p_176403_, (T4)p_176398_, p_176392_, pPropertyValuesToVariantsMapper.apply((T1)p_176412_, (T2)p_176407_, (T3)p_176403_, (T4)p_176398_, p_176392_));
                     });
                  });
               });
            });
         });
         return this;
      }
   }

   @FunctionalInterface
   public interface PentaFunction<P1, P2, P3, P4, P5, R> {
      R apply(P1 pP1, P2 pP2, P3 pP3, P4 pP4, P5 pP5);
   }

   @FunctionalInterface
   public interface QuadFunction<P1, P2, P3, P4, R> {
      R apply(P1 pP1, P2 pP2, P3 pP3, P4 pP4);
   }

   @FunctionalInterface
   public interface TriFunction<P1, P2, P3, R> {
      R apply(P1 pP1, P2 pP2, P3 pP3);
   }
}