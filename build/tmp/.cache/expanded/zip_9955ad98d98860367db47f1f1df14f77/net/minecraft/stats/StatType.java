package net.minecraft.stats;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

/**
 * Holds a map of {@linkplain net.minecraft.stats.Stat statistics} with type {@code T} for a corresponding {@link
 * #registry}.
 * <p>
 * A single type usually defines a particular thing to be counted, such as {@linkplain
 * net.minecraft.stats.Stats#ITEM_USED the number of items used} or {@link net.minecraft.stats.Stats#BLOCK_MINED the
 * number of blocks mined}. However, there is also a {@link net.minecraft.stats.Stats#CUSTOM custom type} which uses
 * entries from the {@linkplain Registry#CUSTOM_STAT custom stat registry}. This is keyed by a {@link
 * net.minecraft.resources.ResourceLocation} and can be used to count any statistic that doesn't require an associated
 * {@link net.minecraft.core.Registry} entry.
 * 
 * @param <T> the type of the associated registry's entry values
 * @see net.minecraft.stats.Stat
 * @see net.minecraft.stats.Stats
 * @see net.minecraft.core.Registry#STAT_TYPE
 * @see net.minecraft.core.Registry#CUSTOM_STAT
 */
public class StatType<T> implements Iterable<Stat<T>> {
   private final Registry<T> registry;
   /**
    * A map of registry entries to their corresponding {@link Stat statistic}. Lazily populated by {@link #get(Object,
    * StatFormatter)}.
    */
   private final Map<T, Stat<T>> map = new IdentityHashMap<>();
   @Nullable
   private Component displayName;

   public StatType(Registry<T> pRegistry) {
      this.registry = pRegistry;
   }

   public boolean contains(T pValue) {
      return this.map.containsKey(pValue);
   }

   public Stat<T> get(T pValue, StatFormatter pFormatter) {
      return this.map.computeIfAbsent(pValue, (p_12896_) -> {
         return new Stat<>(this, p_12896_, pFormatter);
      });
   }

   public Registry<T> getRegistry() {
      return this.registry;
   }

   public Iterator<Stat<T>> iterator() {
      return this.map.values().iterator();
   }

   public Stat<T> get(T pValue) {
      return this.get(pValue, StatFormatter.DEFAULT);
   }

   public String getTranslationKey() {
      return "stat_type." + BuiltInRegistries.STAT_TYPE.getKey(this).toString().replace(':', '.');
   }

   public Component getDisplayName() {
      if (this.displayName == null) {
         this.displayName = Component.translatable(this.getTranslationKey());
      }

      return this.displayName;
   }
}