package net.minecraft.util.profiling;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class FilledProfileResults implements ProfileResults {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final ProfilerPathEntry EMPTY = new ProfilerPathEntry() {
      public long getDuration() {
         return 0L;
      }

      public long getMaxDuration() {
         return 0L;
      }

      public long getCount() {
         return 0L;
      }

      public Object2LongMap<String> getCounters() {
         return Object2LongMaps.emptyMap();
      }
   };
   private static final Splitter SPLITTER = Splitter.on('\u001e');
   private static final Comparator<Map.Entry<String, FilledProfileResults.CounterCollector>> COUNTER_ENTRY_COMPARATOR = Entry.<String, FilledProfileResults.CounterCollector>comparingByValue(Comparator.comparingLong((p_18489_) -> {
      return p_18489_.totalValue;
   })).reversed();
   private final Map<String, ? extends ProfilerPathEntry> entries;
   private final long startTimeNano;
   private final int startTimeTicks;
   private final long endTimeNano;
   private final int endTimeTicks;
   private final int tickDuration;

   public FilledProfileResults(Map<String, ? extends ProfilerPathEntry> pEntries, long pStartTimeNano, int pStartTimeTicks, long pEndTimeNano, int pEndTimeTicks) {
      this.entries = pEntries;
      this.startTimeNano = pStartTimeNano;
      this.startTimeTicks = pStartTimeTicks;
      this.endTimeNano = pEndTimeNano;
      this.endTimeTicks = pEndTimeTicks;
      this.tickDuration = pEndTimeTicks - pStartTimeTicks;
   }

   private ProfilerPathEntry getEntry(String pKey) {
      ProfilerPathEntry profilerpathentry = this.entries.get(pKey);
      return profilerpathentry != null ? profilerpathentry : EMPTY;
   }

   public List<ResultField> getTimes(String pSectionPath) {
      String s = pSectionPath;
      ProfilerPathEntry profilerpathentry = this.getEntry("root");
      long i = profilerpathentry.getDuration();
      ProfilerPathEntry profilerpathentry1 = this.getEntry(pSectionPath);
      long j = profilerpathentry1.getDuration();
      long k = profilerpathentry1.getCount();
      List<ResultField> list = Lists.newArrayList();
      if (!pSectionPath.isEmpty()) {
         pSectionPath = pSectionPath + "\u001e";
      }

      long l = 0L;

      for(String s1 : this.entries.keySet()) {
         if (isDirectChild(pSectionPath, s1)) {
            l += this.getEntry(s1).getDuration();
         }
      }

      float f = (float)l;
      if (l < j) {
         l = j;
      }

      if (i < l) {
         i = l;
      }

      for(String s2 : this.entries.keySet()) {
         if (isDirectChild(pSectionPath, s2)) {
            ProfilerPathEntry profilerpathentry2 = this.getEntry(s2);
            long i1 = profilerpathentry2.getDuration();
            double d0 = (double)i1 * 100.0D / (double)l;
            double d1 = (double)i1 * 100.0D / (double)i;
            String s3 = s2.substring(pSectionPath.length());
            list.add(new ResultField(s3, d0, d1, profilerpathentry2.getCount()));
         }
      }

      if ((float)l > f) {
         list.add(new ResultField("unspecified", (double)((float)l - f) * 100.0D / (double)l, (double)((float)l - f) * 100.0D / (double)i, k));
      }

      Collections.sort(list);
      list.add(0, new ResultField(s, 100.0D, (double)l * 100.0D / (double)i, k));
      return list;
   }

   private static boolean isDirectChild(String pSectionPath, String pEntry) {
      return pEntry.length() > pSectionPath.length() && pEntry.startsWith(pSectionPath) && pEntry.indexOf(30, pSectionPath.length() + 1) < 0;
   }

   private Map<String, FilledProfileResults.CounterCollector> getCounterValues() {
      Map<String, FilledProfileResults.CounterCollector> map = Maps.newTreeMap();
      this.entries.forEach((p_18512_, p_18513_) -> {
         Object2LongMap<String> object2longmap = p_18513_.getCounters();
         if (!object2longmap.isEmpty()) {
            List<String> list = SPLITTER.splitToList(p_18512_);
            object2longmap.forEach((p_145944_, p_145945_) -> {
               map.computeIfAbsent(p_145944_, (p_145947_) -> {
                  return new FilledProfileResults.CounterCollector();
               }).addValue(list.iterator(), p_145945_);
            });
         }

      });
      return map;
   }

   public long getStartTimeNano() {
      return this.startTimeNano;
   }

   public int getStartTimeTicks() {
      return this.startTimeTicks;
   }

   public long getEndTimeNano() {
      return this.endTimeNano;
   }

   public int getEndTimeTicks() {
      return this.endTimeTicks;
   }

   public boolean saveResults(Path pPath) {
      Writer writer = null;

      boolean flag;
      try {
         Files.createDirectories(pPath.getParent());
         writer = Files.newBufferedWriter(pPath, StandardCharsets.UTF_8);
         writer.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
         return true;
      } catch (Throwable throwable) {
         LOGGER.error("Could not save profiler results to {}", pPath, throwable);
         flag = false;
      } finally {
         IOUtils.closeQuietly(writer);
      }

      return flag;
   }

   protected String getProfilerResults(long pTimeSpan, int pTickSpan) {
      StringBuilder stringbuilder = new StringBuilder();
      stringbuilder.append("---- Minecraft Profiler Results ----\n");
      stringbuilder.append("// ");
      stringbuilder.append(getComment());
      stringbuilder.append("\n\n");
      stringbuilder.append("Version: ").append(SharedConstants.getCurrentVersion().getId()).append('\n');
      stringbuilder.append("Time span: ").append(pTimeSpan / 1000000L).append(" ms\n");
      stringbuilder.append("Tick span: ").append(pTickSpan).append(" ticks\n");
      stringbuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", (float)pTickSpan / ((float)pTimeSpan / 1.0E9F))).append(" ticks per second. It should be ").append((int)20).append(" ticks per second\n\n");
      stringbuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
      this.appendProfilerResults(0, "root", stringbuilder);
      stringbuilder.append("--- END PROFILE DUMP ---\n\n");
      Map<String, FilledProfileResults.CounterCollector> map = this.getCounterValues();
      if (!map.isEmpty()) {
         stringbuilder.append("--- BEGIN COUNTER DUMP ---\n\n");
         this.appendCounters(map, stringbuilder, pTickSpan);
         stringbuilder.append("--- END COUNTER DUMP ---\n\n");
      }

      return stringbuilder.toString();
   }

   public String getProfilerResults() {
      StringBuilder stringbuilder = new StringBuilder();
      this.appendProfilerResults(0, "root", stringbuilder);
      return stringbuilder.toString();
   }

   private static StringBuilder indentLine(StringBuilder pBuilder, int pIndents) {
      pBuilder.append(String.format(Locale.ROOT, "[%02d] ", pIndents));

      for(int i = 0; i < pIndents; ++i) {
         pBuilder.append("|   ");
      }

      return pBuilder;
   }

   private void appendProfilerResults(int pDepth, String pSectionPath, StringBuilder pBuilder) {
      List<ResultField> list = this.getTimes(pSectionPath);
      Object2LongMap<String> object2longmap = ObjectUtils.firstNonNull(this.entries.get(pSectionPath), EMPTY).getCounters();
      object2longmap.forEach((p_18508_, p_18509_) -> {
         indentLine(pBuilder, pDepth).append('#').append(p_18508_).append(' ').append((Object)p_18509_).append('/').append(p_18509_ / (long)this.tickDuration).append('\n');
      });
      if (list.size() >= 3) {
         for(int i = 1; i < list.size(); ++i) {
            ResultField resultfield = list.get(i);
            indentLine(pBuilder, pDepth).append(resultfield.name).append('(').append(resultfield.count).append('/').append(String.format(Locale.ROOT, "%.0f", (float)resultfield.count / (float)this.tickDuration)).append(')').append(" - ").append(String.format(Locale.ROOT, "%.2f", resultfield.percentage)).append("%/").append(String.format(Locale.ROOT, "%.2f", resultfield.globalPercentage)).append("%\n");
            if (!"unspecified".equals(resultfield.name)) {
               try {
                  this.appendProfilerResults(pDepth + 1, pSectionPath + "\u001e" + resultfield.name, pBuilder);
               } catch (Exception exception) {
                  pBuilder.append("[[ EXCEPTION ").append((Object)exception).append(" ]]");
               }
            }
         }

      }
   }

   private void appendCounterResults(int pIndents, String pName, FilledProfileResults.CounterCollector pCollector, int pTickSpan, StringBuilder pBuilder) {
      indentLine(pBuilder, pIndents).append(pName).append(" total:").append(pCollector.selfValue).append('/').append(pCollector.totalValue).append(" average: ").append(pCollector.selfValue / (long)pTickSpan).append('/').append(pCollector.totalValue / (long)pTickSpan).append('\n');
      pCollector.children.entrySet().stream().sorted(COUNTER_ENTRY_COMPARATOR).forEach((p_18474_) -> {
         this.appendCounterResults(pIndents + 1, p_18474_.getKey(), p_18474_.getValue(), pTickSpan, pBuilder);
      });
   }

   private void appendCounters(Map<String, FilledProfileResults.CounterCollector> pCounters, StringBuilder pBuilder, int pTickSpan) {
      pCounters.forEach((p_18503_, p_18504_) -> {
         pBuilder.append("-- Counter: ").append(p_18503_).append(" --\n");
         this.appendCounterResults(0, "root", p_18504_.children.get("root"), pTickSpan, pBuilder);
         pBuilder.append("\n\n");
      });
   }

   private static String getComment() {
      String[] astring = new String[]{"I'd Rather Be Surfing", "Shiny numbers!", "Am I not running fast enough? :(", "I'm working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!", "Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers", "Now with the same numbers", "You should add flames to things, it makes them go faster!", "Do you feel the need for... optimization?", "*cracks redstone whip*", "Maybe if you treated it better then it'll have more motivation to work faster! Poor server."};

      try {
         return astring[(int)(Util.getNanos() % (long)astring.length)];
      } catch (Throwable throwable) {
         return "Witty comment unavailable :(";
      }
   }

   public int getTickDuration() {
      return this.tickDuration;
   }

   static class CounterCollector {
      long selfValue;
      long totalValue;
      final Map<String, FilledProfileResults.CounterCollector> children = Maps.newHashMap();

      public void addValue(Iterator<String> p_18548_, long p_18549_) {
         this.totalValue += p_18549_;
         if (!p_18548_.hasNext()) {
            this.selfValue += p_18549_;
         } else {
            this.children.computeIfAbsent(p_18548_.next(), (p_18546_) -> {
               return new FilledProfileResults.CounterCollector();
            }).addValue(p_18548_, p_18549_);
         }

      }
   }
}