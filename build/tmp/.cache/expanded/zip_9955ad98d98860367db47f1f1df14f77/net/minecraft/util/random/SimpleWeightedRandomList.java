package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public class SimpleWeightedRandomList<E> extends WeightedRandomList<WeightedEntry.Wrapper<E>> {
   public static <E> Codec<SimpleWeightedRandomList<E>> wrappedCodecAllowingEmpty(Codec<E> pCodec) {
      return WeightedEntry.Wrapper.codec(pCodec).listOf().xmap(SimpleWeightedRandomList::new, WeightedRandomList::unwrap);
   }

   public static <E> Codec<SimpleWeightedRandomList<E>> wrappedCodec(Codec<E> pElementCodec) {
      return ExtraCodecs.nonEmptyList(WeightedEntry.Wrapper.codec(pElementCodec).listOf()).xmap(SimpleWeightedRandomList::new, WeightedRandomList::unwrap);
   }

   SimpleWeightedRandomList(List<? extends WeightedEntry.Wrapper<E>> p_146262_) {
      super(p_146262_);
   }

   public static <E> SimpleWeightedRandomList.Builder<E> builder() {
      return new SimpleWeightedRandomList.Builder<>();
   }

   public static <E> SimpleWeightedRandomList<E> empty() {
      return new SimpleWeightedRandomList<>(List.of());
   }

   public static <E> SimpleWeightedRandomList<E> single(E pData) {
      return new SimpleWeightedRandomList<>(List.of(WeightedEntry.wrap(pData, 1)));
   }

   public Optional<E> getRandomValue(RandomSource pRandom) {
      return this.getRandom(pRandom).map(WeightedEntry.Wrapper::getData);
   }

   public static class Builder<E> {
      private final ImmutableList.Builder<WeightedEntry.Wrapper<E>> result = ImmutableList.builder();

      public SimpleWeightedRandomList.Builder<E> add(E pData, int pWeight) {
         this.result.add(WeightedEntry.wrap(pData, pWeight));
         return this;
      }

      public SimpleWeightedRandomList<E> build() {
         return new SimpleWeightedRandomList<>(this.result.build());
      }
   }
}