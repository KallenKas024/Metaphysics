package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class IntegerProperty extends Property<Integer> {
   private final ImmutableSet<Integer> values;
   private final int min;
   private final int max;

   protected IntegerProperty(String pName, int pMin, int pMax) {
      super(pName, Integer.class);
      if (pMin < 0) {
         throw new IllegalArgumentException("Min value of " + pName + " must be 0 or greater");
      } else if (pMax <= pMin) {
         throw new IllegalArgumentException("Max value of " + pName + " must be greater than min (" + pMin + ")");
      } else {
         this.min = pMin;
         this.max = pMax;
         Set<Integer> set = Sets.newHashSet();

         for(int i = pMin; i <= pMax; ++i) {
            set.add(i);
         }

         this.values = ImmutableSet.copyOf(set);
      }
   }

   public Collection<Integer> getPossibleValues() {
      return this.values;
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (pOther instanceof IntegerProperty && super.equals(pOther)) {
         IntegerProperty integerproperty = (IntegerProperty)pOther;
         return this.values.equals(integerproperty.values);
      } else {
         return false;
      }
   }

   public int generateHashCode() {
      return 31 * super.generateHashCode() + this.values.hashCode();
   }

   public static IntegerProperty create(String pName, int pMin, int pMax) {
      return new IntegerProperty(pName, pMin, pMax);
   }

   public Optional<Integer> getValue(String pValue) {
      try {
         Integer integer = Integer.valueOf(pValue);
         return integer >= this.min && integer <= this.max ? Optional.of(integer) : Optional.empty();
      } catch (NumberFormatException numberformatexception) {
         return Optional.empty();
      }
   }

   /**
    * @return the name for the given value.
    */
   public String getName(Integer pValue) {
      return pValue.toString();
   }
}