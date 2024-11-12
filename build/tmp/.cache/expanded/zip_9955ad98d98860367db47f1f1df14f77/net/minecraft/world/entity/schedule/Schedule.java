package net.minecraft.world.entity.schedule;

import com.google.common.collect.Maps;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class Schedule {
   public static final int WORK_START_TIME = 2000;
   public static final int TOTAL_WORK_TIME = 7000;
   public static final Schedule EMPTY = register("empty").changeActivityAt(0, Activity.IDLE).build();
   public static final Schedule SIMPLE = register("simple").changeActivityAt(5000, Activity.WORK).changeActivityAt(11000, Activity.REST).build();
   public static final Schedule VILLAGER_BABY = register("villager_baby").changeActivityAt(10, Activity.IDLE).changeActivityAt(3000, Activity.PLAY).changeActivityAt(6000, Activity.IDLE).changeActivityAt(10000, Activity.PLAY).changeActivityAt(12000, Activity.REST).build();
   public static final Schedule VILLAGER_DEFAULT = register("villager_default").changeActivityAt(10, Activity.IDLE).changeActivityAt(2000, Activity.WORK).changeActivityAt(9000, Activity.MEET).changeActivityAt(11000, Activity.IDLE).changeActivityAt(12000, Activity.REST).build();
   private final Map<Activity, Timeline> timelines = Maps.newHashMap();

   protected static ScheduleBuilder register(String pKey) {
      Schedule schedule = Registry.register(BuiltInRegistries.SCHEDULE, pKey, new Schedule());
      return new ScheduleBuilder(schedule);
   }

   protected void ensureTimelineExistsFor(Activity pActivity) {
      if (!this.timelines.containsKey(pActivity)) {
         this.timelines.put(pActivity, new Timeline());
      }

   }

   protected Timeline getTimelineFor(Activity pActivity) {
      return this.timelines.get(pActivity);
   }

   protected List<Timeline> getAllTimelinesExceptFor(Activity pActivity) {
      return this.timelines.entrySet().stream().filter((p_38028_) -> {
         return p_38028_.getKey() != pActivity;
      }).map(Map.Entry::getValue).collect(Collectors.toList());
   }

   public Activity getActivityAt(int pDayTime) {
      return this.timelines.entrySet().stream().max(Comparator.comparingDouble((p_38023_) -> {
         return (double)p_38023_.getValue().getValueAt(pDayTime);
      })).map(Map.Entry::getKey).orElse(Activity.IDLE);
   }
}