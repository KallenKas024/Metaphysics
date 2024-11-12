package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CaveCarverConfiguration extends CarverConfiguration {
   public static final Codec<CaveCarverConfiguration> CODEC = RecordCodecBuilder.create((p_159184_) -> {
      return p_159184_.group(CarverConfiguration.CODEC.forGetter((p_159192_) -> {
         return p_159192_;
      }), FloatProvider.CODEC.fieldOf("horizontal_radius_multiplier").forGetter((p_159190_) -> {
         return p_159190_.horizontalRadiusMultiplier;
      }), FloatProvider.CODEC.fieldOf("vertical_radius_multiplier").forGetter((p_159188_) -> {
         return p_159188_.verticalRadiusMultiplier;
      }), FloatProvider.codec(-1.0F, 1.0F).fieldOf("floor_level").forGetter((p_159186_) -> {
         return p_159186_.floorLevel;
      })).apply(p_159184_, CaveCarverConfiguration::new);
   });
   public final FloatProvider horizontalRadiusMultiplier;
   public final FloatProvider verticalRadiusMultiplier;
   final FloatProvider floorLevel;

   public CaveCarverConfiguration(float pProbability, HeightProvider pY, FloatProvider pYScale, VerticalAnchor pLavaLevel, CarverDebugSettings pDebugSettings, HolderSet<Block> pReplaceable, FloatProvider pHorizontalRadiusMultiplier, FloatProvider pVerticalRadiusMultiplier, FloatProvider pFloorLevel) {
      super(pProbability, pY, pYScale, pLavaLevel, pDebugSettings, pReplaceable);
      this.horizontalRadiusMultiplier = pHorizontalRadiusMultiplier;
      this.verticalRadiusMultiplier = pVerticalRadiusMultiplier;
      this.floorLevel = pFloorLevel;
   }

   public CaveCarverConfiguration(float pProbability, HeightProvider pY, FloatProvider pYScale, VerticalAnchor pLavaLevel, HolderSet<Block> pReplaceable, FloatProvider pHorizontalRadiusMultiplier, FloatProvider pVerticalRadiusMultiplier, FloatProvider pFloorLevel) {
      this(pProbability, pY, pYScale, pLavaLevel, CarverDebugSettings.DEFAULT, pReplaceable, pHorizontalRadiusMultiplier, pVerticalRadiusMultiplier, pFloorLevel);
   }

   public CaveCarverConfiguration(CarverConfiguration p_159179_, FloatProvider p_159180_, FloatProvider p_159181_, FloatProvider p_159182_) {
      this(p_159179_.probability, p_159179_.y, p_159179_.yScale, p_159179_.lavaLevel, p_159179_.debugSettings, p_159179_.replaceable, p_159180_, p_159181_, p_159182_);
   }
}