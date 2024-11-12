package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class NearestVisibleLivingEntities {
   private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
   private final List<LivingEntity> nearbyEntities;
   private final Predicate<LivingEntity> lineOfSightTest;

   private NearestVisibleLivingEntities() {
      this.nearbyEntities = List.of();
      this.lineOfSightTest = (p_186122_) -> {
         return false;
      };
   }

   public NearestVisibleLivingEntities(LivingEntity pLivingEntity, List<LivingEntity> pNearbyLivingEntities) {
      this.nearbyEntities = pNearbyLivingEntities;
      Object2BooleanOpenHashMap<LivingEntity> object2booleanopenhashmap = new Object2BooleanOpenHashMap<>(pNearbyLivingEntities.size());
      Predicate<LivingEntity> predicate = (p_186111_) -> {
         return Sensor.isEntityTargetable(pLivingEntity, p_186111_);
      };
      this.lineOfSightTest = (p_186115_) -> {
         return object2booleanopenhashmap.computeIfAbsent(p_186115_, predicate);
      };
   }

   public static NearestVisibleLivingEntities empty() {
      return EMPTY;
   }

   public Optional<LivingEntity> findClosest(Predicate<LivingEntity> pPredicate) {
      for(LivingEntity livingentity : this.nearbyEntities) {
         if (pPredicate.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
            return Optional.of(livingentity);
         }
      }

      return Optional.empty();
   }

   public Iterable<LivingEntity> findAll(Predicate<LivingEntity> pPredicate) {
      return Iterables.filter(this.nearbyEntities, (p_186127_) -> {
         return pPredicate.test(p_186127_) && this.lineOfSightTest.test(p_186127_);
      });
   }

   public Stream<LivingEntity> find(Predicate<LivingEntity> pPredicate) {
      return this.nearbyEntities.stream().filter((p_186120_) -> {
         return pPredicate.test(p_186120_) && this.lineOfSightTest.test(p_186120_);
      });
   }

   public boolean contains(LivingEntity pEntity) {
      return this.nearbyEntities.contains(pEntity) && this.lineOfSightTest.test(pEntity);
   }

   public boolean contains(Predicate<LivingEntity> pPredicate) {
      for(LivingEntity livingentity : this.nearbyEntities) {
         if (pPredicate.test(livingentity) && this.lineOfSightTest.test(livingentity)) {
            return true;
         }
      }

      return false;
   }
}