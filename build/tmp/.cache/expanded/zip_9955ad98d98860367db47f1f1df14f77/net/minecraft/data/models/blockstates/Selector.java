package net.minecraft.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.world.level.block.state.properties.Property;

public final class Selector {
   private static final Selector EMPTY = new Selector(ImmutableList.of());
   private static final Comparator<Property.Value<?>> COMPARE_BY_NAME = Comparator.comparing((p_125494_) -> {
      return p_125494_.property().getName();
   });
   private final List<Property.Value<?>> values;

   public Selector extend(Property.Value<?> pValue) {
      return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).add(pValue).build());
   }

   public Selector extend(Selector pSelector) {
      return new Selector(ImmutableList.<Property.Value<?>>builder().addAll(this.values).addAll(pSelector.values).build());
   }

   private Selector(List<Property.Value<?>> pValues) {
      this.values = pValues;
   }

   public static Selector empty() {
      return EMPTY;
   }

   public static Selector of(Property.Value<?>... pValues) {
      return new Selector(ImmutableList.copyOf(pValues));
   }

   public boolean equals(Object pOther) {
      return this == pOther || pOther instanceof Selector && this.values.equals(((Selector)pOther).values);
   }

   public int hashCode() {
      return this.values.hashCode();
   }

   public String getKey() {
      return this.values.stream().sorted(COMPARE_BY_NAME).map(Property.Value::toString).collect(Collectors.joining(","));
   }

   public String toString() {
      return this.getKey();
   }
}