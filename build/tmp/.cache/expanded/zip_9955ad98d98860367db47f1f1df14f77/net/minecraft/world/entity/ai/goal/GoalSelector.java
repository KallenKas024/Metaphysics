package net.minecraft.world.entity.ai.goal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class GoalSelector {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final WrappedGoal NO_GOAL = new WrappedGoal(Integer.MAX_VALUE, new Goal() {
      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return false;
      }
   }) {
      public boolean isRunning() {
         return false;
      }
   };
   /** Goals currently using a particular flag */
   private final Map<Goal.Flag, WrappedGoal> lockedFlags = new EnumMap<>(Goal.Flag.class);
   private final Set<WrappedGoal> availableGoals = Sets.newLinkedHashSet();
   private final Supplier<ProfilerFiller> profiler;
   private final EnumSet<Goal.Flag> disabledFlags = EnumSet.noneOf(Goal.Flag.class);
   private int tickCount;
   private int newGoalRate = 3;

   public GoalSelector(Supplier<ProfilerFiller> pProfiler) {
      this.profiler = pProfiler;
   }

   /**
    * Add a goal to the GoalSelector with a certain priority. Lower numbers are higher priority.
    */
   public void addGoal(int pPriority, Goal pGoal) {
      this.availableGoals.add(new WrappedGoal(pPriority, pGoal));
   }

   @VisibleForTesting
   public void removeAllGoals(Predicate<Goal> pFilter) {
      this.availableGoals.removeIf((p_262564_) -> {
         return pFilter.test(p_262564_.getGoal());
      });
   }

   /**
    * Remove the goal from the GoalSelector. This must be the same object as the goal you are trying to remove, which
    * may not always be accessible.
    */
   public void removeGoal(Goal pGoal) {
      this.availableGoals.stream().filter((p_25378_) -> {
         return p_25378_.getGoal() == pGoal;
      }).filter(WrappedGoal::isRunning).forEach(WrappedGoal::stop);
      this.availableGoals.removeIf((p_25367_) -> {
         return p_25367_.getGoal() == pGoal;
      });
   }

   private static boolean goalContainsAnyFlags(WrappedGoal pGoal, EnumSet<Goal.Flag> pFlag) {
      for(Goal.Flag goal$flag : pGoal.getFlags()) {
         if (pFlag.contains(goal$flag)) {
            return true;
         }
      }

      return false;
   }

   private static boolean goalCanBeReplacedForAllFlags(WrappedGoal pGoal, Map<Goal.Flag, WrappedGoal> pFlag) {
      for(Goal.Flag goal$flag : pGoal.getFlags()) {
         if (!pFlag.getOrDefault(goal$flag, NO_GOAL).canBeReplacedBy(pGoal)) {
            return false;
         }
      }

      return true;
   }

   /**
    * Ticks every goal in the selector.
    * Attempts to start each goal based on if it can be used, or stop it if it can't.
    */
   public void tick() {
      ProfilerFiller profilerfiller = this.profiler.get();
      profilerfiller.push("goalCleanup");

      for(WrappedGoal wrappedgoal : this.availableGoals) {
         if (wrappedgoal.isRunning() && (goalContainsAnyFlags(wrappedgoal, this.disabledFlags) || !wrappedgoal.canContinueToUse())) {
            wrappedgoal.stop();
         }
      }

      Iterator<Map.Entry<Goal.Flag, WrappedGoal>> iterator = this.lockedFlags.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<Goal.Flag, WrappedGoal> entry = iterator.next();
         if (!entry.getValue().isRunning()) {
            iterator.remove();
         }
      }

      profilerfiller.pop();
      profilerfiller.push("goalUpdate");

      for(WrappedGoal wrappedgoal2 : this.availableGoals) {
         if (!wrappedgoal2.isRunning() && !goalContainsAnyFlags(wrappedgoal2, this.disabledFlags) && goalCanBeReplacedForAllFlags(wrappedgoal2, this.lockedFlags) && wrappedgoal2.canUse()) {
            for(Goal.Flag goal$flag : wrappedgoal2.getFlags()) {
               WrappedGoal wrappedgoal1 = this.lockedFlags.getOrDefault(goal$flag, NO_GOAL);
               wrappedgoal1.stop();
               this.lockedFlags.put(goal$flag, wrappedgoal2);
            }

            wrappedgoal2.start();
         }
      }

      profilerfiller.pop();
      this.tickRunningGoals(true);
   }

   public void tickRunningGoals(boolean pTickAllRunning) {
      ProfilerFiller profilerfiller = this.profiler.get();
      profilerfiller.push("goalTick");

      for(WrappedGoal wrappedgoal : this.availableGoals) {
         if (wrappedgoal.isRunning() && (pTickAllRunning || wrappedgoal.requiresUpdateEveryTick())) {
            wrappedgoal.tick();
         }
      }

      profilerfiller.pop();
   }

   public Set<WrappedGoal> getAvailableGoals() {
      return this.availableGoals;
   }

   public Stream<WrappedGoal> getRunningGoals() {
      return this.availableGoals.stream().filter(WrappedGoal::isRunning);
   }

   public void setNewGoalRate(int pNewGoalRate) {
      this.newGoalRate = pNewGoalRate;
   }

   public void disableControlFlag(Goal.Flag pFlag) {
      this.disabledFlags.add(pFlag);
   }

   public void enableControlFlag(Goal.Flag pFlag) {
      this.disabledFlags.remove(pFlag);
   }

   public void setControlFlag(Goal.Flag pFlag, boolean pEnabled) {
      if (pEnabled) {
         this.enableControlFlag(pFlag);
      } else {
         this.disableControlFlag(pFlag);
      }

   }
}