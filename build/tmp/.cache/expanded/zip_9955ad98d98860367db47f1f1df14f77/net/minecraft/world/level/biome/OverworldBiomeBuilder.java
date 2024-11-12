package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.TerrainProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public final class OverworldBiomeBuilder {
   private static final float VALLEY_SIZE = 0.05F;
   private static final float LOW_START = 0.26666668F;
   public static final float HIGH_START = 0.4F;
   private static final float HIGH_END = 0.93333334F;
   private static final float PEAK_SIZE = 0.1F;
   public static final float PEAK_START = 0.56666666F;
   private static final float PEAK_END = 0.7666667F;
   public static final float NEAR_INLAND_START = -0.11F;
   public static final float MID_INLAND_START = 0.03F;
   public static final float FAR_INLAND_START = 0.3F;
   public static final float EROSION_INDEX_1_START = -0.78F;
   public static final float EROSION_INDEX_2_START = -0.375F;
   private static final float EROSION_DEEP_DARK_DRYNESS_THRESHOLD = -0.225F;
   private static final float DEPTH_DEEP_DARK_DRYNESS_THRESHOLD = 0.9F;
   private final Climate.Parameter FULL_RANGE = Climate.Parameter.span(-1.0F, 1.0F);
   private final Climate.Parameter[] temperatures = new Climate.Parameter[]{Climate.Parameter.span(-1.0F, -0.45F), Climate.Parameter.span(-0.45F, -0.15F), Climate.Parameter.span(-0.15F, 0.2F), Climate.Parameter.span(0.2F, 0.55F), Climate.Parameter.span(0.55F, 1.0F)};
   private final Climate.Parameter[] humidities = new Climate.Parameter[]{Climate.Parameter.span(-1.0F, -0.35F), Climate.Parameter.span(-0.35F, -0.1F), Climate.Parameter.span(-0.1F, 0.1F), Climate.Parameter.span(0.1F, 0.3F), Climate.Parameter.span(0.3F, 1.0F)};
   private final Climate.Parameter[] erosions = new Climate.Parameter[]{Climate.Parameter.span(-1.0F, -0.78F), Climate.Parameter.span(-0.78F, -0.375F), Climate.Parameter.span(-0.375F, -0.2225F), Climate.Parameter.span(-0.2225F, 0.05F), Climate.Parameter.span(0.05F, 0.45F), Climate.Parameter.span(0.45F, 0.55F), Climate.Parameter.span(0.55F, 1.0F)};
   private final Climate.Parameter FROZEN_RANGE = this.temperatures[0];
   private final Climate.Parameter UNFROZEN_RANGE = Climate.Parameter.span(this.temperatures[1], this.temperatures[4]);
   private final Climate.Parameter mushroomFieldsContinentalness = Climate.Parameter.span(-1.2F, -1.05F);
   private final Climate.Parameter deepOceanContinentalness = Climate.Parameter.span(-1.05F, -0.455F);
   private final Climate.Parameter oceanContinentalness = Climate.Parameter.span(-0.455F, -0.19F);
   private final Climate.Parameter coastContinentalness = Climate.Parameter.span(-0.19F, -0.11F);
   private final Climate.Parameter inlandContinentalness = Climate.Parameter.span(-0.11F, 0.55F);
   private final Climate.Parameter nearInlandContinentalness = Climate.Parameter.span(-0.11F, 0.03F);
   private final Climate.Parameter midInlandContinentalness = Climate.Parameter.span(0.03F, 0.3F);
   private final Climate.Parameter farInlandContinentalness = Climate.Parameter.span(0.3F, 1.0F);
   private final ResourceKey<Biome>[][] OCEANS = new ResourceKey[][]{{Biomes.DEEP_FROZEN_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN, Biomes.WARM_OCEAN}, {Biomes.FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.OCEAN, Biomes.LUKEWARM_OCEAN, Biomes.WARM_OCEAN}};
   private final ResourceKey<Biome>[][] MIDDLE_BIOMES = new ResourceKey[][]{{Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.TAIGA}, {Biomes.PLAINS, Biomes.PLAINS, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA}, {Biomes.FLOWER_FOREST, Biomes.PLAINS, Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST}, {Biomes.SAVANNA, Biomes.SAVANNA, Biomes.FOREST, Biomes.JUNGLE, Biomes.JUNGLE}, {Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.DESERT}};
   private final ResourceKey<Biome>[][] MIDDLE_BIOMES_VARIANT = new ResourceKey[][]{{Biomes.ICE_SPIKES, null, Biomes.SNOWY_TAIGA, null, null}, {null, null, null, null, Biomes.OLD_GROWTH_PINE_TAIGA}, {Biomes.SUNFLOWER_PLAINS, null, null, Biomes.OLD_GROWTH_BIRCH_FOREST, null}, {null, null, Biomes.PLAINS, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE}, {null, null, null, null, null}};
   private final ResourceKey<Biome>[][] PLATEAU_BIOMES = new ResourceKey[][]{{Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA}, {Biomes.MEADOW, Biomes.MEADOW, Biomes.FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA}, {Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.MEADOW, Biomes.DARK_FOREST}, {Biomes.SAVANNA_PLATEAU, Biomes.SAVANNA_PLATEAU, Biomes.FOREST, Biomes.FOREST, Biomes.JUNGLE}, {Biomes.BADLANDS, Biomes.BADLANDS, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.WOODED_BADLANDS}};
   private final ResourceKey<Biome>[][] PLATEAU_BIOMES_VARIANT = new ResourceKey[][]{{Biomes.ICE_SPIKES, null, null, null, null}, {Biomes.CHERRY_GROVE, null, Biomes.MEADOW, Biomes.MEADOW, Biomes.OLD_GROWTH_PINE_TAIGA}, {Biomes.CHERRY_GROVE, Biomes.CHERRY_GROVE, Biomes.FOREST, Biomes.BIRCH_FOREST, null}, {null, null, null, null, null}, {Biomes.ERODED_BADLANDS, Biomes.ERODED_BADLANDS, null, null, null}};
   private final ResourceKey<Biome>[][] SHATTERED_BIOMES = new ResourceKey[][]{{Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST}, {Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST}, {Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_FOREST}, {null, null, null, null, null}, {null, null, null, null, null}};

   public List<Climate.ParameterPoint> spawnTarget() {
      Climate.Parameter climate$parameter = Climate.Parameter.point(0.0F);
      float f = 0.16F;
      return List.of(new Climate.ParameterPoint(this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE), this.FULL_RANGE, climate$parameter, Climate.Parameter.span(-1.0F, -0.16F), 0L), new Climate.ParameterPoint(this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.FULL_RANGE), this.FULL_RANGE, climate$parameter, Climate.Parameter.span(0.16F, 1.0F), 0L));
   }

   protected void addBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pKey) {
      if (SharedConstants.debugGenerateSquareTerrainWithoutNoise) {
         this.addDebugBiomes(pKey);
      } else {
         this.addOffCoastBiomes(pKey);
         this.addInlandBiomes(pKey);
         this.addUndergroundBiomes(pKey);
      }
   }

   private void addDebugBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pKey) {
      HolderLookup.Provider holderlookup$provider = VanillaRegistries.createLookup();
      HolderGetter<DensityFunction> holdergetter = holderlookup$provider.lookupOrThrow(Registries.DENSITY_FUNCTION);
      DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate = new DensityFunctions.Spline.Coordinate(holdergetter.getOrThrow(NoiseRouterData.CONTINENTS));
      DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate1 = new DensityFunctions.Spline.Coordinate(holdergetter.getOrThrow(NoiseRouterData.EROSION));
      DensityFunctions.Spline.Coordinate densityfunctions$spline$coordinate2 = new DensityFunctions.Spline.Coordinate(holdergetter.getOrThrow(NoiseRouterData.RIDGES_FOLDED));
      pKey.accept(Pair.of(Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(0.0F), this.FULL_RANGE, 0.01F), Biomes.PLAINS));
      CubicSpline<?, ?> cubicspline = TerrainProvider.buildErosionOffsetSpline(densityfunctions$spline$coordinate1, densityfunctions$spline$coordinate2, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, ToFloatFunction.IDENTITY);
      if (cubicspline instanceof CubicSpline.Multipoint<?, ?> multipoint) {
         ResourceKey<Biome> resourcekey = Biomes.DESERT;

         for(float f : multipoint.locations()) {
            pKey.accept(Pair.of(Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(f), Climate.Parameter.point(0.0F), this.FULL_RANGE, 0.0F), resourcekey));
            resourcekey = resourcekey == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
         }
      }

      CubicSpline<?, ?> cubicspline1 = TerrainProvider.overworldOffset(densityfunctions$spline$coordinate, densityfunctions$spline$coordinate1, densityfunctions$spline$coordinate2, false);
      if (cubicspline1 instanceof CubicSpline.Multipoint<?, ?> multipoint1) {
         for(float f1 : multipoint1.locations()) {
            pKey.accept(Pair.of(Climate.parameters(this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.point(f1), this.FULL_RANGE, Climate.Parameter.point(0.0F), this.FULL_RANGE, 0.0F), Biomes.SNOWY_TAIGA));
         }
      }

   }

   private void addOffCoastBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer) {
      this.addSurfaceBiome(pConsumer, this.FULL_RANGE, this.FULL_RANGE, this.mushroomFieldsContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.MUSHROOM_FIELDS);

      for(int i = 0; i < this.temperatures.length; ++i) {
         Climate.Parameter climate$parameter = this.temperatures[i];
         this.addSurfaceBiome(pConsumer, climate$parameter, this.FULL_RANGE, this.deepOceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[0][i]);
         this.addSurfaceBiome(pConsumer, climate$parameter, this.FULL_RANGE, this.oceanContinentalness, this.FULL_RANGE, this.FULL_RANGE, 0.0F, this.OCEANS[1][i]);
      }

   }

   private void addInlandBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer) {
      this.addMidSlice(pConsumer, Climate.Parameter.span(-1.0F, -0.93333334F));
      this.addHighSlice(pConsumer, Climate.Parameter.span(-0.93333334F, -0.7666667F));
      this.addPeaks(pConsumer, Climate.Parameter.span(-0.7666667F, -0.56666666F));
      this.addHighSlice(pConsumer, Climate.Parameter.span(-0.56666666F, -0.4F));
      this.addMidSlice(pConsumer, Climate.Parameter.span(-0.4F, -0.26666668F));
      this.addLowSlice(pConsumer, Climate.Parameter.span(-0.26666668F, -0.05F));
      this.addValleys(pConsumer, Climate.Parameter.span(-0.05F, 0.05F));
      this.addLowSlice(pConsumer, Climate.Parameter.span(0.05F, 0.26666668F));
      this.addMidSlice(pConsumer, Climate.Parameter.span(0.26666668F, 0.4F));
      this.addHighSlice(pConsumer, Climate.Parameter.span(0.4F, 0.56666666F));
      this.addPeaks(pConsumer, Climate.Parameter.span(0.56666666F, 0.7666667F));
      this.addHighSlice(pConsumer, Climate.Parameter.span(0.7666667F, 0.93333334F));
      this.addMidSlice(pConsumer, Climate.Parameter.span(0.93333334F, 1.0F));
   }

   private void addPeaks(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pParam) {
      for(int i = 0; i < this.temperatures.length; ++i) {
         Climate.Parameter climate$parameter = this.temperatures[i];

         for(int j = 0; j < this.humidities.length; ++j) {
            Climate.Parameter climate$parameter1 = this.humidities[j];
            ResourceKey<Biome> resourcekey = this.pickMiddleBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey1 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, pParam);
            ResourceKey<Biome> resourcekey2 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, pParam);
            ResourceKey<Biome> resourcekey3 = this.pickPlateauBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey4 = this.pickShatteredBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey5 = this.maybePickWindsweptSavannaBiome(i, j, pParam, resourcekey4);
            ResourceKey<Biome> resourcekey6 = this.pickPeakBiome(i, j, pParam);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[0], pParam, 0.0F, resourcekey6);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[1], pParam, 0.0F, resourcekey2);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[1], pParam, 0.0F, resourcekey6);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[3]), pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[2], pParam, 0.0F, resourcekey3);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.midInlandContinentalness, this.erosions[3], pParam, 0.0F, resourcekey1);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.farInlandContinentalness, this.erosions[3], pParam, 0.0F, resourcekey3);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[5], pParam, 0.0F, resourcekey5);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], pParam, 0.0F, resourcekey4);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, resourcekey);
         }
      }

   }

   private void addHighSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pParam) {
      for(int i = 0; i < this.temperatures.length; ++i) {
         Climate.Parameter climate$parameter = this.temperatures[i];

         for(int j = 0; j < this.humidities.length; ++j) {
            Climate.Parameter climate$parameter1 = this.humidities[j];
            ResourceKey<Biome> resourcekey = this.pickMiddleBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey1 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, pParam);
            ResourceKey<Biome> resourcekey2 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, pParam);
            ResourceKey<Biome> resourcekey3 = this.pickPlateauBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey4 = this.pickShatteredBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey5 = this.maybePickWindsweptSavannaBiome(i, j, pParam, resourcekey);
            ResourceKey<Biome> resourcekey6 = this.pickSlopeBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey7 = this.pickPeakBiome(i, j, pParam);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, this.erosions[0], pParam, 0.0F, resourcekey6);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[0], pParam, 0.0F, resourcekey7);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, this.erosions[1], pParam, 0.0F, resourcekey2);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[1], pParam, 0.0F, resourcekey6);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[3]), pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[2], pParam, 0.0F, resourcekey3);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.midInlandContinentalness, this.erosions[3], pParam, 0.0F, resourcekey1);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.farInlandContinentalness, this.erosions[3], pParam, 0.0F, resourcekey3);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[5], pParam, 0.0F, resourcekey5);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], pParam, 0.0F, resourcekey4);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, resourcekey);
         }
      }

   }

   private void addMidSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pParam) {
      this.addSurfaceBiome(pConsumer, this.FULL_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[2]), pParam, 0.0F, Biomes.STONY_SHORE);
      this.addSurfaceBiome(pConsumer, Climate.Parameter.span(this.temperatures[1], this.temperatures[2]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.SWAMP);
      this.addSurfaceBiome(pConsumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.MANGROVE_SWAMP);

      for(int i = 0; i < this.temperatures.length; ++i) {
         Climate.Parameter climate$parameter = this.temperatures[i];

         for(int j = 0; j < this.humidities.length; ++j) {
            Climate.Parameter climate$parameter1 = this.humidities[j];
            ResourceKey<Biome> resourcekey = this.pickMiddleBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey1 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, pParam);
            ResourceKey<Biome> resourcekey2 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, pParam);
            ResourceKey<Biome> resourcekey3 = this.pickShatteredBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey4 = this.pickPlateauBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey5 = this.pickBeachBiome(i, j);
            ResourceKey<Biome> resourcekey6 = this.maybePickWindsweptSavannaBiome(i, j, pParam, resourcekey);
            ResourceKey<Biome> resourcekey7 = this.pickShatteredCoastBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey8 = this.pickSlopeBiome(i, j, pParam);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[0], pParam, 0.0F, resourcekey8);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.nearInlandContinentalness, this.midInlandContinentalness), this.erosions[1], pParam, 0.0F, resourcekey2);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.farInlandContinentalness, this.erosions[1], pParam, 0.0F, i == 0 ? resourcekey8 : resourcekey4);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, this.erosions[2], pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.midInlandContinentalness, this.erosions[2], pParam, 0.0F, resourcekey1);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.farInlandContinentalness, this.erosions[2], pParam, 0.0F, resourcekey4);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.nearInlandContinentalness), this.erosions[3], pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[3], pParam, 0.0F, resourcekey1);
            if (pParam.max() < 0L) {
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, this.erosions[4], pParam, 0.0F, resourcekey5);
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[4], pParam, 0.0F, resourcekey);
            } else {
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), this.erosions[4], pParam, 0.0F, resourcekey);
            }

            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, this.erosions[5], pParam, 0.0F, resourcekey7);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, this.erosions[5], pParam, 0.0F, resourcekey6);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], pParam, 0.0F, resourcekey3);
            if (pParam.max() < 0L) {
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, this.erosions[6], pParam, 0.0F, resourcekey5);
            } else {
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, this.erosions[6], pParam, 0.0F, resourcekey);
            }

            if (i == 0) {
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, resourcekey);
            }
         }
      }

   }

   private void addLowSlice(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pParam) {
      this.addSurfaceBiome(pConsumer, this.FULL_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[2]), pParam, 0.0F, Biomes.STONY_SHORE);
      this.addSurfaceBiome(pConsumer, Climate.Parameter.span(this.temperatures[1], this.temperatures[2]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.SWAMP);
      this.addSurfaceBiome(pConsumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.MANGROVE_SWAMP);

      for(int i = 0; i < this.temperatures.length; ++i) {
         Climate.Parameter climate$parameter = this.temperatures[i];

         for(int j = 0; j < this.humidities.length; ++j) {
            Climate.Parameter climate$parameter1 = this.humidities[j];
            ResourceKey<Biome> resourcekey = this.pickMiddleBiome(i, j, pParam);
            ResourceKey<Biome> resourcekey1 = this.pickMiddleBiomeOrBadlandsIfHot(i, j, pParam);
            ResourceKey<Biome> resourcekey2 = this.pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(i, j, pParam);
            ResourceKey<Biome> resourcekey3 = this.pickBeachBiome(i, j);
            ResourceKey<Biome> resourcekey4 = this.maybePickWindsweptSavannaBiome(i, j, pParam, resourcekey);
            ResourceKey<Biome> resourcekey5 = this.pickShatteredCoastBiome(i, j, pParam);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, resourcekey1);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, resourcekey2);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[2], this.erosions[3]), pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[3]), pParam, 0.0F, resourcekey1);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, Climate.Parameter.span(this.erosions[3], this.erosions[4]), pParam, 0.0F, resourcekey3);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[4], pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, this.erosions[5], pParam, 0.0F, resourcekey5);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.nearInlandContinentalness, this.erosions[5], pParam, 0.0F, resourcekey4);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), this.erosions[5], pParam, 0.0F, resourcekey);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, this.coastContinentalness, this.erosions[6], pParam, 0.0F, resourcekey3);
            if (i == 0) {
               this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.nearInlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, resourcekey);
            }
         }
      }

   }

   private void addValleys(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pParam) {
      this.addSurfaceBiome(pConsumer, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, pParam.max() < 0L ? Biomes.STONY_SHORE : Biomes.FROZEN_RIVER);
      this.addSurfaceBiome(pConsumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, pParam.max() < 0L ? Biomes.STONY_SHORE : Biomes.RIVER);
      this.addSurfaceBiome(pConsumer, this.FROZEN_RANGE, this.FULL_RANGE, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, Biomes.FROZEN_RIVER);
      this.addSurfaceBiome(pConsumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.nearInlandContinentalness, Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, Biomes.RIVER);
      this.addSurfaceBiome(pConsumer, this.FROZEN_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[5]), pParam, 0.0F, Biomes.FROZEN_RIVER);
      this.addSurfaceBiome(pConsumer, this.UNFROZEN_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.coastContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[2], this.erosions[5]), pParam, 0.0F, Biomes.RIVER);
      this.addSurfaceBiome(pConsumer, this.FROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], pParam, 0.0F, Biomes.FROZEN_RIVER);
      this.addSurfaceBiome(pConsumer, this.UNFROZEN_RANGE, this.FULL_RANGE, this.coastContinentalness, this.erosions[6], pParam, 0.0F, Biomes.RIVER);
      this.addSurfaceBiome(pConsumer, Climate.Parameter.span(this.temperatures[1], this.temperatures[2]), this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.SWAMP);
      this.addSurfaceBiome(pConsumer, Climate.Parameter.span(this.temperatures[3], this.temperatures[4]), this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.MANGROVE_SWAMP);
      this.addSurfaceBiome(pConsumer, this.FROZEN_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.inlandContinentalness, this.farInlandContinentalness), this.erosions[6], pParam, 0.0F, Biomes.FROZEN_RIVER);

      for(int i = 0; i < this.temperatures.length; ++i) {
         Climate.Parameter climate$parameter = this.temperatures[i];

         for(int j = 0; j < this.humidities.length; ++j) {
            Climate.Parameter climate$parameter1 = this.humidities[j];
            ResourceKey<Biome> resourcekey = this.pickMiddleBiomeOrBadlandsIfHot(i, j, pParam);
            this.addSurfaceBiome(pConsumer, climate$parameter, climate$parameter1, Climate.Parameter.span(this.midInlandContinentalness, this.farInlandContinentalness), Climate.Parameter.span(this.erosions[0], this.erosions[1]), pParam, 0.0F, resourcekey);
         }
      }

   }

   private void addUndergroundBiomes(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsume) {
      this.addUndergroundBiome(pConsume, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(0.8F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.DRIPSTONE_CAVES);
      this.addUndergroundBiome(pConsume, this.FULL_RANGE, Climate.Parameter.span(0.7F, 1.0F), this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, 0.0F, Biomes.LUSH_CAVES);
      this.addBottomBiome(pConsume, this.FULL_RANGE, this.FULL_RANGE, this.FULL_RANGE, Climate.Parameter.span(this.erosions[0], this.erosions[1]), this.FULL_RANGE, 0.0F, Biomes.DEEP_DARK);
   }

   private ResourceKey<Biome> pickMiddleBiome(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      if (pParam.max() < 0L) {
         return this.MIDDLE_BIOMES[pTemperature][pHumidity];
      } else {
         ResourceKey<Biome> resourcekey = this.MIDDLE_BIOMES_VARIANT[pTemperature][pHumidity];
         return resourcekey == null ? this.MIDDLE_BIOMES[pTemperature][pHumidity] : resourcekey;
      }
   }

   private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHot(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      return pTemperature == 4 ? this.pickBadlandsBiome(pHumidity, pParam) : this.pickMiddleBiome(pTemperature, pHumidity, pParam);
   }

   private ResourceKey<Biome> pickMiddleBiomeOrBadlandsIfHotOrSlopeIfCold(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      return pTemperature == 0 ? this.pickSlopeBiome(pTemperature, pHumidity, pParam) : this.pickMiddleBiomeOrBadlandsIfHot(pTemperature, pHumidity, pParam);
   }

   private ResourceKey<Biome> maybePickWindsweptSavannaBiome(int pTemperature, int pHumidity, Climate.Parameter pParam, ResourceKey<Biome> pKey) {
      return pTemperature > 1 && pHumidity < 4 && pParam.max() >= 0L ? Biomes.WINDSWEPT_SAVANNA : pKey;
   }

   private ResourceKey<Biome> pickShatteredCoastBiome(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      ResourceKey<Biome> resourcekey = pParam.max() >= 0L ? this.pickMiddleBiome(pTemperature, pHumidity, pParam) : this.pickBeachBiome(pTemperature, pHumidity);
      return this.maybePickWindsweptSavannaBiome(pTemperature, pHumidity, pParam, resourcekey);
   }

   private ResourceKey<Biome> pickBeachBiome(int pTemperature, int pHumidity) {
      if (pTemperature == 0) {
         return Biomes.SNOWY_BEACH;
      } else {
         return pTemperature == 4 ? Biomes.DESERT : Biomes.BEACH;
      }
   }

   private ResourceKey<Biome> pickBadlandsBiome(int pHumidity, Climate.Parameter pParam) {
      if (pHumidity < 2) {
         return pParam.max() < 0L ? Biomes.BADLANDS : Biomes.ERODED_BADLANDS;
      } else {
         return pHumidity < 3 ? Biomes.BADLANDS : Biomes.WOODED_BADLANDS;
      }
   }

   private ResourceKey<Biome> pickPlateauBiome(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      if (pParam.max() >= 0L) {
         ResourceKey<Biome> resourcekey = this.PLATEAU_BIOMES_VARIANT[pTemperature][pHumidity];
         if (resourcekey != null) {
            return resourcekey;
         }
      }

      return this.PLATEAU_BIOMES[pTemperature][pHumidity];
   }

   private ResourceKey<Biome> pickPeakBiome(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      if (pTemperature <= 2) {
         return pParam.max() < 0L ? Biomes.JAGGED_PEAKS : Biomes.FROZEN_PEAKS;
      } else {
         return pTemperature == 3 ? Biomes.STONY_PEAKS : this.pickBadlandsBiome(pHumidity, pParam);
      }
   }

   private ResourceKey<Biome> pickSlopeBiome(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      if (pTemperature >= 3) {
         return this.pickPlateauBiome(pTemperature, pHumidity, pParam);
      } else {
         return pHumidity <= 1 ? Biomes.SNOWY_SLOPES : Biomes.GROVE;
      }
   }

   private ResourceKey<Biome> pickShatteredBiome(int pTemperature, int pHumidity, Climate.Parameter pParam) {
      ResourceKey<Biome> resourcekey = this.SHATTERED_BIOMES[pTemperature][pHumidity];
      return resourcekey == null ? this.pickMiddleBiome(pTemperature, pHumidity, pParam) : resourcekey;
   }

   private void addSurfaceBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pTemperature, Climate.Parameter pHumidity, Climate.Parameter pContinentalness, Climate.Parameter pErosion, Climate.Parameter pDepth, float pWeirdness, ResourceKey<Biome> pKey) {
      pConsumer.accept(Pair.of(Climate.parameters(pTemperature, pHumidity, pContinentalness, pErosion, Climate.Parameter.point(0.0F), pDepth, pWeirdness), pKey));
      pConsumer.accept(Pair.of(Climate.parameters(pTemperature, pHumidity, pContinentalness, pErosion, Climate.Parameter.point(1.0F), pDepth, pWeirdness), pKey));
   }

   private void addUndergroundBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pTemperature, Climate.Parameter pHumidity, Climate.Parameter pContinentalness, Climate.Parameter pErosion, Climate.Parameter pDepth, float pWeirdness, ResourceKey<Biome> pKey) {
      pConsumer.accept(Pair.of(Climate.parameters(pTemperature, pHumidity, pContinentalness, pErosion, Climate.Parameter.span(0.2F, 0.9F), pDepth, pWeirdness), pKey));
   }

   private void addBottomBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> pConsumer, Climate.Parameter pTemerature, Climate.Parameter pHumidity, Climate.Parameter pContinentalness, Climate.Parameter pErosion, Climate.Parameter pDepth, float pWeirdness, ResourceKey<Biome> pKey) {
      pConsumer.accept(Pair.of(Climate.parameters(pTemerature, pHumidity, pContinentalness, pErosion, Climate.Parameter.point(1.1F), pDepth, pWeirdness), pKey));
   }

   public static boolean isDeepDarkRegion(DensityFunction pErosionFunction, DensityFunction pDepthFunction, DensityFunction.FunctionContext pFunctionContext) {
      return pErosionFunction.compute(pFunctionContext) < (double)-0.225F && pDepthFunction.compute(pFunctionContext) > (double)0.9F;
   }

   public static String getDebugStringForPeaksAndValleys(double pPeaksAndValleysData) {
      if (pPeaksAndValleysData < (double)NoiseRouterData.peaksAndValleys(0.05F)) {
         return "Valley";
      } else if (pPeaksAndValleysData < (double)NoiseRouterData.peaksAndValleys(0.26666668F)) {
         return "Low";
      } else if (pPeaksAndValleysData < (double)NoiseRouterData.peaksAndValleys(0.4F)) {
         return "Mid";
      } else {
         return pPeaksAndValleysData < (double)NoiseRouterData.peaksAndValleys(0.56666666F) ? "High" : "Peak";
      }
   }

   public String getDebugStringForContinentalness(double pContinentalness) {
      double d0 = (double)Climate.quantizeCoord((float)pContinentalness);
      if (d0 < (double)this.mushroomFieldsContinentalness.max()) {
         return "Mushroom fields";
      } else if (d0 < (double)this.deepOceanContinentalness.max()) {
         return "Deep ocean";
      } else if (d0 < (double)this.oceanContinentalness.max()) {
         return "Ocean";
      } else if (d0 < (double)this.coastContinentalness.max()) {
         return "Coast";
      } else if (d0 < (double)this.nearInlandContinentalness.max()) {
         return "Near inland";
      } else {
         return d0 < (double)this.midInlandContinentalness.max() ? "Mid inland" : "Far inland";
      }
   }

   public String getDebugStringForErosion(double pErosion) {
      return getDebugStringForNoiseValue(pErosion, this.erosions);
   }

   public String getDebugStringForTemperature(double pTemperature) {
      return getDebugStringForNoiseValue(pTemperature, this.temperatures);
   }

   public String getDebugStringForHumidity(double pHumidity) {
      return getDebugStringForNoiseValue(pHumidity, this.humidities);
   }

   private static String getDebugStringForNoiseValue(double pDepth, Climate.Parameter[] pValues) {
      double d0 = (double)Climate.quantizeCoord((float)pDepth);

      for(int i = 0; i < pValues.length; ++i) {
         if (d0 < (double)pValues[i].max()) {
            return "" + i;
         }
      }

      return "?";
   }

   @VisibleForDebug
   public Climate.Parameter[] getTemperatureThresholds() {
      return this.temperatures;
   }

   @VisibleForDebug
   public Climate.Parameter[] getHumidityThresholds() {
      return this.humidities;
   }

   @VisibleForDebug
   public Climate.Parameter[] getErosionThresholds() {
      return this.erosions;
   }

   @VisibleForDebug
   public Climate.Parameter[] getContinentalnessThresholds() {
      return new Climate.Parameter[]{this.mushroomFieldsContinentalness, this.deepOceanContinentalness, this.oceanContinentalness, this.coastContinentalness, this.nearInlandContinentalness, this.midInlandContinentalness, this.farInlandContinentalness};
   }

   @VisibleForDebug
   public Climate.Parameter[] getPeaksAndValleysThresholds() {
      return new Climate.Parameter[]{Climate.Parameter.span(-2.0F, NoiseRouterData.peaksAndValleys(0.05F)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.05F), NoiseRouterData.peaksAndValleys(0.26666668F)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.26666668F), NoiseRouterData.peaksAndValleys(0.4F)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.4F), NoiseRouterData.peaksAndValleys(0.56666666F)), Climate.Parameter.span(NoiseRouterData.peaksAndValleys(0.56666666F), 2.0F)};
   }

   @VisibleForDebug
   public Climate.Parameter[] getWeirdnessThresholds() {
      return new Climate.Parameter[]{Climate.Parameter.span(-2.0F, 0.0F), Climate.Parameter.span(0.0F, 2.0F)};
   }
}