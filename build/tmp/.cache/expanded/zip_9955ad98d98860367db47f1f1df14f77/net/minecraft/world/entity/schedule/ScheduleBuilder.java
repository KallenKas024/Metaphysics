package net.minecraft.world.entity.schedule;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleBuilder {
   private final Schedule schedule;
   private final List<ScheduleBuilder.ActivityTransition> transitions = Lists.newArrayList();

   public ScheduleBuilder(Schedule pSchedule) {
      this.schedule = pSchedule;
   }

   public ScheduleBuilder changeActivityAt(int pDuration, Activity pActivity) {
      this.transitions.add(new ScheduleBuilder.ActivityTransition(pDuration, pActivity));
      return this;
   }

   public Schedule build() {
      this.transitions.stream().map(ScheduleBuilder.ActivityTransition::getActivity).collect(Collectors.toSet()).forEach(this.schedule::ensureTimelineExistsFor);
      this.transitions.forEach((p_38044_) -> {
         Activity activity = p_38044_.getActivity();
         this.schedule.getAllTimelinesExceptFor(activity).forEach((p_150245_) -> {
            p_150245_.addKeyframe(p_38044_.getTime(), 0.0F);
         });
         this.schedule.getTimelineFor(activity).addKeyframe(p_38044_.getTime(), 1.0F);
      });
      return this.schedule;
   }

   static class ActivityTransition {
      private final int time;
      private final Activity activity;

      public ActivityTransition(int pTime, Activity pActivity) {
         this.time = pTime;
         this.activity = pActivity;
      }

      public int getTime() {
         return this.time;
      }

      public Activity getActivity() {
         return this.activity;
      }
   }
}