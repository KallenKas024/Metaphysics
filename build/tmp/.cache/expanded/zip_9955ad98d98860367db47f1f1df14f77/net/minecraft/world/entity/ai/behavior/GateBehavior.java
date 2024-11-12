package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity> implements BehaviorControl<E> {
   private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
   private final Set<MemoryModuleType<?>> exitErasedMemories;
   private final GateBehavior.OrderPolicy orderPolicy;
   private final GateBehavior.RunningPolicy runningPolicy;
   private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList<>();
   private Behavior.Status status = Behavior.Status.STOPPED;

   public GateBehavior(Map<MemoryModuleType<?>, MemoryStatus> pEntryCondition, Set<MemoryModuleType<?>> pExitErasedMemories, GateBehavior.OrderPolicy pOrderPolicy, GateBehavior.RunningPolicy pRunningPolicy, List<Pair<? extends BehaviorControl<? super E>, Integer>> pDurations) {
      this.entryCondition = pEntryCondition;
      this.exitErasedMemories = pExitErasedMemories;
      this.orderPolicy = pOrderPolicy;
      this.runningPolicy = pRunningPolicy;
      pDurations.forEach((p_258332_) -> {
         this.behaviors.add(p_258332_.getFirst(), p_258332_.getSecond());
      });
   }

   public Behavior.Status getStatus() {
      return this.status;
   }

   private boolean hasRequiredMemories(E pEntity) {
      for(Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
         MemoryModuleType<?> memorymoduletype = entry.getKey();
         MemoryStatus memorystatus = entry.getValue();
         if (!pEntity.getBrain().checkMemory(memorymoduletype, memorystatus)) {
            return false;
         }
      }

      return true;
   }

   public final boolean tryStart(ServerLevel pLevel, E pEntity, long pGameTime) {
      if (this.hasRequiredMemories(pEntity)) {
         this.status = Behavior.Status.RUNNING;
         this.orderPolicy.apply(this.behaviors);
         this.runningPolicy.apply(this.behaviors.stream(), pLevel, pEntity, pGameTime);
         return true;
      } else {
         return false;
      }
   }

   public final void tickOrStop(ServerLevel pLevel, E pEntity, long pGameTime) {
      this.behaviors.stream().filter((p_258342_) -> {
         return p_258342_.getStatus() == Behavior.Status.RUNNING;
      }).forEach((p_258336_) -> {
         p_258336_.tickOrStop(pLevel, pEntity, pGameTime);
      });
      if (this.behaviors.stream().noneMatch((p_258344_) -> {
         return p_258344_.getStatus() == Behavior.Status.RUNNING;
      })) {
         this.doStop(pLevel, pEntity, pGameTime);
      }

   }

   public final void doStop(ServerLevel pLevel, E pEntity, long pGameTime) {
      this.status = Behavior.Status.STOPPED;
      this.behaviors.stream().filter((p_258337_) -> {
         return p_258337_.getStatus() == Behavior.Status.RUNNING;
      }).forEach((p_258341_) -> {
         p_258341_.doStop(pLevel, pEntity, pGameTime);
      });
      this.exitErasedMemories.forEach(pEntity.getBrain()::eraseMemory);
   }

   public String debugString() {
      return this.getClass().getSimpleName();
   }

   public String toString() {
      Set<? extends BehaviorControl<? super E>> set = this.behaviors.stream().filter((p_258343_) -> {
         return p_258343_.getStatus() == Behavior.Status.RUNNING;
      }).collect(Collectors.toSet());
      return "(" + this.getClass().getSimpleName() + "): " + set;
   }

   public static enum OrderPolicy {
      ORDERED((p_147530_) -> {
      }),
      SHUFFLED(ShufflingList::shuffle);

      private final Consumer<ShufflingList<?>> consumer;

      private OrderPolicy(Consumer<ShufflingList<?>> pConsumer) {
         this.consumer = pConsumer;
      }

      public void apply(ShufflingList<?> pList) {
         this.consumer.accept(pList);
      }
   }

   public static enum RunningPolicy {
      RUN_ONE {
         public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> p_147537_, ServerLevel p_147538_, E p_147539_, long p_147540_) {
            p_147537_.filter((p_258349_) -> {
               return p_258349_.getStatus() == Behavior.Status.STOPPED;
            }).filter((p_258348_) -> {
               return p_258348_.tryStart(p_147538_, p_147539_, p_147540_);
            }).findFirst();
         }
      },
      TRY_ALL {
         public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> p_147542_, ServerLevel p_147543_, E p_147544_, long p_147545_) {
            p_147542_.filter((p_258350_) -> {
               return p_258350_.getStatus() == Behavior.Status.STOPPED;
            }).forEach((p_258354_) -> {
               p_258354_.tryStart(p_147543_, p_147544_, p_147545_);
            });
         }
      };

      public abstract <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> pBehaviors, ServerLevel pLevel, E pOwner, long pGameTime);
   }
}