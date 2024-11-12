package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public abstract class IntProvider {
   private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
   public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap((p_146543_) -> {
      return p_146543_.map(ConstantInt::of, (p_146549_) -> {
         return p_146549_;
      });
   }, (p_146541_) -> {
      return p_146541_.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)p_146541_).getValue()) : Either.right(p_146541_);
   });
   public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
   public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

   /**
    * Creates a codec for an IntProvider that only accepts numbers in the given range.
    */
   public static Codec<IntProvider> codec(int pMinInclusive, int pMaxInclusive) {
      return codec(pMinInclusive, pMaxInclusive, CODEC);
   }

   public static <T extends IntProvider> Codec<T> codec(int pMinInclusive, int pMaxInclusive, Codec<T> pCodec) {
      return ExtraCodecs.validate(pCodec, (p_274951_) -> {
         if (p_274951_.getMinValue() < pMinInclusive) {
            return DataResult.error(() -> {
               return "Value provider too low: " + pMinInclusive + " [" + p_274951_.getMinValue() + "-" + p_274951_.getMaxValue() + "]";
            });
         } else {
            return p_274951_.getMaxValue() > pMaxInclusive ? DataResult.error(() -> {
               return "Value provider too high: " + pMaxInclusive + " [" + p_274951_.getMinValue() + "-" + p_274951_.getMaxValue() + "]";
            }) : DataResult.success(p_274951_);
         }
      });
   }

   public abstract int sample(RandomSource pRandom);

   public abstract int getMinValue();

   public abstract int getMaxValue();

   public abstract IntProviderType<?> getType();
}