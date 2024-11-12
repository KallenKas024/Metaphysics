package net.minecraft.world.level.block.state.properties;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.StateHolder;

public abstract class Property<T extends Comparable<T>> {
   private final Class<T> clazz;
   private final String name;
   @Nullable
   private Integer hashCode;
   private final Codec<T> codec = Codec.STRING.comapFlatMap((p_61698_) -> {
      return this.getValue(p_61698_).map(DataResult::success).orElseGet(() -> {
         return DataResult.error(() -> {
            return "Unable to read property: " + this + " with value: " + p_61698_;
         });
      });
   }, this::getName);
   private final Codec<Property.Value<T>> valueCodec = this.codec.xmap(this::value, Property.Value::value);

   protected Property(String pName, Class<T> pClazz) {
      this.clazz = pClazz;
      this.name = pName;
   }

   public Property.Value<T> value(T p_61700_) {
      return new Property.Value<>(this, p_61700_);
   }

   public Property.Value<T> value(StateHolder<?, ?> pHolder) {
      return new Property.Value<>(this, pHolder.getValue(this));
   }

   public Stream<Property.Value<T>> getAllValues() {
      return this.getPossibleValues().stream().map(this::value);
   }

   public Codec<T> codec() {
      return this.codec;
   }

   public Codec<Property.Value<T>> valueCodec() {
      return this.valueCodec;
   }

   public String getName() {
      return this.name;
   }

   /**
    * @return the class of the values of this property
    */
   public Class<T> getValueClass() {
      return this.clazz;
   }

   public abstract Collection<T> getPossibleValues();

   /**
    * @return the name for the given value.
    */
   public abstract String getName(T p_61696_);

   public abstract Optional<T> getValue(String pValue);

   public String toString() {
      return MoreObjects.toStringHelper(this).add("name", this.name).add("clazz", this.clazz).add("values", this.getPossibleValues()).toString();
   }

   public boolean equals(Object pOther) {
      if (this == pOther) {
         return true;
      } else if (!(pOther instanceof Property)) {
         return false;
      } else {
         Property<?> property = (Property)pOther;
         return this.clazz.equals(property.clazz) && this.name.equals(property.name);
      }
   }

   public final int hashCode() {
      if (this.hashCode == null) {
         this.hashCode = this.generateHashCode();
      }

      return this.hashCode;
   }

   public int generateHashCode() {
      return 31 * this.clazz.hashCode() + this.name.hashCode();
   }

   public <U, S extends StateHolder<?, S>> DataResult<S> parseValue(DynamicOps<U> pOps, S pStateHolder, U pUnparsedValue) {
      DataResult<T> dataresult = this.codec.parse(pOps, pUnparsedValue);
      return dataresult.map((p_156030_) -> {
         return pStateHolder.setValue(this, p_156030_);
      }).setPartial(pStateHolder);
   }

   public static record Value<T extends Comparable<T>>(Property<T> property, T value) {
      public Value {
         if (!property.getPossibleValues().contains(value)) {
            throw new IllegalArgumentException("Value " + value + " does not belong to property " + property);
         }
      }

      public String toString() {
         return this.property.getName() + "=" + this.property.getName(this.value);
      }
   }
}