package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;

public class OreConfiguration implements FeatureConfiguration {
   public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create((p_67849_) -> {
      return p_67849_.group(Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter((p_161027_) -> {
         return p_161027_.targetStates;
      }), Codec.intRange(0, 64).fieldOf("size").forGetter((p_161025_) -> {
         return p_161025_.size;
      }), Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter((p_161020_) -> {
         return p_161020_.discardChanceOnAirExposure;
      })).apply(p_67849_, OreConfiguration::new);
   });
   public final List<OreConfiguration.TargetBlockState> targetStates;
   public final int size;
   public final float discardChanceOnAirExposure;

   public OreConfiguration(List<OreConfiguration.TargetBlockState> p_161016_, int p_161017_, float p_161018_) {
      this.size = p_161017_;
      this.targetStates = p_161016_;
      this.discardChanceOnAirExposure = p_161018_;
   }

   public OreConfiguration(List<OreConfiguration.TargetBlockState> pTargetStates, int pSize) {
      this(pTargetStates, pSize, 0.0F);
   }

   public OreConfiguration(RuleTest pTarget, BlockState pState, int pSize, float pDiscardChanceOnAirExposure) {
      this(ImmutableList.of(new OreConfiguration.TargetBlockState(pTarget, pState)), pSize, pDiscardChanceOnAirExposure);
   }

   public OreConfiguration(RuleTest pTarget, BlockState pState, int pSize) {
      this(ImmutableList.of(new OreConfiguration.TargetBlockState(pTarget, pState)), pSize, 0.0F);
   }

   public static OreConfiguration.TargetBlockState target(RuleTest pTarget, BlockState pState) {
      return new OreConfiguration.TargetBlockState(pTarget, pState);
   }

   public static class TargetBlockState {
      public static final Codec<OreConfiguration.TargetBlockState> CODEC = RecordCodecBuilder.create((p_161039_) -> {
         return p_161039_.group(RuleTest.CODEC.fieldOf("target").forGetter((p_161043_) -> {
            return p_161043_.target;
         }), BlockState.CODEC.fieldOf("state").forGetter((p_161041_) -> {
            return p_161041_.state;
         })).apply(p_161039_, OreConfiguration.TargetBlockState::new);
      });
      public final RuleTest target;
      public final BlockState state;

      TargetBlockState(RuleTest p_161036_, BlockState p_161037_) {
         this.target = p_161036_;
         this.state = p_161037_;
      }
   }
}