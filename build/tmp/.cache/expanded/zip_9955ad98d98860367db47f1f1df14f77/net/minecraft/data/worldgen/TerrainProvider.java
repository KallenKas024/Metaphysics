package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
   private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51F;
   private static final float OCEAN_CONTINENTALNESS = -0.4F;
   private static final float PLAINS_CONTINENTALNESS = 0.1F;
   private static final float BEACH_CONTINENTALNESS = -0.15F;
   private static final ToFloatFunction<Float> NO_TRANSFORM = ToFloatFunction.IDENTITY;
   private static final ToFloatFunction<Float> AMPLIFIED_OFFSET = ToFloatFunction.createUnlimited((p_236651_) -> {
      return p_236651_ < 0.0F ? p_236651_ : p_236651_ * 2.0F;
   });
   private static final ToFloatFunction<Float> AMPLIFIED_FACTOR = ToFloatFunction.createUnlimited((p_236649_) -> {
      return 1.25F - 6.25F / (p_236649_ + 5.0F);
   });
   private static final ToFloatFunction<Float> AMPLIFIED_JAGGEDNESS = ToFloatFunction.createUnlimited((p_236641_) -> {
      return p_236641_ * 2.0F;
   });

   public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldOffset(I pContinents, I pErosion, I pRidgesFolded, boolean pAmplified) {
      ToFloatFunction<Float> tofloatfunction = pAmplified ? AMPLIFIED_OFFSET : NO_TRANSFORM;
      CubicSpline<C, I> cubicspline = buildErosionOffsetSpline(pErosion, pRidgesFolded, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, tofloatfunction);
      CubicSpline<C, I> cubicspline1 = buildErosionOffsetSpline(pErosion, pRidgesFolded, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false, tofloatfunction);
      CubicSpline<C, I> cubicspline2 = buildErosionOffsetSpline(pErosion, pRidgesFolded, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true, tofloatfunction);
      CubicSpline<C, I> cubicspline3 = buildErosionOffsetSpline(pErosion, pRidgesFolded, -0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true, tofloatfunction);
      return CubicSpline.<C, I>builder(pContinents, tofloatfunction).addPoint(-1.1F, 0.044F).addPoint(-1.02F, -0.2222F).addPoint(-0.51F, -0.2222F).addPoint(-0.44F, -0.12F).addPoint(-0.18F, -0.12F).addPoint(-0.16F, cubicspline).addPoint(-0.15F, cubicspline).addPoint(-0.1F, cubicspline1).addPoint(0.25F, cubicspline2).addPoint(1.0F, cubicspline3).build();
   }

   public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldFactor(I pContinents, I pErosion, I pRidges, I pRidgesFolded, boolean pAmplified) {
      ToFloatFunction<Float> tofloatfunction = pAmplified ? AMPLIFIED_FACTOR : NO_TRANSFORM;
      return CubicSpline.<C, I>builder(pContinents, NO_TRANSFORM).addPoint(-0.19F, 3.95F).addPoint(-0.15F, getErosionFactor(pErosion, pRidges, pRidgesFolded, 6.25F, true, NO_TRANSFORM)).addPoint(-0.1F, getErosionFactor(pErosion, pRidges, pRidgesFolded, 5.47F, true, tofloatfunction)).addPoint(0.03F, getErosionFactor(pErosion, pRidges, pRidgesFolded, 5.08F, true, tofloatfunction)).addPoint(0.06F, getErosionFactor(pErosion, pRidges, pRidgesFolded, 4.69F, false, tofloatfunction)).build();
   }

   public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(I pContinents, I pErosion, I pRidges, I pRidgesFolded, boolean pAmplified) {
      ToFloatFunction<Float> tofloatfunction = pAmplified ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
      float f = 0.65F;
      return CubicSpline.<C, I>builder(pContinents, tofloatfunction).addPoint(-0.11F, 0.0F).addPoint(0.03F, buildErosionJaggednessSpline(pErosion, pRidges, pRidgesFolded, 1.0F, 0.5F, 0.0F, 0.0F, tofloatfunction)).addPoint(0.65F, buildErosionJaggednessSpline(pErosion, pRidges, pRidgesFolded, 1.0F, 1.0F, 1.0F, 0.0F, tofloatfunction)).build();
   }

   private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(I pErosion, I pRidges, I pRidgesFolded, float p_236617_, float p_236618_, float p_236619_, float p_236620_, ToFloatFunction<Float> pTransform) {
      float f = -0.5775F;
      CubicSpline<C, I> cubicspline = buildRidgeJaggednessSpline(pRidges, pRidgesFolded, p_236617_, p_236619_, pTransform);
      CubicSpline<C, I> cubicspline1 = buildRidgeJaggednessSpline(pRidges, pRidgesFolded, p_236618_, p_236620_, pTransform);
      return CubicSpline.<C, I>builder(pErosion, pTransform).addPoint(-1.0F, cubicspline).addPoint(-0.78F, cubicspline1).addPoint(-0.5775F, cubicspline1).addPoint(-0.375F, 0.0F).build();
   }

   private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(I pRidges, I pRidgesFolded, float p_236610_, float p_236611_, ToFloatFunction<Float> pTransform) {
      float f = NoiseRouterData.peaksAndValleys(0.4F);
      float f1 = NoiseRouterData.peaksAndValleys(0.56666666F);
      float f2 = (f + f1) / 2.0F;
      CubicSpline.Builder<C, I> builder = CubicSpline.builder(pRidgesFolded, pTransform);
      builder.addPoint(f, 0.0F);
      if (p_236611_ > 0.0F) {
         builder.addPoint(f2, buildWeirdnessJaggednessSpline(pRidges, p_236611_, pTransform));
      } else {
         builder.addPoint(f2, 0.0F);
      }

      if (p_236610_ > 0.0F) {
         builder.addPoint(1.0F, buildWeirdnessJaggednessSpline(pRidges, p_236610_, pTransform));
      } else {
         builder.addPoint(1.0F, 0.0F);
      }

      return builder.build();
   }

   private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(I pRidges, float p_236588_, ToFloatFunction<Float> pTransform) {
      float f = 0.63F * p_236588_;
      float f1 = 0.3F * p_236588_;
      return CubicSpline.<C, I>builder(pRidges, pTransform).addPoint(-0.01F, f).addPoint(0.01F, f1).build();
   }

   private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> getErosionFactor(I pErosion, I pRidges, I pRidgesFolded, float p_236626_, boolean p_236627_, ToFloatFunction<Float> pTransform) {
      CubicSpline<C, I> cubicspline = CubicSpline.<C, I>builder(pRidges, pTransform).addPoint(-0.2F, 6.3F).addPoint(0.2F, p_236626_).build();
      CubicSpline.Builder<C, I> builder = CubicSpline.<C, I>builder(pErosion, pTransform).addPoint(-0.6F, cubicspline).addPoint(-0.5F, CubicSpline.<C, I>builder(pRidges, pTransform).addPoint(-0.05F, 6.3F).addPoint(0.05F, 2.67F).build()).addPoint(-0.35F, cubicspline).addPoint(-0.25F, cubicspline).addPoint(-0.1F, CubicSpline.<C, I>builder(pRidges, pTransform).addPoint(-0.05F, 2.67F).addPoint(0.05F, 6.3F).build()).addPoint(0.03F, cubicspline);
      if (p_236627_) {
         CubicSpline<C, I> cubicspline1 = CubicSpline.<C, I>builder(pRidges, pTransform).addPoint(0.0F, p_236626_).addPoint(0.1F, 0.625F).build();
         CubicSpline<C, I> cubicspline2 = CubicSpline.<C, I>builder(pRidgesFolded, pTransform).addPoint(-0.9F, p_236626_).addPoint(-0.69F, cubicspline1).build();
         builder.addPoint(0.35F, p_236626_).addPoint(0.45F, cubicspline2).addPoint(0.55F, cubicspline2).addPoint(0.62F, p_236626_);
      } else {
         CubicSpline<C, I> cubicspline3 = CubicSpline.<C, I>builder(pRidgesFolded, pTransform).addPoint(-0.7F, cubicspline).addPoint(-0.15F, 1.37F).build();
         CubicSpline<C, I> cubicspline4 = CubicSpline.<C, I>builder(pRidgesFolded, pTransform).addPoint(0.45F, cubicspline).addPoint(0.7F, 1.56F).build();
         builder.addPoint(0.05F, cubicspline4).addPoint(0.4F, cubicspline4).addPoint(0.45F, cubicspline3).addPoint(0.55F, cubicspline3).addPoint(0.58F, p_236626_);
      }

      return builder.build();
   }

   private static float calculateSlope(float pY1, float pY2, float pX1, float pX2) {
      return (pY2 - pY1) / (pX2 - pX1);
   }

   private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(I pRidgesFolded, float p_236592_, boolean p_236593_, ToFloatFunction<Float> pTransform) {
      CubicSpline.Builder<C, I> builder = CubicSpline.builder(pRidgesFolded, pTransform);
      float f = -0.7F;
      float f1 = -1.0F;
      float f2 = mountainContinentalness(-1.0F, p_236592_, -0.7F);
      float f3 = 1.0F;
      float f4 = mountainContinentalness(1.0F, p_236592_, -0.7F);
      float f5 = calculateMountainRidgeZeroContinentalnessPoint(p_236592_);
      float f6 = -0.65F;
      if (-0.65F < f5 && f5 < 1.0F) {
         float f14 = mountainContinentalness(-0.65F, p_236592_, -0.7F);
         float f8 = -0.75F;
         float f9 = mountainContinentalness(-0.75F, p_236592_, -0.7F);
         float f10 = calculateSlope(f2, f9, -1.0F, -0.75F);
         builder.addPoint(-1.0F, f2, f10);
         builder.addPoint(-0.75F, f9);
         builder.addPoint(-0.65F, f14);
         float f11 = mountainContinentalness(f5, p_236592_, -0.7F);
         float f12 = calculateSlope(f11, f4, f5, 1.0F);
         float f13 = 0.01F;
         builder.addPoint(f5 - 0.01F, f11);
         builder.addPoint(f5, f11, f12);
         builder.addPoint(1.0F, f4, f12);
      } else {
         float f7 = calculateSlope(f2, f4, -1.0F, 1.0F);
         if (p_236593_) {
            builder.addPoint(-1.0F, Math.max(0.2F, f2));
            builder.addPoint(0.0F, Mth.lerp(0.5F, f2, f4), f7);
         } else {
            builder.addPoint(-1.0F, f2, f7);
         }

         builder.addPoint(1.0F, f4, f7);
      }

      return builder.build();
   }

   private static float mountainContinentalness(float p_236569_, float p_236570_, float p_236571_) {
      float f = 1.17F;
      float f1 = 0.46082947F;
      float f2 = 1.0F - (1.0F - p_236570_) * 0.5F;
      float f3 = 0.5F * (1.0F - p_236570_);
      float f4 = (p_236569_ + 1.17F) * 0.46082947F;
      float f5 = f4 * f2 - f3;
      return p_236569_ < p_236571_ ? Math.max(f5, -0.2222F) : Math.max(f5, 0.0F);
   }

   private static float calculateMountainRidgeZeroContinentalnessPoint(float p_236567_) {
      float f = 1.17F;
      float f1 = 0.46082947F;
      float f2 = 1.0F - (1.0F - p_236567_) * 0.5F;
      float f3 = 0.5F * (1.0F - p_236567_);
      return f3 / (0.46082947F * f2) - 1.17F;
   }

   public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(I pErosion, I pRidgesFolded, float p_236598_, float p_236599_, float p_236600_, float p_236601_, float p_236602_, float p_236603_, boolean p_236604_, boolean p_236605_, ToFloatFunction<Float> pTransform) {
      float f = 0.6F;
      float f1 = 0.5F;
      float f2 = 0.5F;
      CubicSpline<C, I> cubicspline = buildMountainRidgeSplineWithPoints(pRidgesFolded, Mth.lerp(p_236601_, 0.6F, 1.5F), p_236605_, pTransform);
      CubicSpline<C, I> cubicspline1 = buildMountainRidgeSplineWithPoints(pRidgesFolded, Mth.lerp(p_236601_, 0.6F, 1.0F), p_236605_, pTransform);
      CubicSpline<C, I> cubicspline2 = buildMountainRidgeSplineWithPoints(pRidgesFolded, p_236601_, p_236605_, pTransform);
      CubicSpline<C, I> cubicspline3 = ridgeSpline(pRidgesFolded, p_236598_ - 0.15F, 0.5F * p_236601_, Mth.lerp(0.5F, 0.5F, 0.5F) * p_236601_, 0.5F * p_236601_, 0.6F * p_236601_, 0.5F, pTransform);
      CubicSpline<C, I> cubicspline4 = ridgeSpline(pRidgesFolded, p_236598_, p_236602_ * p_236601_, p_236599_ * p_236601_, 0.5F * p_236601_, 0.6F * p_236601_, 0.5F, pTransform);
      CubicSpline<C, I> cubicspline5 = ridgeSpline(pRidgesFolded, p_236598_, p_236602_, p_236602_, p_236599_, p_236600_, 0.5F, pTransform);
      CubicSpline<C, I> cubicspline6 = ridgeSpline(pRidgesFolded, p_236598_, p_236602_, p_236602_, p_236599_, p_236600_, 0.5F, pTransform);
      CubicSpline<C, I> cubicspline7 = CubicSpline.<C, I>builder(pRidgesFolded, pTransform).addPoint(-1.0F, p_236598_).addPoint(-0.4F, cubicspline5).addPoint(0.0F, p_236600_ + 0.07F).build();
      CubicSpline<C, I> cubicspline8 = ridgeSpline(pRidgesFolded, -0.02F, p_236603_, p_236603_, p_236599_, p_236600_, 0.0F, pTransform);
      CubicSpline.Builder<C, I> builder = CubicSpline.<C, I>builder(pErosion, pTransform).addPoint(-0.85F, cubicspline).addPoint(-0.7F, cubicspline1).addPoint(-0.4F, cubicspline2).addPoint(-0.35F, cubicspline3).addPoint(-0.1F, cubicspline4).addPoint(0.2F, cubicspline5);
      if (p_236604_) {
         builder.addPoint(0.4F, cubicspline6).addPoint(0.45F, cubicspline7).addPoint(0.55F, cubicspline7).addPoint(0.58F, cubicspline6);
      }

      builder.addPoint(0.7F, cubicspline8);
      return builder.build();
   }

   private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> ridgeSpline(I pRidgesFolded, float p_236579_, float p_236580_, float p_236581_, float p_236582_, float p_236583_, float p_236584_, ToFloatFunction<Float> pTransform) {
      float f = Math.max(0.5F * (p_236580_ - p_236579_), p_236584_);
      float f1 = 5.0F * (p_236581_ - p_236580_);
      return CubicSpline.<C, I>builder(pRidgesFolded, pTransform).addPoint(-1.0F, p_236579_, f).addPoint(-0.4F, p_236580_, Math.min(f, f1)).addPoint(0.0F, p_236581_, f1).addPoint(0.4F, p_236582_, 2.0F * (p_236582_ - p_236581_)).addPoint(1.0F, p_236583_, 0.7F * (p_236583_ - p_236582_)).build();
   }
}