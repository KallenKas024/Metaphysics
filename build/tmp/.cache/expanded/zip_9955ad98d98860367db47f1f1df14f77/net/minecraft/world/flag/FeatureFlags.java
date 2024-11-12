package net.minecraft.world.flag;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public class FeatureFlags {
   public static final FeatureFlag VANILLA;
   public static final FeatureFlag BUNDLE;
   public static final FeatureFlagRegistry REGISTRY;
   public static final Codec<FeatureFlagSet> CODEC;
   public static final FeatureFlagSet VANILLA_SET;
   public static final FeatureFlagSet DEFAULT_FLAGS;

   public static String printMissingFlags(FeatureFlagSet pEnabledFeatures, FeatureFlagSet pRequestedFeatures) {
      return printMissingFlags(REGISTRY, pEnabledFeatures, pRequestedFeatures);
   }

   public static String printMissingFlags(FeatureFlagRegistry pRegistry, FeatureFlagSet pEnabledFeatures, FeatureFlagSet pRequestedFeatures) {
      Set<ResourceLocation> set = pRegistry.toNames(pRequestedFeatures);
      Set<ResourceLocation> set1 = pRegistry.toNames(pEnabledFeatures);
      return set.stream().filter((p_251831_) -> {
         return !set1.contains(p_251831_);
      }).map(ResourceLocation::toString).collect(Collectors.joining(", "));
   }

   public static boolean isExperimental(FeatureFlagSet pSet) {
      return !pSet.isSubsetOf(VANILLA_SET);
   }

   static {
      FeatureFlagRegistry.Builder featureflagregistry$builder = new FeatureFlagRegistry.Builder("main");
      VANILLA = featureflagregistry$builder.createVanilla("vanilla");
      BUNDLE = featureflagregistry$builder.createVanilla("bundle");
      REGISTRY = featureflagregistry$builder.build();
      CODEC = REGISTRY.codec();
      VANILLA_SET = FeatureFlagSet.of(VANILLA);
      DEFAULT_FLAGS = VANILLA_SET;
   }
}