package net.minecraft.stats;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.entity.player.Player;

/**
 * Manages counting a set of {@link net.minecraft.stats.Stat} objects, stored by a map of statistics to their count.
 * <p>
 * This base {@code StatsCounter} is only used client-side for keeping track of and reading counts sent from the server.
 * 
 * @see net.minecraft.stats.ServerStatsCounter
 */
public class StatsCounter {
   protected final Object2IntMap<Stat<?>> stats = Object2IntMaps.synchronize(new Object2IntOpenHashMap<>());

   public StatsCounter() {
      this.stats.defaultReturnValue(0);
   }

   public void increment(Player pPlayer, Stat<?> pStat, int pAmount) {
      int i = (int)Math.min((long)this.getValue(pStat) + (long)pAmount, 2147483647L);
      this.setValue(pPlayer, pStat, i);
   }

   public void setValue(Player pPlayer, Stat<?> pStat, int pValue) {
      this.stats.put(pStat, pValue);
   }

   public <T> int getValue(StatType<T> pType, T pValue) {
      return pType.contains(pValue) ? this.getValue(pType.get(pValue)) : 0;
   }

   public int getValue(Stat<?> pStat) {
      return this.stats.getInt(pStat);
   }
}