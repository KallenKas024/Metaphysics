package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class Biome {
   public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create((p_220544_) -> {
      return p_220544_.group(Biome.ClimateSettings.CODEC.forGetter((p_151717_) -> {
         return p_151717_.modifiableBiomeInfo().getOriginalBiomeInfo().climateSettings(); // FORGE: Patch codec to ignore field redirect coremods.
      }), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((p_220550_) -> {
         return p_220550_.modifiableBiomeInfo().getOriginalBiomeInfo().effects(); // FORGE: Patch codec to ignore field redirect coremods.
      }), BiomeGenerationSettings.CODEC.forGetter((p_220548_) -> {
         return p_220548_.generationSettings;
      }), MobSpawnSettings.CODEC.forGetter((p_220546_) -> {
         return p_220546_.mobSettings;
      })).apply(p_220544_, Biome::new);
   });
   public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create((p_220540_) -> {
      return p_220540_.group(Biome.ClimateSettings.CODEC.forGetter((p_220542_) -> {
         return p_220542_.climateSettings;
      }), BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter((p_220538_) -> {
         return p_220538_.specialEffects;
      })).apply(p_220540_, (p_220535_, p_220536_) -> {
         return new Biome(p_220535_, p_220536_, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY);
      });
   });
   public static final Codec<Holder<Biome>> CODEC = RegistryFileCodec.create(Registries.BIOME, DIRECT_CODEC);
   public static final Codec<HolderSet<Biome>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.BIOME, DIRECT_CODEC);
   private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(1234L)), ImmutableList.of(0));
   static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(3456L)), ImmutableList.of(-2, -1, 0));
   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), ImmutableList.of(0));
   private static final int TEMPERATURE_CACHE_SIZE = 1024;
   private final Biome.ClimateSettings climateSettings;
   private final BiomeGenerationSettings generationSettings;
   private final MobSpawnSettings mobSettings;
   private final BiomeSpecialEffects specialEffects;
   private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> {
      return Util.make(() -> {
         Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
            protected void rehash(int p_47580_) {
            }
         };
         long2floatlinkedopenhashmap.defaultReturnValue(Float.NaN);
         return long2floatlinkedopenhashmap;
      });
   });

   Biome(Biome.ClimateSettings p_220530_, BiomeSpecialEffects p_220531_, BiomeGenerationSettings p_220532_, MobSpawnSettings p_220533_) {
      this.climateSettings = p_220530_;
      this.generationSettings = p_220532_;
      this.mobSettings = p_220533_;
      this.specialEffects = p_220531_;
      this.modifiableBiomeInfo = new net.minecraftforge.common.world.ModifiableBiomeInfo(new net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo(p_220530_, p_220531_, p_220532_, p_220533_)); // FORGE: cache original biome info on construction so we can bypass our field read coremods where necessary
   }

   public int getSkyColor() {
      return this.specialEffects.getSkyColor();
   }

   public MobSpawnSettings getMobSettings() {
      return this.modifiableBiomeInfo().get().mobSpawnSettings();
   }

   public boolean hasPrecipitation() {
      return this.climateSettings.hasPrecipitation();
   }

   public Biome.Precipitation getPrecipitationAt(BlockPos pPos) {
      if (!this.hasPrecipitation()) {
         return Biome.Precipitation.NONE;
      } else {
         return this.coldEnoughToSnow(pPos) ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
      }
   }

   private float getHeightAdjustedTemperature(BlockPos pPos) {
      float f = this.climateSettings.temperatureModifier.modifyTemperature(pPos, this.getBaseTemperature());
      if (pPos.getY() > 80) {
         float f1 = (float)(TEMPERATURE_NOISE.getValue((double)((float)pPos.getX() / 8.0F), (double)((float)pPos.getZ() / 8.0F), false) * 8.0D);
         return f - (f1 + (float)pPos.getY() - 80.0F) * 0.05F / 40.0F;
      } else {
         return f;
      }
   }

   /** @deprecated */
   @Deprecated
   private float getTemperature(BlockPos pPos) {
      long i = pPos.asLong();
      Long2FloatLinkedOpenHashMap long2floatlinkedopenhashmap = this.temperatureCache.get();
      float f = long2floatlinkedopenhashmap.get(i);
      if (!Float.isNaN(f)) {
         return f;
      } else {
         float f1 = this.getHeightAdjustedTemperature(pPos);
         if (long2floatlinkedopenhashmap.size() == 1024) {
            long2floatlinkedopenhashmap.removeFirstFloat();
         }

         long2floatlinkedopenhashmap.put(i, f1);
         return f1;
      }
   }

   public boolean shouldFreeze(LevelReader pLevel, BlockPos pPos) {
      return this.shouldFreeze(pLevel, pPos, true);
   }

   public boolean shouldFreeze(LevelReader pLevel, BlockPos pWater, boolean pMustBeAtEdge) {
      if (this.warmEnoughToRain(pWater)) {
         return false;
      } else {
         if (pWater.getY() >= pLevel.getMinBuildHeight() && pWater.getY() < pLevel.getMaxBuildHeight() && pLevel.getBrightness(LightLayer.BLOCK, pWater) < 10) {
            BlockState blockstate = pLevel.getBlockState(pWater);
            FluidState fluidstate = pLevel.getFluidState(pWater);
            if (fluidstate.getType() == Fluids.WATER && blockstate.getBlock() instanceof LiquidBlock) {
               if (!pMustBeAtEdge) {
                  return true;
               }

               boolean flag = pLevel.isWaterAt(pWater.west()) && pLevel.isWaterAt(pWater.east()) && pLevel.isWaterAt(pWater.north()) && pLevel.isWaterAt(pWater.south());
               if (!flag) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean coldEnoughToSnow(BlockPos pPos) {
      return !this.warmEnoughToRain(pPos);
   }

   public boolean warmEnoughToRain(BlockPos pPos) {
      return this.getTemperature(pPos) >= 0.15F;
   }

   public boolean shouldMeltFrozenOceanIcebergSlightly(BlockPos pPos) {
      return this.getTemperature(pPos) > 0.1F;
   }

   public boolean shouldSnow(LevelReader pLevel, BlockPos pPos) {
      if (this.warmEnoughToRain(pPos)) {
         return false;
      } else {
         if (pPos.getY() >= pLevel.getMinBuildHeight() && pPos.getY() < pLevel.getMaxBuildHeight() && pLevel.getBrightness(LightLayer.BLOCK, pPos) < 10) {
            BlockState blockstate = pLevel.getBlockState(pPos);
            if ((blockstate.isAir() || blockstate.is(Blocks.SNOW)) && Blocks.SNOW.defaultBlockState().canSurvive(pLevel, pPos)) {
               return true;
            }
         }

         return false;
      }
   }

   public BiomeGenerationSettings getGenerationSettings() {
      return this.modifiableBiomeInfo().get().generationSettings();
   }

   public int getFogColor() {
      return this.specialEffects.getFogColor();
   }

   public int getGrassColor(double pPosX, double pPosZ) {
      int i = this.specialEffects.getGrassColorOverride().orElseGet(this::getGrassColorFromTexture);
      return this.specialEffects.getGrassColorModifier().modifyColor(pPosX, pPosZ, i);
   }

   private int getGrassColorFromTexture() {
      double d0 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double d1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return GrassColor.get(d0, d1);
   }

   public int getFoliageColor() {
      return this.specialEffects.getFoliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
   }

   private int getFoliageColorFromTexture() {
      double d0 = (double)Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double d1 = (double)Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return FoliageColor.get(d0, d1);
   }

   public float getBaseTemperature() {
      return this.climateSettings.temperature;
   }

   public BiomeSpecialEffects getSpecialEffects() {
      return this.specialEffects;
   }

   public int getWaterColor() {
      return this.specialEffects.getWaterColor();
   }

   public int getWaterFogColor() {
      return this.specialEffects.getWaterFogColor();
   }

   public Optional<AmbientParticleSettings> getAmbientParticle() {
      return this.specialEffects.getAmbientParticleSettings();
   }

   public Optional<Holder<SoundEvent>> getAmbientLoop() {
      return this.specialEffects.getAmbientLoopSoundEvent();
   }

   public Optional<AmbientMoodSettings> getAmbientMood() {
      return this.specialEffects.getAmbientMoodSettings();
   }

   public Optional<AmbientAdditionsSettings> getAmbientAdditions() {
      return this.specialEffects.getAmbientAdditionsSettings();
   }

   public Optional<Music> getBackgroundMusic() {
      return this.specialEffects.getBackgroundMusic();
   }

   public static class BiomeBuilder {
      private boolean hasPrecipitation = true;
      @Nullable
      private Float temperature;
      private Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
      @Nullable
      private Float downfall;
      @Nullable
      private BiomeSpecialEffects specialEffects;
      @Nullable
      private MobSpawnSettings mobSpawnSettings;
      @Nullable
      private BiomeGenerationSettings generationSettings;

      public Biome.BiomeBuilder hasPrecipitation(boolean pHasPercipitation) {
         this.hasPrecipitation = pHasPercipitation;
         return this;
      }

      public Biome.BiomeBuilder temperature(float pTemperature) {
         this.temperature = pTemperature;
         return this;
      }

      public Biome.BiomeBuilder downfall(float pDownfall) {
         this.downfall = pDownfall;
         return this;
      }

      public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects pEffects) {
         this.specialEffects = pEffects;
         return this;
      }

      public Biome.BiomeBuilder mobSpawnSettings(MobSpawnSettings pMobSpawnSettings) {
         this.mobSpawnSettings = pMobSpawnSettings;
         return this;
      }

      public Biome.BiomeBuilder generationSettings(BiomeGenerationSettings pGenerationSettings) {
         this.generationSettings = pGenerationSettings;
         return this;
      }

      public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier pTemperatureSettings) {
         this.temperatureModifier = pTemperatureSettings;
         return this;
      }

      public Biome build() {
         if (this.temperature != null && this.downfall != null && this.specialEffects != null && this.mobSpawnSettings != null && this.generationSettings != null) {
            return new Biome(new Biome.ClimateSettings(this.hasPrecipitation, this.temperature, this.temperatureModifier, this.downfall), this.specialEffects, this.generationSettings, this.mobSpawnSettings);
         } else {
            throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
         }
      }

      public String toString() {
         return "BiomeBuilder{\nhasPrecipitation=" + this.hasPrecipitation + ",\ntemperature=" + this.temperature + ",\ntemperatureModifier=" + this.temperatureModifier + ",\ndownfall=" + this.downfall + ",\nspecialEffects=" + this.specialEffects + ",\nmobSpawnSettings=" + this.mobSpawnSettings + ",\ngenerationSettings=" + this.generationSettings + ",\n}";
      }
   }

   public static record ClimateSettings(boolean hasPrecipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
      public static final MapCodec<Biome.ClimateSettings> CODEC = RecordCodecBuilder.mapCodec((p_264995_) -> {
         return p_264995_.group(Codec.BOOL.fieldOf("has_precipitation").forGetter((p_264996_) -> {
            return p_264996_.hasPrecipitation;
         }), Codec.FLOAT.fieldOf("temperature").forGetter((p_151737_) -> {
            return p_151737_.temperature;
         }), Biome.TemperatureModifier.CODEC.optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE).forGetter((p_151735_) -> {
            return p_151735_.temperatureModifier;
         }), Codec.FLOAT.fieldOf("downfall").forGetter((p_151733_) -> {
            return p_151733_.downfall;
         })).apply(p_264995_, Biome.ClimateSettings::new);
      });
   }

   public static enum Precipitation {
      NONE,
      RAIN,
      SNOW;
   }

   public static enum TemperatureModifier implements StringRepresentable {
      NONE("none") {
         public float modifyTemperature(BlockPos p_47767_, float p_47768_) {
            return p_47768_;
         }
      },
      FROZEN("frozen") {
         public float modifyTemperature(BlockPos p_47774_, float p_47775_) {
            double d0 = Biome.FROZEN_TEMPERATURE_NOISE.getValue((double)p_47774_.getX() * 0.05D, (double)p_47774_.getZ() * 0.05D, false) * 7.0D;
            double d1 = Biome.BIOME_INFO_NOISE.getValue((double)p_47774_.getX() * 0.2D, (double)p_47774_.getZ() * 0.2D, false);
            double d2 = d0 + d1;
            if (d2 < 0.3D) {
               double d3 = Biome.BIOME_INFO_NOISE.getValue((double)p_47774_.getX() * 0.09D, (double)p_47774_.getZ() * 0.09D, false);
               if (d3 < 0.8D) {
                  return 0.2F;
               }
            }

            return p_47775_;
         }
      };

      private final String name;
      public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(Biome.TemperatureModifier::values);

      public abstract float modifyTemperature(BlockPos pPos, float pTemperature);

      TemperatureModifier(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   // FORGE START

   private final net.minecraftforge.common.world.ModifiableBiomeInfo modifiableBiomeInfo;

   /**
    * {@return Cache of original biome data and biome data modified by biome modifiers}
    * Modified biome data is set by server after datapacks and serverconfigs load.
    * Climate and effects field reads are coremodded to redirect to this.
    **/
   public net.minecraftforge.common.world.ModifiableBiomeInfo modifiableBiomeInfo()
   {
       return this.modifiableBiomeInfo;
   }

   /**
    * {@return The biome's climate settings, with modifications if called after modifiers are applied in server init.}
    */
   public ClimateSettings getModifiedClimateSettings() {
      return this.modifiableBiomeInfo().get().climateSettings();
   }

   /**
    * {@return The biome's client effects, with modifications if called after modifiers are applied in server init.}
    */
   public BiomeSpecialEffects getModifiedSpecialEffects() {
      return this.modifiableBiomeInfo().get().effects();
   }

   // FORGE END
}
