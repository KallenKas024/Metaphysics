package net.minecraft.world.flag;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class FeatureFlagRegistry {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final FeatureFlagUniverse universe;
   private final Map<ResourceLocation, FeatureFlag> names;
   private final FeatureFlagSet allFlags;

   FeatureFlagRegistry(FeatureFlagUniverse pUniverse, FeatureFlagSet pAllFlags, Map<ResourceLocation, FeatureFlag> pNames) {
      this.universe = pUniverse;
      this.names = pNames;
      this.allFlags = pAllFlags;
   }

   public boolean isSubset(FeatureFlagSet pSet) {
      return pSet.isSubsetOf(this.allFlags);
   }

   public FeatureFlagSet allFlags() {
      return this.allFlags;
   }

   public FeatureFlagSet fromNames(Iterable<ResourceLocation> pNames) {
      return this.fromNames(pNames, (p_251224_) -> {
         LOGGER.warn("Unknown feature flag: {}", (Object)p_251224_);
      });
   }

   public FeatureFlagSet subset(FeatureFlag... pFlags) {
      return FeatureFlagSet.create(this.universe, Arrays.asList(pFlags));
   }

   public FeatureFlagSet fromNames(Iterable<ResourceLocation> pNames, Consumer<ResourceLocation> pOnError) {
      Set<FeatureFlag> set = Sets.newIdentityHashSet();

      for(ResourceLocation resourcelocation : pNames) {
         FeatureFlag featureflag = this.names.get(resourcelocation);
         if (featureflag == null) {
            pOnError.accept(resourcelocation);
         } else {
            set.add(featureflag);
         }
      }

      return FeatureFlagSet.create(this.universe, set);
   }

   public Set<ResourceLocation> toNames(FeatureFlagSet pSet) {
      Set<ResourceLocation> set = new HashSet<>();
      this.names.forEach((p_252018_, p_250772_) -> {
         if (pSet.contains(p_250772_)) {
            set.add(p_252018_);
         }

      });
      return set;
   }

   public Codec<FeatureFlagSet> codec() {
      return ResourceLocation.CODEC.listOf().comapFlatMap((p_275144_) -> {
         Set<ResourceLocation> set = new HashSet<>();
         FeatureFlagSet featureflagset = this.fromNames(p_275144_, set::add);
         return !set.isEmpty() ? DataResult.error(() -> {
            return "Unknown feature ids: " + set;
         }, featureflagset) : DataResult.success(featureflagset);
      }, (p_249796_) -> {
         return List.copyOf(this.toNames(p_249796_));
      });
   }

   public static class Builder {
      private final FeatureFlagUniverse universe;
      private int id;
      private final Map<ResourceLocation, FeatureFlag> flags = new LinkedHashMap<>();

      public Builder(String pId) {
         this.universe = new FeatureFlagUniverse(pId);
      }

      public FeatureFlag createVanilla(String pId) {
         return this.create(new ResourceLocation("minecraft", pId));
      }

      public FeatureFlag create(ResourceLocation pLocation) {
         if (this.id >= 64) {
            throw new IllegalStateException("Too many feature flags");
         } else {
            FeatureFlag featureflag = new FeatureFlag(this.universe, this.id++);
            FeatureFlag featureflag1 = this.flags.put(pLocation, featureflag);
            if (featureflag1 != null) {
               throw new IllegalStateException("Duplicate feature flag " + pLocation);
            } else {
               return featureflag;
            }
         }
      }

      public FeatureFlagRegistry build() {
         FeatureFlagSet featureflagset = FeatureFlagSet.create(this.universe, this.flags.values());
         return new FeatureFlagRegistry(this.universe, featureflagset, Map.copyOf(this.flags));
      }
   }
}