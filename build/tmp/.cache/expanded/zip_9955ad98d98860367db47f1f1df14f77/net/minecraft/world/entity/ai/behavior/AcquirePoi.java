package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableLong;

public class AcquirePoi {
   public static final int SCAN_RANGE = 48;

   public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> pAcquirablePois, MemoryModuleType<GlobalPos> pAcquiringMemory, boolean pOnlyIfAdult, Optional<Byte> pEntityEventId) {
      return create(pAcquirablePois, pAcquiringMemory, pAcquiringMemory, pOnlyIfAdult, pEntityEventId);
   }

   public static BehaviorControl<PathfinderMob> create(Predicate<Holder<PoiType>> pAcquirablePois, MemoryModuleType<GlobalPos> pExistingAbsentMemory, MemoryModuleType<GlobalPos> pAcquiringMemory, boolean pOnlyIfAdult, Optional<Byte> pEntityEventId) {
      int i = 5;
      int j = 20;
      MutableLong mutablelong = new MutableLong(0L);
      Long2ObjectMap<AcquirePoi.JitteredLinearRetry> long2objectmap = new Long2ObjectOpenHashMap<>();
      OneShot<PathfinderMob> oneshot = BehaviorBuilder.create((p_258276_) -> {
         return p_258276_.group(p_258276_.absent(pAcquiringMemory)).apply(p_258276_, (p_258300_) -> {
            return (p_258292_, p_258293_, p_258294_) -> {
               if (pOnlyIfAdult && p_258293_.isBaby()) {
                  return false;
               } else if (mutablelong.getValue() == 0L) {
                  mutablelong.setValue(p_258292_.getGameTime() + (long)p_258292_.random.nextInt(20));
                  return false;
               } else if (p_258292_.getGameTime() < mutablelong.getValue()) {
                  return false;
               } else {
                  mutablelong.setValue(p_258294_ + 20L + (long)p_258292_.getRandom().nextInt(20));
                  PoiManager poimanager = p_258292_.getPoiManager();
                  long2objectmap.long2ObjectEntrySet().removeIf((p_22338_) -> {
                     return !p_22338_.getValue().isStillValid(p_258294_);
                  });
                  Predicate<BlockPos> predicate = (p_258266_) -> {
                     AcquirePoi.JitteredLinearRetry acquirepoi$jitteredlinearretry = long2objectmap.get(p_258266_.asLong());
                     if (acquirepoi$jitteredlinearretry == null) {
                        return true;
                     } else if (!acquirepoi$jitteredlinearretry.shouldRetry(p_258294_)) {
                        return false;
                     } else {
                        acquirepoi$jitteredlinearretry.markAttempt(p_258294_);
                        return true;
                     }
                  };
                  Set<Pair<Holder<PoiType>, BlockPos>> set = poimanager.findAllClosestFirstWithType(pAcquirablePois, predicate, p_258293_.blockPosition(), 48, PoiManager.Occupancy.HAS_SPACE).limit(5L).collect(Collectors.toSet());
                  Path path = findPathToPois(p_258293_, set);
                  if (path != null && path.canReach()) {
                     BlockPos blockpos = path.getTarget();
                     poimanager.getType(blockpos).ifPresent((p_288780_) -> {
                        poimanager.take(pAcquirablePois, (p_217108_, p_217109_) -> {
                           return p_217109_.equals(blockpos);
                        }, blockpos, 1);
                        p_258300_.set(GlobalPos.of(p_258292_.dimension(), blockpos));
                        pEntityEventId.ifPresent((p_147369_) -> {
                           p_258292_.broadcastEntityEvent(p_258293_, p_147369_);
                        });
                        long2objectmap.clear();
                        DebugPackets.sendPoiTicketCountPacket(p_258292_, blockpos);
                     });
                  } else {
                     for(Pair<Holder<PoiType>, BlockPos> pair : set) {
                        long2objectmap.computeIfAbsent(pair.getSecond().asLong(), (p_264881_) -> {
                           return new AcquirePoi.JitteredLinearRetry(p_258292_.random, p_258294_);
                        });
                     }
                  }

                  return true;
               }
            };
         });
      });
      return pAcquiringMemory == pExistingAbsentMemory ? oneshot : BehaviorBuilder.create((p_258269_) -> {
         return p_258269_.group(p_258269_.absent(pExistingAbsentMemory)).apply(p_258269_, (p_258302_) -> {
            return oneshot;
         });
      });
   }

   @Nullable
   public static Path findPathToPois(Mob pMob, Set<Pair<Holder<PoiType>, BlockPos>> pPoiPositions) {
      if (pPoiPositions.isEmpty()) {
         return null;
      } else {
         Set<BlockPos> set = new HashSet<>();
         int i = 1;

         for(Pair<Holder<PoiType>, BlockPos> pair : pPoiPositions) {
            i = Math.max(i, pair.getFirst().value().validRange());
            set.add(pair.getSecond());
         }

         return pMob.getNavigation().createPath(set, i);
      }
   }

   static class JitteredLinearRetry {
      private static final int MIN_INTERVAL_INCREASE = 40;
      private static final int MAX_INTERVAL_INCREASE = 80;
      private static final int MAX_RETRY_PATHFINDING_INTERVAL = 400;
      private final RandomSource random;
      private long previousAttemptTimestamp;
      private long nextScheduledAttemptTimestamp;
      private int currentDelay;

      JitteredLinearRetry(RandomSource pRandom, long pTimestamp) {
         this.random = pRandom;
         this.markAttempt(pTimestamp);
      }

      public void markAttempt(long pTimestamp) {
         this.previousAttemptTimestamp = pTimestamp;
         int i = this.currentDelay + this.random.nextInt(40) + 40;
         this.currentDelay = Math.min(i, 400);
         this.nextScheduledAttemptTimestamp = pTimestamp + (long)this.currentDelay;
      }

      public boolean isStillValid(long pTimestamp) {
         return pTimestamp - this.previousAttemptTimestamp < 400L;
      }

      public boolean shouldRetry(long pTimestamp) {
         return pTimestamp >= this.nextScheduledAttemptTimestamp;
      }

      public String toString() {
         return "RetryMarker{, previousAttemptAt=" + this.previousAttemptTimestamp + ", nextScheduledAttemptAt=" + this.nextScheduledAttemptTimestamp + ", currentDelay=" + this.currentDelay + "}";
      }
   }
}