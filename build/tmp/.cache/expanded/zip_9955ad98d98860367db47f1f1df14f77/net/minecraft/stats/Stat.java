package net.minecraft.stats;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * An immutable statistic to be counted for a particular entry in the {@linkplain #type}'s registry. This is used as a
 * key in a {@link net.minecraft.stats.StatsCounter} for a corresponding count.
 * <p>
 * By default, the statistic's {@linkplain #getName() name} is formatted {@code <stat type namespace>.<stat type
 * path>:<value namespace>.<value path>}, as created by {@link #buildName(StatType, Object)}.
 * 
 * @param <T> the type of the registry entry for this statistic
 * @see net.minecraft.stats.StatType
 * @see net.minecraft.stats.Stats
 */
public class Stat<T> extends ObjectiveCriteria {
   private final StatFormatter formatter;
   /** The registry entry for this statistic. */
   private final T value;
   /** The parent statistic type. */
   private final StatType<T> type;

   protected Stat(StatType<T> pType, T pValue, StatFormatter pFormatter) {
      super(buildName(pType, pValue));
      this.type = pType;
      this.formatter = pFormatter;
      this.value = pValue;
   }

   /**
    * @return the name for the specified {@code type} and {@code value} in the form {@code <stat type namespace>.<stat
    * type path>:<value namespace>.<value path>}
    */
   public static <T> String buildName(StatType<T> pType, T pValue) {
      return locationToKey(BuiltInRegistries.STAT_TYPE.getKey(pType)) + ":" + locationToKey(pType.getRegistry().getKey(pValue));
   }

   /**
    * @return the specified {@code location} as a string with {@code .} as the separator character
    */
   private static <T> String locationToKey(@Nullable ResourceLocation pLocation) {
      return pLocation.toString().replace(':', '.');
   }

   public StatType<T> getType() {
      return this.type;
   }

   public T getValue() {
      return this.value;
   }

   public String format(int pValue) {
      return this.formatter.format(pValue);
   }

   public boolean equals(Object pOther) {
      return this == pOther || pOther instanceof Stat && Objects.equals(this.getName(), ((Stat)pOther).getName());
   }

   public int hashCode() {
      return this.getName().hashCode();
   }

   public String toString() {
      return "Stat{name=" + this.getName() + ", formatter=" + this.formatter + "}";
   }
}