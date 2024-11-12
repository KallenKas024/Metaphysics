package net.minecraft.world.food;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;

public class FoodProperties {
   private final int nutrition;
   private final float saturationModifier;
   private final boolean isMeat;
   private final boolean canAlwaysEat;
   private final boolean fastFood;
   private final List<Pair<java.util.function.Supplier<MobEffectInstance>, Float>> effects;

   private FoodProperties(FoodProperties.Builder builder) {
      this.nutrition = builder.nutrition;
      this.saturationModifier = builder.saturationModifier;
      this.isMeat = builder.isMeat;
      this.canAlwaysEat = builder.canAlwaysEat;
      this.fastFood = builder.fastFood;
      this.effects = builder.effects;
   }

   // Forge: Use builder method instead
   @Deprecated
   FoodProperties(int pNutrition, float pSaturationModifier, boolean pIsMeat, boolean pCanAlwaysEat, boolean pFastFood, List<Pair<MobEffectInstance, Float>> pEffects) {
      this.nutrition = pNutrition;
      this.saturationModifier = pSaturationModifier;
      this.isMeat = pIsMeat;
      this.canAlwaysEat = pCanAlwaysEat;
      this.fastFood = pFastFood;
      this.effects = pEffects.stream().map(pair -> Pair.<java.util.function.Supplier<MobEffectInstance>, Float>of(pair::getFirst, pair.getSecond())).collect(java.util.stream.Collectors.toList());
   }

   public int getNutrition() {
      return this.nutrition;
   }

   public float getSaturationModifier() {
      return this.saturationModifier;
   }

   public boolean isMeat() {
      return this.isMeat;
   }

   public boolean canAlwaysEat() {
      return this.canAlwaysEat;
   }

   public boolean isFastFood() {
      return this.fastFood;
   }

   public List<Pair<MobEffectInstance, Float>> getEffects() {
      return this.effects.stream().map(pair -> Pair.of(pair.getFirst() != null ? pair.getFirst().get() : null, pair.getSecond())).collect(java.util.stream.Collectors.toList());
   }

   public static class Builder {
      private int nutrition;
      private float saturationModifier;
      private boolean isMeat;
      private boolean canAlwaysEat;
      private boolean fastFood;
      private final List<Pair<java.util.function.Supplier<MobEffectInstance>, Float>> effects = Lists.newArrayList();

      public FoodProperties.Builder nutrition(int pNutrition) {
         this.nutrition = pNutrition;
         return this;
      }

      public FoodProperties.Builder saturationMod(float pSaturationModifier) {
         this.saturationModifier = pSaturationModifier;
         return this;
      }

      public FoodProperties.Builder meat() {
         this.isMeat = true;
         return this;
      }

      public FoodProperties.Builder alwaysEat() {
         this.canAlwaysEat = true;
         return this;
      }

      public FoodProperties.Builder fast() {
         this.fastFood = true;
         return this;
      }

      public FoodProperties.Builder effect(java.util.function.Supplier<MobEffectInstance> effectIn, float probability) {
          this.effects.add(Pair.of(effectIn, probability));
          return this;
       }

      // Forge: Use supplier method instead
      @Deprecated
      public FoodProperties.Builder effect(MobEffectInstance pEffect, float pProbability) {
         this.effects.add(Pair.of(() -> pEffect, pProbability));
         return this;
      }

      public FoodProperties build() {
         return new FoodProperties(this);
      }
   }
}
